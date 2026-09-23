package org.oosd.controller;

import org.oosd.model.TetrominoType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Shared piece order for Two-Player Mode: both GameControllers hold the same
// instance and pull by index, so piece #1, #2, #3... always match for both
// players even if one is spawning pieces faster than the other.
public class TetrominoSequence {

    private final Random random = new Random();
    private final List<TetrominoType> generated = new ArrayList<>();

    public synchronized TetrominoType get(int index) {
        TetrominoType[] values = TetrominoType.values();
        while (generated.size() <= index) {
            generated.add(values[random.nextInt(values.length)]);
        }
        return generated.get(index);
    }
}
