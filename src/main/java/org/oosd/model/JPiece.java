package org.oosd.model;

public class JPiece extends Tetromino {
    private static final int[][][] STATES = {
            {{0, 0}, {1, 0}, {1, 1}, {1, 2}},
            {{0, 1}, {0, 2}, {1, 1}, {2, 1}},
            {{1, 0}, {1, 1}, {1, 2}, {2, 2}},
            {{0, 1}, {1, 1}, {2, 0}, {2, 1}}
    };
    public JPiece(int startRow, int startCol) { super(TetrominoType.J, startRow, startCol); }
    @Override protected int[][][] getRotationStates() { return STATES; }
}
