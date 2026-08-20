package org.oosd;

import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoFactory;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

/**
 * Game Play screen (rubric items 15, 16, 17).
 * Uses the same building blocks taught in lecture (JavaFX Animation and
 * Screen Navigation): a Pane of Rectangle shapes for the board grid, and
 * an AnimationTimer as the game loop - instead of Canvas/GraphicsContext
 * or Timeline, which were not covered in class.
 *
 * Wiring into Main.java's showGameScreen():
 *
 *   private void showGameScreen() {
 *       GameScreen gameScreen = new GameScreen(this::showMainScreen);
 *       root.getChildren().setAll(gameScreen.getRoot());
 *       gameScreen.getRoot().requestFocus();
 *       gameScreen.startGame();
 *   }
 */
public class GameScreen {

    private static final int CELL_SIZE = 24;
    private static final long FALL_INTERVAL_NANOS = 600_000_000L; // 600ms per row, as taught with AnimationTimer's handle(long now)

    private final Runnable onBackToMenu;

    private final StackPane root;
    private final Pane boardPane;
    private final Rectangle[][] cellRects = new Rectangle[Board.HEIGHT][Board.WIDTH];
    private final Label scoreLabel;
    private final Label pausedLabel;
    private final Label gameOverLabel;

    private final Board board = new Board();
    private Tetromino currentPiece;

    private AnimationTimer gameLoop;
    private long lastFallTime = 0;
    private boolean paused = false;
    private boolean gameOver = false;
    private int score = 0;

    public GameScreen(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        // Pane + Rectangle grid, exactly as shown in the "Pane – Free
        // placement container" / "Shape – Drawing graphics" lecture example.
        boardPane = new Pane();
        boardPane.setPrefSize(Board.WIDTH * CELL_SIZE, Board.HEIGHT * CELL_SIZE);
        boardPane.setStyle("-fx-background-color: #111111;");
        boardPane.setMaxSize(Board.WIDTH * CELL_SIZE, Board.HEIGHT * CELL_SIZE);

        for (int r = 0; r < Board.HEIGHT; r++) {
            for (int c = 0; c < Board.WIDTH; c++) {
                Rectangle cell = new Rectangle(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                cell.setFill(Color.web("#111111"));
                cell.setStroke(Color.web("#333333"));
                cellRects[r][c] = cell;
                boardPane.getChildren().add(cell);
            }
        }

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        pausedLabel = new Label("PAUSED - press P to resume");
        pausedLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffcc00; "
                + "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 16px;");
        pausedLabel.setVisible(false);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: white; "
                + "-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20px;");
        gameOverLabel.setVisible(false);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleBack());

        BorderPane top = new BorderPane();
        top.setLeft(scoreLabel);
        top.setRight(backButton);
        BorderPane.setMargin(scoreLabel, new Insets(8));
        BorderPane.setMargin(backButton, new Insets(8));

        BorderPane layout = new BorderPane();
        layout.setTop(top);
        layout.setCenter(boardPane);
        BorderPane.setAlignment(boardPane, Pos.CENTER);

        root = new StackPane(layout, pausedLabel, gameOverLabel);
        root.setStyle(
          "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );
        StackPane.setAlignment(pausedLabel, Pos.CENTER);
        StackPane.setAlignment(gameOverLabel, Pos.CENTER);
        root.setFocusTraversable(true);
        // Keystroke handling exactly as taught: scene/node.setOnKeyPressed(e -> switch(e.getCode()) ...)
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    public void startGame() {
        board.reset();
        score = 0;
        gameOver = false;
        gameOverLabel.setVisible(false);
        scoreLabel.setText("Score: 0");
        spawnNewPiece();

        // AnimationTimer - "JavaFX's simplest way to create an animation or
        // game loop", as taught. handle(now) is called ~60 times a second;
        // we only act once enough time has passed for one row-step.
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFallTime == 0) {
                    lastFallTime = now;
                    return;
                }
                if (!paused && !gameOver && now - lastFallTime >= FALL_INTERVAL_NANOS) {
                    moveDown();
                    lastFallTime = now;
                }
            }
        };
        gameLoop.start();

        draw();
        root.requestFocus();
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    private void spawnNewPiece() {
        currentPiece = TetrominoFactory.createRandom(Board.WIDTH / 2 - 2);
        if (!board.isValidPosition(currentPiece, 0, 0, currentPiece.getCurrentCells())) {
            gameOver = true;
            gameOverLabel.setVisible(true);
            stopGame();
        }
    }

    private void moveDown() {
        if (board.isValidPosition(currentPiece, 1, 0, currentPiece.getCurrentCells())) {
            currentPiece.moveDown();
        } else {
            lockAndClear();
        }
        draw();
    }

    private void lockAndClear() {
        board.lockPiece(currentPiece);
        Board.RowClearResult result = board.clearFullRows();
        if (result.rowsCleared() > 0) {
            score += result.pointsEarned();
            scoreLabel.setText("Score: " + score);
        }
        spawnNewPiece();
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.P) {
            togglePause();
            return;
        }
        if (paused || gameOver) {
            return;
        }
        switch (event.getCode()) {
            case LEFT -> tryMove(-1);
            case RIGHT -> tryMove(1);
            case DOWN -> moveDown();
            case UP -> tryRotate();
            default -> { /* ignore */ }
        }
    }

    private void tryMove(int colDelta) {
        if (board.isValidPosition(currentPiece, 0, colDelta, currentPiece.getCurrentCells())) {
            if (colDelta < 0) {
                currentPiece.moveLeft();
            } else {
                currentPiece.moveRight();
            }
            draw();
        }
    }

    private void tryRotate() {
        int[][] rotated = currentPiece.rotateClockwise();
        if (board.isValidPosition(currentPiece, 0, 0, rotated)) {
            currentPiece.applyRotation();
            draw();
        }
    }

    private void togglePause() {
        paused = !paused;
        pausedLabel.setVisible(paused);
    }

    /**
     * FR14-16: confirm before stopping the current game.
     */
    private void handleBack() {
        boolean wasPaused = paused;
        paused = true; // freeze gameplay while the dialog is open

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Stop Game");
        confirm.setHeaderText("Are you sure you want to stop the current game?");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        confirm.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            stopGame();
            onBackToMenu.run();
        } else {
            paused = wasPaused; // FR15: cancel - game continues as before
        }
    }

    /**
     * Redraws the board by updating each Rectangle's fill color - the same
     * "Shape" building block used throughout the lecture, just reused as a
     * grid instead of a single ball.
     */
    private void draw() {
        Color[][] grid = board.getGrid();
        for (int r = 0; r < Board.HEIGHT; r++) {
            for (int c = 0; c < Board.WIDTH; c++) {
                Color locked = grid[r][c];
                cellRects[r][c].setFill(locked != null ? locked : Color.web("#111111"));
            }
        }

        if (currentPiece != null) {
            for (int[] cell : currentPiece.getCurrentCells()) {
                int r = currentPiece.getRow() + cell[0];
                int c = currentPiece.getCol() + cell[1];
                if (r >= 0 && r < Board.HEIGHT && c >= 0 && c < Board.WIDTH) {
                    cellRects[r][c].setFill(currentPiece.getType().getColor());
                }
            }
        }
    }

    public StackPane getRoot() {
        return root;
    }
}

