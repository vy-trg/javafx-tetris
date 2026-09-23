package org.oosd.controller;

/**
 * Game is frozen: falling stops, movement/rotation input is ignored, and P
 * resumes back to PlayingState. Nothing about the board or score changes
 * while in this state.
 */
public class PausedState implements GameState {

    public static final PausedState INSTANCE = new PausedState();

    private PausedState() {
    }

    @Override
    public void tick(GameController controller, long now) {
        // intentionally does nothing - this is the whole point of Paused
    }

    @Override
    public void handleMoveLeft(GameController controller) {
        // ignored while paused
    }

    @Override
    public void handleMoveRight(GameController controller) {
        // ignored while paused
    }

    @Override
    public void handleSoftDrop(GameController controller) {
        // ignored while paused
    }

    @Override
    public void handleRotate(GameController controller) {
        // ignored while paused
    }

    @Override
    public void handlePauseKey(GameController controller) {
        controller.setState(PlayingState.INSTANCE);
    }

    @Override
    public boolean isPaused() {
        return true;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }
}
