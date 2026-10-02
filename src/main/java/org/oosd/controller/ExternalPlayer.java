package org.oosd.controller;

import javafx.scene.paint.Color;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//External Player
//asks TetrisServer.jar (localhost:3000) where to put current piece, then steers real piece there
public class ExternalPlayer {

    public static final String HOST = "localhost";
    public static final int PORT = 3000;

    private static final int CONNECT_TIMEOUT_MS = 1000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final long HEARTBEAT_INTERVAL_NANOS = 1_000_000_000L;
    //while offline, probe faster so control resumes almost the instant the server is started
    private static final long RECONNECT_INTERVAL_NANOS = 250_000_000L;

    private static final Pattern OP_X = Pattern.compile("\"opX\"\\s*:\\s*(-?\\d+)");
    private static final Pattern OP_ROTATE = Pattern.compile("\"opRotate\"\\s*:\\s*(-?\\d+)");

    private record Move(int x, int rotate) {
    }

    //server answered but reply unusable, not a connection problem so does not count as offline
    private static class ProtocolException extends IOException {
        ProtocolException(String message) {
            super(message);
        }
    }

    private record Target(int rotationState, int col) {
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "external-player");
        thread.setDaemon(true); //never keep app alive after window closes
        return thread;
    });

    //false while TetrisServer unreachable, game keeps running but piece is not steered.
    //flips back to true by itself as soon as the server answers again.
    private volatile boolean connected = true;
    private volatile String disconnectReason = "";
    private volatile int generation;
    private boolean wasConnected = true; //FX-thread copy, used to spot the offline -> online edge

    private Tetromino planningForPiece;
    private CompletableFuture<Move> pending;
    private int pendingStartRotation;
    private int[][] pendingMatrix;
    private Target target;

    private long lastHeartbeat;
    private boolean heartbeatInFlight;

    //quick "is TetrisServer.jar running?" check used before game start, only opens/closes connection - server ignores empty one.
    public static boolean isServerReachable() {
        try {
            probe();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void probe() throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT_MS);
        }
    }

    public boolean isDisconnected() {
        return !connected;
    }

    public String getDisconnectReason() {
        return disconnectReason;
    }

    public void reset() {
        generation++; //any in-flight reply belongs to old game and is ignored
        planningForPiece = null;
        pending = null;
        target = null;
        lastHeartbeat = 0;
        heartbeatInFlight = false;
        connected = true;
        wasConnected = true;
        disconnectReason = "";
    }

    public void shutdown() {
        generation++;
        executor.shutdownNow();
    }

    //one step per call, gamecontroller calls this on same short timer as AI
    public void takeTurn(GameController controller, long now) {
        heartbeat(now); //keep running while offline too, that's how we notice server came back

        boolean nowConnected = connected;
        if (!nowConnected) {
            wasConnected = false;
            return; //server down, warn (see isDisconnected) and leave piece alone, no control
        }
        if (!wasConnected) {
            //server came back mid-game, drop stale plan so current piece is re-planne now
            wasConnected = true;
            planningForPiece = null;
            pending = null;
            target = null;
        }

        Tetromino piece = controller.getCurrentPiece();
        if (piece == null) {
            return;
        }

        if (piece != planningForPiece) {
            requestMove(controller, piece);
            planningForPiece = piece;
            target = null;
        }

        if (pending != null) {
            if (!pending.isDone()) {
                return; //still waiting on server, piece keeps falling
            }
            Move move;
            try {
                move = pending.get();
            } catch (Exception e) {
                //connection lost (already flagged by worker) or unusable reply, no move for this piece
                pending = null;
                target = null;
                return;
            }
            pending = null;
            target = toTarget(piece, move);
            if (target == null) {
                return; //move does not fit this piece, ignore it, next piece asks again
            }
        }

        if (target == null) {
            return;
        }

        if (piece.getRotationState() != target.rotationState()) {
            controller.handleRotate();
        } else if (piece.getCol() < target.col()) {
            controller.handleMoveRight();
        } else if (piece.getCol() > target.col()) {
            controller.handleMoveLeft();
        } else {
            controller.handleSoftDrop();
        }
    }

    //periodically open/close connection so server that dies while a piece is falling is noticed, and so server started later is noticed straight away (faster polling while offline).
    private void heartbeat(long now) {
        long interval = connected ? HEARTBEAT_INTERVAL_NANOS : RECONNECT_INTERVAL_NANOS;
        if (heartbeatInFlight || (lastHeartbeat != 0 && now - lastHeartbeat < interval)) {
            return;
        }
        lastHeartbeat = now;
        heartbeatInFlight = true;
        int gen = generation;
        try {
            executor.submit(() -> {
                try {
                    probe();
                    if (gen == generation) {
                        markConnected();
                    }
                } catch (IOException e) {
                    if (gen == generation) {
                        markDisconnected("TetrisServer is not running (" + rootMessage(e) + ")");
                    }
                } finally {
                    if (gen == generation) {
                        heartbeatInFlight = false;
                    }
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            heartbeatInFlight = false; //shut down
        }
    }

    private void requestMove(GameController controller, Tetromino piece) {
        Board board = controller.getBoard();
        TetrominoType nextType = controller.getNextPieceType();

        pendingStartRotation = piece.getRotationState();
        pendingMatrix = toMatrix(piece.getCurrentCells());

        int[][] nextMatrix = pendingMatrix; // no look-ahead available -> fall back to the current shape
        if (nextType != null) {
            nextMatrix = toMatrix(org.oosd.model.TetrominoFactory.create(nextType, 0).getCurrentCells());
        }

        String json = buildRequest(board, pendingMatrix, nextMatrix);
        int gen = generation;
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                Move move = send(json);
                if (gen != generation) {
                    throw new IllegalStateException("stale");
                }
                return move;
            } catch (ProtocolException e) {
                throw new IllegalStateException(e); //server is there, reply just unusable
            } catch (IOException e) {
                if (gen == generation) {
                    markDisconnected("Lost connection to TetrisServer (" + rootMessage(e) + ")");
                }
                throw new IllegalStateException(e);
            }
        }, executor);
    }

    private static Move send(String json) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out.println(json);
            String line = in.readLine();
            if (line == null || line.isBlank()) {
                throw new ProtocolException("server closed the connection");
            }
            Matcher x = OP_X.matcher(line);
            Matcher r = OP_ROTATE.matcher(line);
            if (!x.find() || !r.find()) {
                throw new ProtocolException("unexpected reply from server");
            }
            return new Move(Integer.parseInt(x.group(1)), Integer.parseInt(r.group(1)));
        }
    }

    private void markDisconnected(String reason) {
        disconnectReason = reason;
        connected = false;
    }

    private void markConnected() {
        connected = true;
        disconnectReason = "";
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        String message = t.getMessage();
        return (message == null || message.isBlank()) ? t.getClass().getSimpleName() : message;
    }

    //translating between piece model and server matrices
    private static String buildRequest(Board board, int[][] current, int[][] next) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"width\":").append(board.getWidth())
                .append(",\"height\":").append(board.getHeight())
                .append(",\"cells\":[");
        Color[][] grid = board.getGrid();
        for (int r = 0; r < grid.length; r++) {
            if (r > 0) {
                sb.append(',');
            }
            sb.append('[');
            for (int c = 0; c < grid[r].length; c++) {
                if (c > 0) {
                    sb.append(',');
                }
                sb.append(grid[r][c] != null ? 1 : 0);
            }
            sb.append(']');
        }
        sb.append("],\"currentShape\":").append(matrixJson(current))
                .append(",\"nextShape\":").append(matrixJson(next))
                .append('}');
        return sb.toString();
    }

    private static String matrixJson(int[][] matrix) {
        StringBuilder sb = new StringBuilder("[");
        for (int r = 0; r < matrix.length; r++) {
            if (r > 0) {
                sb.append(',');
            }
            sb.append(Arrays.toString(matrix[r]).replace(" ", ""));
        }
        return sb.append(']').toString();
    }

    //trimmed 0/1 bounding-box matrix of set of {row, col} cells.
    private static int[][] toMatrix(int[][] cells) {
        int minR = Integer.MAX_VALUE, maxR = Integer.MIN_VALUE;
        int minC = Integer.MAX_VALUE, maxC = Integer.MIN_VALUE;
        for (int[] cell : cells) {
            minR = Math.min(minR, cell[0]);
            maxR = Math.max(maxR, cell[0]);
            minC = Math.min(minC, cell[1]);
            maxC = Math.max(maxC, cell[1]);
        }
        int[][] matrix = new int[maxR - minR + 1][maxC - minC + 1];
        for (int[] cell : cells) {
            matrix[cell[0] - minR][cell[1] - minC] = 1;
        }
        return matrix;
    }

    private static int minCol(int[][] cells) {
        int min = Integer.MAX_VALUE;
        for (int[] cell : cells) {
            min = Math.min(min, cell[1]);
        }
        return min;
    }

    private static int[][] rotateClockwise(int[][] m) {
        int h = m.length;
        int w = m[0].length;
        int[][] rotated = new int[w][h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rotated[j][h - 1 - i] = m[i][j];
            }
        }
        return rotated;
    }

    //server counts clockwise turns of shape it was sent
    //pieces store 4 fixed rotation states, find state whose shape equals the server's rotated shape.
    private Target toTarget(Tetromino piece, Move move) {
        if (move.rotate() < 0 || move.rotate() > 3) {
            return null;
        }
        int[][] wanted = pendingMatrix;
        for (int i = 0; i < move.rotate(); i++) {
            wanted = rotateClockwise(wanted);
        }

        Tetromino probe = org.oosd.model.TetrominoFactory.create(piece.getType(), 0);
        for (int i = 0; i < pendingStartRotation; i++) {
            probe.applyRotation();
        }
        for (int turns = 0; turns < 4; turns++) {
            int[][] cells = probe.getCurrentCells();
            if (Arrays.deepEquals(toMatrix(cells), wanted)) {
                return new Target(probe.getRotationState(), move.x() - minCol(cells));
            }
            probe.applyRotation();
        }
        return null;
    }
}
