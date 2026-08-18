package org.oosd.model;

import javafx.scene.paint.Color;

public enum TetrominoType {
    I(Color.CYAN),
    J(Color.web("#3355FF")),
    L(Color.web("#FF9933")),
    O(Color.YELLOW),
    S(Color.LIMEGREEN),
    T(Color.web("#AA33FF")),
    Z(Color.web("#FF3344"));

    private final Color color;

    TetrominoType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
