package org.oosd.model;

public class LPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{0, 2}, {1, 0}, {1, 1}, {1, 2}},
            {{0, 1}, {1, 1}, {2, 1}, {2, 2}},
            {{1, 0}, {1, 1}, {1, 2}, {2, 0}},
            {{0, 0}, {0, 1}, {1, 1}, {2, 1}}
    };
    public LPiece(int startRow, int startCol) { super(TetrominoType.L, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
