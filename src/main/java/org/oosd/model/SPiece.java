package org.oosd.model;

public class SPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{0, 1}, {0, 2}, {1, 0}, {1, 1}},
            {{0, 1}, {1, 1}, {1, 2}, {2, 2}},
            {{0, 1}, {0, 2}, {1, 0}, {1, 1}},
            {{0, 1}, {1, 1}, {1, 2}, {2, 2}}
    };
    public SPiece(int startRow, int startCol) { super(TetrominoType.S, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
