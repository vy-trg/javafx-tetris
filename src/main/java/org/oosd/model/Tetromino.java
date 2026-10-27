package org.oosd.model;

/**
 * Holds everything common to every falling piece (position, rotation state,
 * type/color). Each concrete shape only supplies its own 4 rotation states.
 */
public abstract class Tetromino implements Rotatable {

    protected int row;
    protected int col;
    protected int rotationState;
    protected final TetrominoType type;

    protected Tetromino(TetrominoType type, int startRow, int startCol) {
        this.type = type;
        this.row = startRow;
        this.col = startCol;
        this.rotationState = 0;
    }

    protected abstract int[][][] getRotationStates();

    @Override
    public int[][] getCurrentCells() {
        return getRotationStates()[rotationState];
    }

    @Override
    public int[][] rotateClockwise() {
        int nextState = (rotationState + 1) % 4;
        return getRotationStates()[nextState];
    }

    public void applyRotation() {
        this.rotationState = (this.rotationState + 1) % 4;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getRotationState() {
        return rotationState;
    }

    public void moveDown() {
        row++;
    }

    public void moveLeft() {
        col--;
    }

    public void moveRight() {
        col++;
    }

    public TetrominoType getType() {
        return type;
    }
}
