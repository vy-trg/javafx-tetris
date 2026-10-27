package org.oosd.controller;

import org.junit.jupiter.api.Test;
import org.oosd.model.TetrominoType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TetrominoSequenceTest {

    @Test
    void sameIndex_alwaysReturnsSamePiece() {
        TetrominoSequence sequence = new TetrominoSequence();

        List<TetrominoType> firstRead = IntStream.range(0, 50).mapToObj(sequence::get).toList();
        List<TetrominoType> secondRead = IntStream.range(0, 50).mapToObj(sequence::get).toList();

        assertEquals(firstRead, secondRead);
    }

    @Test
    void twoPlayersSharingASequence_receiveIdenticalPieces() {
        TetrominoSequence shared = new TetrominoSequence();
        GameController player1 = new GameController(10, 20, shared);
        GameController player2 = new GameController(10, 20, shared);
        player1.start();
        player2.start();

        // Player 1 races ahead; player 2 plays later and must still get the same order.
        List<TetrominoType> player1Pieces = dropPieces(player1, 5);
        List<TetrominoType> player2Pieces = dropPieces(player2, 5);

        assertEquals(player1Pieces, player2Pieces);
    }

    @Test
    void concurrentReaders_allSeeTheSameOrder() throws Exception {
        TetrominoSequence sequence = new TetrominoSequence();
        int threads = 8;
        int pieces = 1_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<List<TetrominoType>>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(pool.submit(() -> IntStream.range(0, pieces).mapToObj(sequence::get).toList()));
            }

            List<TetrominoType> expected = results.get(0).get();
            assertEquals(pieces, expected.size());
            for (Future<List<TetrominoType>> result : results) {
                assertEquals(expected, result.get());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    // Records each spawned piece type, then soft-drops it until it locks.
    private static List<TetrominoType> dropPieces(GameController controller, int count) {
        int[] locks = {0};
        controller.setOnLock(() -> locks[0]++);
        List<TetrominoType> types = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            types.add(controller.getCurrentPiece().getType());
            int target = locks[0] + 1;
            while (locks[0] < target) {
                controller.handleSoftDrop();
            }
        }
        return types;
    }
}
