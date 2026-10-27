package org.oosd.controller;

/**
 * Advanced Pattern 1 - State.
 *
 * Instead of GameController checking booleans like "if (paused) ... else if
 * (gameOver) ..." everywhere, each phase of a game (Playing, Paused,
 * GameOver) is its own class. GameController just holds a reference to the
 * "current" GameState and delegates every input/tick to it - the state
 * itself decides what happens (or doesn't) and whether to switch the
 * controller to a different state.
 *
 * This is the classic State pattern:
 * a context (GameController) holds a State, and delegates behaviour to it
 * rather than branching on flags.
 */
public interface GameState {

    /** Called once per game-loop tick. Only PlayingState actually moves anything. */
    void tick(GameController controller, long now);

    void handleMoveLeft(GameController controller);

    void handleMoveRight(GameController controller);

    void handleSoftDrop(GameController controller);

    void handleRotate(GameController controller);

    /** The P key. Playing <-> Paused toggle; no-op once the game is over. */
    void handlePauseKey(GameController controller);

    boolean isPaused();

    boolean isGameOver();
}
