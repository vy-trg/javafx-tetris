package org.oosd.model;

import java.util.Random;

/**
 * Enhanced switch EXPRESSION usage (Milestone 1 rubric requirement).
 */
public class TetrominoFactory {

    private static final Random RANDOM = new Random();

    public static Tetromino createRandom(int startCol) {
        TetrominoType[] values = TetrominoType.values();
        TetrominoType type = values[RANDOM.nextInt(values.length)];
        return create(type, startCol);
    }

    public static Tetromino create(TetrominoType type, int startCol) {
        return switch (type) {
            case I -> new IPiece(0, startCol);
            case J -> new JPiece(0, startCol);
            case L -> new LPiece(0, startCol);
            case O -> new OPiece(0, startCol);
            case S -> new SPiece(0, startCol);
            case T -> new TPiece(0, startCol);
            case Z -> new ZPiece(0, startCol);
        };
    }
}
