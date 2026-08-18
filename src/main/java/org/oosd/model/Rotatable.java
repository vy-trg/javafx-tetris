package org.oosd.model;

/**
 * Interface usage (Milestone 1 rubric requirement).
 * Contract for anything on the board that can be rotated. Tetromino
 * implements this so GameScreen can rotate the falling piece without
 * knowing which of the 7 concrete shapes it currently holds.
 */
public interface Rotatable {

    int[][] rotateClockwise();

    int[][] getCurrentCells();
}
