package org.oosd.model;

public class OPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{1, 1}, {1, 2}, {2, 1}, {2, 2}},
            {{1, 1}, {1, 2}, {2, 1}, {2, 2}},
            {{1, 1}, {1, 2}, {2, 1}, {2, 2}},
            {{1, 1}, {1, 2}, {2, 1}, {2, 2}}
    };
    public OPiece(int startRow, int startCol) { super(TetrominoType.O, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
