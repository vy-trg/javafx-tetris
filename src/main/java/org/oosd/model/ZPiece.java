package org.oosd.model;

public class ZPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{0, 0}, {0, 1}, {1, 1}, {1, 2}},
            {{0, 2}, {1, 1}, {1, 2}, {2, 1}},
            {{0, 0}, {0, 1}, {1, 1}, {1, 2}},
            {{0, 2}, {1, 1}, {1, 2}, {2, 1}}
    };
    public ZPiece(int startRow, int startCol) { super(TetrominoType.Z, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
