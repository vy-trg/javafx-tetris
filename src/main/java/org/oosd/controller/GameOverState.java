package org.oosd.controller;

/**
 * Terminal state: no more falling, no more input accepted. GameController
 * fires its onGameOver callback the moment it switches into this state (see
 * GameController.setState), which is what GameScreen uses to pop the
 * name-entry dialog for the high score screen.
 */
public class GameOverState implements GameState {

    public static final GameOverState INSTANCE = new GameOverState();

    private GameOverState() {
    }

    @Override
    public void tick(GameController controller, long now) {
        // game is over, nothing left to advance
    }

    @Override
    public void handleMoveLeft(GameController controller) {
        // ignored - game over
    }

    @Override
    public void handleMoveRight(GameController controller) {
        // ignored - game over
    }

    @Override
    public void handleSoftDrop(GameController controller) {
        // ignored - game over
    }

    @Override
    public void handleRotate(GameController controller) {
        // ignored - game over
    }

    @Override
    public void handlePauseKey(GameController controller) {
        // pausing a finished game makes no sense - ignored
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isGameOver() {
        return true;
    }
}
