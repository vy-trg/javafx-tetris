package org.oosd.controller;

import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoFactory;

// The Controller in our MVC split. Used to all live inside GameScreen along
// with the JavaFX stuff, now it just holds Board/Tetromino and the game
// rules, and delegates paused/gameOver behaviour to whichever GameState is
// currently active instead of branching on booleans.
public class GameController {

    public static final long FALL_INTERVAL_NANOS = 600_000_000L; // 600ms per row
    public static final long AI_ACTION_INTERVAL_NANOS = 120_000_000L; // ~8 AI moves/sec, so play is watchable

    private final int width;
    private final int height;
    private final TetrominoSequence sharedSequence;
    private int pieceIndex;

    private Board board;
    private Tetromino currentPiece;
    private int score;
    private long lastFallTime;

    private final AIPlayer aiPlayer = new AIPlayer();
    private boolean aiEnabled;
    private long lastAiActionTime;

    private GameState state = PlayingState.INSTANCE;

    // so GameScreen finds out the moment the game ends and can pop the name-entry dialog
    private Runnable onGameOver = () -> { };

    public GameController() {
        this(Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT);
    }

    public GameController(int width, int height) {
        this(width, height, null);
    }

    // sharedSequence is only used for two-player mode, so both players get the same pieces
    public GameController(int width, int height, TetrominoSequence sharedSequence) {
        this.width = width;
        this.height = height;
        this.sharedSequence = sharedSequence;
    }

    public void start() {
        board = new Board(width, height);
        score = 0;
        lastFallTime = 0;
        lastAiActionTime = 0;
        pieceIndex = 0;
        aiPlayer.reset();
        state = PlayingState.INSTANCE;
        spawnNewPiece();
    }

    public void tick(long now) {
        state.tick(this, now);

        if (aiEnabled && !isPaused() && !isGameOver()) {
            if (lastAiActionTime == 0 || now - lastAiActionTime >= AI_ACTION_INTERVAL_NANOS) {
                aiPlayer.takeTurn(this);
                lastAiActionTime = now;
            }
        }
    }

    public void setAiEnabled(boolean enabled) {
        this.aiEnabled = enabled;
        if (enabled) {
            aiPlayer.reset();
            lastAiActionTime = 0;
        }
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void handleMoveLeft() {
        state.handleMoveLeft(this);
    }

    public void handleMoveRight() {
        state.handleMoveRight(this);
    }

    public void handleSoftDrop() {
        state.handleSoftDrop(this);
    }

    public void handleRotate() {
        state.handleRotate(this);
    }

    public void handlePauseKey() {
        state.handlePauseKey(this);
    }

    // --- Package-visible: only GameState implementations call these ---

    void setState(GameState newState) {
        this.state = newState;
        if (newState.isGameOver()) {
            onGameOver.run();
        }
    }

    void advanceFallIfDue(long now) {
        if (lastFallTime == 0) {
            lastFallTime = now;
            return;
        }
        if (now - lastFallTime >= FALL_INTERVAL_NANOS) {
            moveDown();
            lastFallTime = now;
        }
    }

    void moveDown() {
        if (board.isValidPosition(currentPiece, 1, 0, currentPiece.getCurrentCells())) {
            currentPiece.moveDown();
        } else {
            lockAndClear();
        }
    }

    void tryMove(int colDelta) {
        if (board.isValidPosition(currentPiece, 0, colDelta, currentPiece.getCurrentCells())) {
            if (colDelta < 0) {
                currentPiece.moveLeft();
            } else {
                currentPiece.moveRight();
            }
        }
    }

    void tryRotate() {
        int[][] rotated = currentPiece.rotateClockwise();
        if (board.isValidPosition(currentPiece, 0, 0, rotated)) {
            currentPiece.applyRotation();
        }
    }

    private void lockAndClear() {
        board.lockPiece(currentPiece);
        Board.RowClearResult result = board.clearFullRows();
        if (result.rowsCleared() > 0) {
            score += result.pointsEarned();
        }
        spawnNewPiece();
    }

    private void spawnNewPiece() {
        int startCol = board.getWidth() / 2 - 2;
        // Two-Player Mode: draw from the shared sequence (by spawn count) so both
        // players see the same piece order; single-player keeps its own randomness.
        currentPiece = (sharedSequence != null)
                ? TetrominoFactory.create(sharedSequence.get(pieceIndex++), startCol)
                : TetrominoFactory.createRandom(startCol);
        if (!board.isValidPosition(currentPiece, 0, 0, currentPiece.getCurrentCells())) {
            setState(GameOverState.INSTANCE);
        }
    }

    // --- Read-only access for the View ---

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    public int getScore() {
        return score;
    }

    public boolean isPaused() {
        return state.isPaused();
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }
}
