package org.oosd.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void defaultConstructor_usesDefaultDimensions() {
        Board board = new Board();
        assertEquals(Board.DEFAULT_WIDTH, board.getWidth());
        assertEquals(Board.DEFAULT_HEIGHT, board.getHeight());
    }

    @Test
    void customConstructor_supportsDynamicGridSize() {
        Board board = new Board(5, 15);
        assertEquals(5, board.getWidth());
        assertEquals(15, board.getHeight());
    }

    @Test
    void clearFullRows_returnsNone_whenBoardIsEmpty() {
        Board board = new Board(5, 15);
        Board.RowClearResult result = board.clearFullRows();
        assertEquals(0, result.rowsCleared());
        assertEquals(0, result.pointsEarned());
    }

    @Test
    void clearFullRows_clearsOneFullRow_andAwards100Points() {
        Board board = new Board(5, 15);
        Color[][] grid = board.getGrid();
        int lastRow = board.getHeight() - 1;
        for (int c = 0; c < board.getWidth(); c++) {
            grid[lastRow][c] = Color.RED;
        }

        Board.RowClearResult result = board.clearFullRows();

        assertEquals(1, result.rowsCleared());
        assertEquals(100, result.pointsEarned());
        for (Color cell : board.getGrid()[0]) {
            assertNull(cell);
        }
    }

    @Test
    void reset_clearsEveryCellOnTheBoard() {
        Board board = new Board(5, 15);
        board.getGrid()[0][0] = Color.BLUE;

        board.reset();

        for (Color[] row : board.getGrid()) {
            for (Color cell : row) {
                assertNull(cell);
            }
        }
    }
}