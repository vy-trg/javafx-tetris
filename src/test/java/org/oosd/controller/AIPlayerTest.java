package org.oosd.controller;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.oosd.model.TetrominoType;

import static org.junit.jupiter.api.Assertions.*;

class AIPlayerTest {

    // Stub sequence: always hands out the same piece, so the test is deterministic.
    private static class FixedSequence extends TetrominoSequence {
        private final TetrominoType type;

        FixedSequence(TetrominoType type) {
            this.type = type;
        }

        @Override
        public synchronized TetrominoType get(int index) {
            return type;
        }
    }

    // Stub sequence: cycles through all seven pieces in a fixed order.
    private static class CyclingSequence extends TetrominoSequence {
        @Override
        public synchronized TetrominoType get(int index) {
            TetrominoType[] all = TetrominoType.values();
            return all[index % all.length];
        }
    }

    @Test
    void dropsIPieceIntoTheWell_toClearFourLines() {
        GameController controller = new GameController(10, 20, new FixedSequence(TetrominoType.I));
        controller.start();

        // Bottom four rows are full except the right-most column (a "well").
        Color[][] grid = controller.getBoard().getGrid();
        for (int row = 16; row < 20; row++) {
            for (int col = 0; col < 9; col++) {
                grid[row][col] = Color.GRAY;
            }
        }

        AIPlayer ai = new AIPlayer();
        for (int step = 0; step < 100 && controller.getLinesCleared() == 0; step++) {
            ai.takeTurn(controller);
        }

        assertEquals(4, controller.getLinesCleared());
        assertEquals(1000, controller.getScore());
        for (Color[] row : grid) {
            for (Color cell : row) {
                assertNull(cell, "board should be empty after the four-line clear");
            }
        }
    }

    @Test
    void survivesOneHundredPieces_onStandardBoard() {
        GameController controller = new GameController(10, 20, new CyclingSequence());
        int[] piecesLocked = {0};
        controller.setOnLock(() -> piecesLocked[0]++);
        controller.start();

        AIPlayer ai = new AIPlayer();
        for (int step = 0; step < 20_000 && piecesLocked[0] < 100 && !controller.isGameOver(); step++) {
            ai.takeTurn(controller);
        }

        assertFalse(controller.isGameOver());
        assertEquals(100, piecesLocked[0]);
        assertTrue(controller.getLinesCleared() >= 25,
                "expected the AI to keep clearing lines, but it cleared " + controller.getLinesCleared());
    }
}
