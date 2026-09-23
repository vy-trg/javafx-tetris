package org.oosd.controller;

/**
 * The normal, active phase of the game: pieces fall, input moves/rotates
 * the current piece, and P pauses.
 */
public class PlayingState implements GameState {

    public static final PlayingState INSTANCE = new PlayingState();

    private PlayingState() {
    }

    @Override
    public void tick(GameController controller, long now) {
        controller.advanceFallIfDue(now);
    }

    @Override
    public void handleMoveLeft(GameController controller) {
        controller.tryMove(-1);
    }

    @Override
    public void handleMoveRight(GameController controller) {
        controller.tryMove(1);
    }

    @Override
    public void handleSoftDrop(GameController controller) {
        controller.moveDown();
    }

    @Override
    public void handleRotate(GameController controller) {
        controller.tryRotate();
    }

    @Override
    public void handlePauseKey(GameController controller) {
        controller.setState(PausedState.INSTANCE);
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }
}
