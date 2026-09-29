package org.oosd.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameControllerAdvancedTest {

    // --- Parameterized test: same check run across several Dynamic Grid sizes ---
    @ParameterizedTest
    @CsvSource({"5,15", "10,20", "15,30"})
    void start_appliesRequestedDynamicGridSize(int width, int height) {
        GameController controller = new GameController(width, height);
        controller.start();

        assertEquals(width, controller.getBoard().getWidth());
        assertEquals(height, controller.getBoard().getHeight());
    }

    // --- Test double (hand-written stub) ---
    private static class RecordingCallback implements Runnable {
        int callCount = 0;

        @Override
        public void run() {
            callCount++;
        }
    }

    @Test
    void start_onImpossibleBoard_notifiesGameOverViaStub() {
        GameController controller = new GameController(1, 1); // no piece can ever fit
        RecordingCallback stub = new RecordingCallback();
        controller.setOnGameOver(stub);

        controller.start();

        assertTrue(controller.isGameOver());
        assertEquals(1, stub.callCount);
    }

    // --- Mockito mock ---
    @Test
    void start_onImpossibleBoard_notifiesGameOverViaMock() {
        GameController controller = new GameController(1, 1);
        Runnable mockCallback = mock(Runnable.class);
        controller.setOnGameOver(mockCallback);

        controller.start();

        verify(mockCallback, times(1)).run();
    }
}