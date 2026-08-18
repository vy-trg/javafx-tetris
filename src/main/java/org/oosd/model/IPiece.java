package org.oosd.model;

public class IPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{1, 0}, {1, 1}, {1, 2}, {1, 3}},
            {{0, 2}, {1, 2}, {2, 2}, {3, 2}},
            {{2, 0}, {2, 1}, {2, 2}, {2, 3}},
            {{0, 1}, {1, 1}, {2, 1}, {3, 1}}
    };
    public IPiece(int startRow, int startCol) { super(TetrominoType.I, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
