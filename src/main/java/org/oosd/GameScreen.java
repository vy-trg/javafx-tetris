package org.oosd;

import org.oosd.model.Board;
import org.oosd.model.Tetromino;
import org.oosd.model.TetrominoFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Game Play screen (rubric items 15, 16, 17):
 * - 10x20 field, smooth row-by-row falling (never jumps a row).
 * - Arrow keys move/rotate the falling piece.
 * - Full rows are detected and erased, including multi-row clears.
 * - 'P' pauses/resumes with a visible message.
 * - Back button shows a Yes/No confirmation before returning to the menu (FR14-16).
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

    private final Runnable onBackToMenu;

    private final StackPane root;
    private final Canvas canvas;
    private final Label scoreLabel;
    private final Label pausedLabel;

    private final Board board = new Board();
    private Tetromino currentPiece;

    private Timeline fallTimeline;
    private boolean paused = false;
    private boolean gameOver = false;
    private int score = 0;

    public GameScreen(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        canvas = new Canvas(Board.WIDTH * CELL_SIZE, Board.HEIGHT * CELL_SIZE);

        scoreLabel = new Label("Score: 0");
        pausedLabel = new Label("PAUSED - press P to resume");
        pausedLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffcc00; "
                + "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 16px;");
        pausedLabel.setVisible(false);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleBack());

        BorderPane top = new BorderPane();
        top.setLeft(scoreLabel);
        top.setRight(backButton);
        BorderPane.setMargin(scoreLabel, new Insets(8));
        BorderPane.setMargin(backButton, new Insets(8));

        BorderPane layout = new BorderPane();
        layout.setTop(top);
        layout.setCenter(canvas);

        root = new StackPane(layout, pausedLabel);
        StackPane.setAlignment(pausedLabel, Pos.CENTER);
        root.setFocusTraversable(true);
        root.setOnKeyPressed(this::handleKeyPress);
    }

    public void startGame() {
        board.reset();
        score = 0;
        gameOver = false;
        scoreLabel.setText("Score: 0");
        spawnNewPiece();

        fallTimeline = new Timeline(new KeyFrame(Duration.millis(600), e -> tick()));
        fallTimeline.setCycleCount(Timeline.INDEFINITE);
        fallTimeline.play();

        draw();
        root.requestFocus();
    }

    public void stopGame() {
        if (fallTimeline != null) {
            fallTimeline.stop();
        }
    }

    private void spawnNewPiece() {
        currentPiece = TetrominoFactory.createRandom(Board.WIDTH / 2 - 2);
        if (!board.isValidPosition(currentPiece, 0, 0, currentPiece.getCurrentCells())) {
            gameOver = true;
            fallTimeline.stop();
        }
    }

    private void tick() {
        if (paused || gameOver) {
            return;
        }
        moveDown();
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

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#111111"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Color[][] grid = board.getGrid();
        for (int r = 0; r < Board.HEIGHT; r++) {
            for (int c = 0; c < Board.WIDTH; c++) {
                if (grid[r][c] != null) {
                    drawCell(gc, r, c, grid[r][c]);
                }
            }
        }

        if (currentPiece != null) {
            for (int[] cell : currentPiece.getCurrentCells()) {
                int r = currentPiece.getRow() + cell[0];
                int c = currentPiece.getCol() + cell[1];
                if (r >= 0) {
                    drawCell(gc, r, c, currentPiece.getType().getColor());
                }
            }
        }

        gc.setStroke(Color.web("#333333"));
        for (int r = 0; r <= Board.HEIGHT; r++) {
            gc.strokeLine(0, r * CELL_SIZE, Board.WIDTH * CELL_SIZE, r * CELL_SIZE);
        }
        for (int c = 0; c <= Board.WIDTH; c++) {
            gc.strokeLine(c * CELL_SIZE, 0, c * CELL_SIZE, Board.HEIGHT * CELL_SIZE);
        }

        if (gameOver) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(20));
            gc.fillText("GAME OVER", canvas.getWidth() / 2 - 55, canvas.getHeight() / 2);
        }
    }

    private void drawCell(GraphicsContext gc, int row, int col, Color color) {
        gc.setFill(color);
        gc.fillRect(col * CELL_SIZE + 1, row * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
    }

    public StackPane getRoot() {
        return root;
    }
}
