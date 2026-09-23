package org.oosd.controller;

import javafx.scene.paint.Color;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoFactory;
import org.oosd.model.TetrominoType;

// AI Play feature. Tries every (rotation, column) for the current piece on a
// cloned copy of the board, scores each result, and steers the real piece to
// the best one using the normal move/rotate/drop calls (same as a keypress
// would). Scoring weights are the standard height/lines/holes/bumpiness ones.
public class AIPlayer {

    private static final double HEIGHT_WEIGHT = -0.510066;
    private static final double LINES_WEIGHT = 0.760666;
    private static final double HOLES_WEIGHT = -0.35663;
    private static final double BUMPINESS_WEIGHT = -0.184483;

    private record Placement(int rotations, int col) {
    }

    private Tetromino planningForPiece;
    private Placement target;

    public void reset() {
        planningForPiece = null;
        target = null;
    }

    // One move per call - GameController calls this on a short timer so the
    // piece visibly steps toward its target instead of teleporting there.
    public void takeTurn(GameController controller) {
        Tetromino piece = controller.getCurrentPiece();
        if (piece == null) {
            return;
        }

        if (piece != planningForPiece) {
            target = findBestPlacement(controller.getBoard(), piece);
            planningForPiece = piece;
        }
        if (target == null) {
            return; // no legal placement exists - board is effectively full, let it top out
        }

        if (piece.getRotationState() != target.rotations()) {
            controller.handleRotate();
        } else if (piece.getCol() < target.col()) {
            controller.handleMoveRight();
        } else if (piece.getCol() > target.col()) {
            controller.handleMoveLeft();
        } else {
            controller.handleSoftDrop();
        }
    }

    private Placement findBestPlacement(Board board, Tetromino piece) {
        int width = board.getWidth();
        int height = board.getHeight();
        Color[][] snapshot = cloneGrid(board.getGrid());
        TetrominoType type = piece.getType();

        Placement best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int rotations = 0; rotations < 4; rotations++) {
            Tetromino probe = TetrominoFactory.create(type, 0);
            for (int i = 0; i < rotations; i++) {
                probe.applyRotation();
            }
            int[][] cells = probe.getCurrentCells();

            int minColOffset = Integer.MAX_VALUE;
            int maxColOffset = Integer.MIN_VALUE;
            for (int[] cell : cells) {
                minColOffset = Math.min(minColOffset, cell[1]);
                maxColOffset = Math.max(maxColOffset, cell[1]);
            }
            int firstCol = -minColOffset;
            int lastCol = width - 1 - maxColOffset;

            for (int col = firstCol; col <= lastCol; col++) {
                if (!isValidOn(snapshot, width, height, cells, 0, col)) {
                    continue; // this column is already blocked right at spawn height
                }

                int row = 0;
                while (isValidOn(snapshot, width, height, cells, row + 1, col)) {
                    row++;
                }

                Color[][] result = cloneGrid(snapshot);
                for (int[] cell : cells) {
                    result[row + cell[0]][col + cell[1]] = type.getColor();
                }
                int linesCleared = clearFullRowsOn(result, width, height);

                double score = evaluate(result, width, height, linesCleared);
                if (score > bestScore) {
                    bestScore = score;
                    best = new Placement(rotations, col);
                }
            }
        }
        return best;
    }

    private double evaluate(Color[][] grid, int width, int height, int linesCleared) {
        int[] heights = new int[width];
        int holes = 0;

        for (int c = 0; c < width; c++) {
            int topFilledRow = -1;
            for (int r = 0; r < height; r++) {
                if (grid[r][c] != null) {
                    topFilledRow = r;
                    break;
                }
            }
            heights[c] = (topFilledRow == -1) ? 0 : height - topFilledRow;

            if (topFilledRow != -1) {
                for (int r = topFilledRow + 1; r < height; r++) {
                    if (grid[r][c] == null) {
                        holes++;
                    }
                }
            }
        }

        int aggregateHeight = 0;
        int bumpiness = 0;
        for (int c = 0; c < width; c++) {
            aggregateHeight += heights[c];
            if (c > 0) {
                bumpiness += Math.abs(heights[c] - heights[c - 1]);
            }
        }

        return HEIGHT_WEIGHT * aggregateHeight
                + LINES_WEIGHT * linesCleared
                + HOLES_WEIGHT * holes
                + BUMPINESS_WEIGHT * bumpiness;
    }

    private boolean isValidOn(Color[][] grid, int width, int height, int[][] cells, int row, int col) {
        for (int[] cell : cells) {
            int r = row + cell[0];
            int c = col + cell[1];
            if (c < 0 || c >= width || r >= height) {
                return false;
            }
            if (r >= 0 && grid[r][c] != null) {
                return false;
            }
        }
        return true;
    }

    private int clearFullRowsOn(Color[][] grid, int width, int height) {
        int cleared = 0;
        for (int r = 0; r < height; r++) {
            boolean full = true;
            for (Color cell : grid[r]) {
                if (cell == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int shiftRow = r; shiftRow > 0; shiftRow--) {
                    grid[shiftRow] = grid[shiftRow - 1].clone();
                }
                grid[0] = new Color[width];
                cleared++;
            }
        }
        return cleared;
    }

    private Color[][] cloneGrid(Color[][] source) {
        Color[][] copy = new Color[source.length][];
        for (int r = 0; r < source.length; r++) {
            copy[r] = source[r].clone();
        }
        return copy;
    }
}
