package org.oosd.model;

public class TPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{0, 1}, {1, 0}, {1, 1}, {1, 2}},
            {{0, 1}, {1, 1}, {1, 2}, {2, 1}},
            {{1, 0}, {1, 1}, {1, 2}, {2, 1}},
            {{0, 1}, {1, 0}, {1, 1}, {2, 1}}
    };
    public TPiece(int startRow, int startCol) { super(TetrominoType.T, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
