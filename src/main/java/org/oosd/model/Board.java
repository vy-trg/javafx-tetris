package org.oosd.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Board {

    /**
     * Default board size - what you get from {@code new Board()}, and the
     * defaults shown on the Configuration Screen's spinners.
     * Dynamic Grid Size Adjustment (Vy): the board no longer has to be this
     * size - {@link #Board(int, int)} builds any width/height the player
     * chose (spinner ranges: width 5-15, height 15-30), and everything else
     * (GameController, GameScreen, AIPlayer) reads the size back from the
     * Board instance instead of assuming a fixed 10x20.
     */
    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_HEIGHT = 20;

    private final int width;
    private final int height;
    private final Color[][] grid;

    public Board() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Color[height][width];
    }

    /**
     * Record type usage (Milestone 1 rubric requirement).
     * Immutable result of a row-clear check: how many rows were cleared and
     * how many points that earns. Naturally a simple data carrier, no
     * behaviour attached, so a record fits better than a full class.
     */
    public record RowClearResult(int rowsCleared, int pointsEarned) {

        public static RowClearResult none() {
            return new RowClearResult(0, 0);
        }
    }

    public boolean isValidPosition(Tetromino piece, int rowOffset, int colOffset, int[][] cells) {
        for (int[] cell : cells) {
            int r = piece.getRow() + cell[0] + rowOffset;
            int c = piece.getCol() + cell[1] + colOffset;
            if (c < 0 || c >= width || r >= height) {
                return false;
            }
            if (r >= 0 && grid[r][c] != null) {
                return false;
            }
        }
        return true;
    }

    public void lockPiece(Tetromino piece) {
        for (int[] cell : piece.getCurrentCells()) {
            int r = piece.getRow() + cell[0];
            int c = piece.getCol() + cell[1];
            if (r >= 0 && r < height && c >= 0 && c < width) {
                grid[r][c] = piece.getType().getColor();
            }
        }
    }

    /**
     * Enhanced for-loop usage (Milestone 1 rubric requirement) to scan the
     * board and find/clear every completed row.
     */
    public RowClearResult clearFullRows() {
        List<Integer> fullRows = new ArrayList<>();

        for (int r = 0; r < height; r++) {
            boolean full = true;
            for (Color cell : grid[r]) {
                if (cell == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                fullRows.add(r);
            }
        }

        for (int rowToClear : fullRows) {
            for (int r = rowToClear; r > 0; r--) {
                grid[r] = grid[r - 1].clone();
            }
            grid[0] = new Color[width];
        }

        int cleared = fullRows.size();
        if (cleared == 0) {
            return RowClearResult.none();
        }
        int points = switch (cleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            default -> 800;
        };
        return new RowClearResult(cleared, points);
    }

    public Color[][] getGrid() {
        return grid;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void reset() {
        for (Color[] row : grid) {
            java.util.Arrays.fill(row, null);
        }
    }
}
