package org.oosd;

import org.oosd.audio.SoundManager;
import org.oosd.controller.GameController;
import org.oosd.model.Board;
import org.oosd.model.Tetromino;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

// Game Play screen (rubric items 15, 16, 17) - View only. All the game
// rules/state now live in GameController; this class just draws whatever
// it reports and turns key presses / timer ticks into calls on it.
public class GameScreen {

    private static final int CELL_SIZE = 24;

    private final Runnable onBackToMenu;
    private final GameController controller;
    private final int gridWidth;
    private final int gridHeight;

    private final StackPane root;
    private final Pane boardPane;
    private final Rectangle[][] cellRects;
    private final Label scoreLabel;
    private final Label aiStatusLabel;
    private final Label pausedLabel;
    private final Label gameOverLabel;

    private AnimationTimer gameLoop;

    public GameScreen(Runnable onBackToMenu) {
        this(onBackToMenu, Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT);
    }

    // gridWidth/gridHeight come from the Configuration Screen spinners
    // (5-15 x 15-30) - everything below is built from these instead of a
    // fixed 10x20 size.
    public GameScreen(Runnable onBackToMenu, int gridWidth, int gridHeight) {
        this.onBackToMenu = onBackToMenu;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.controller = new GameController(gridWidth, gridHeight);
        this.cellRects = new Rectangle[gridHeight][gridWidth];

        controller.setOnGameOver(this::onGameOver);
        controller.setOnMove(() -> SoundManager.getInstance().playEffect(SoundManager.Sound.MOVE));
        controller.setOnRotate(() -> SoundManager.getInstance().playEffect(SoundManager.Sound.ROTATE));
        controller.setOnLock(() -> SoundManager.getInstance().playEffect(SoundManager.Sound.LOCK));
        controller.setOnLinesCleared(rows -> SoundManager.getInstance().playEffect(SoundManager.Sound.LINE_CLEAR));

        boardPane = new Pane();
        boardPane.setPrefSize(gridWidth * CELL_SIZE, gridHeight * CELL_SIZE);
        boardPane.setMaxSize(gridWidth * CELL_SIZE, gridHeight * CELL_SIZE);
        boardPane.setStyle("-fx-background-color: #111111;");

        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                Rectangle cell = new Rectangle(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE - 1, CELL_SIZE - 1);
                cell.setFill(Color.web("#111111"));
                cell.setStroke(Color.web("#333333"));
                cellRects[r][c] = cell;
                boardPane.getChildren().add(cell);
            }
        }

        scoreLabel = new Label("Score: 0");
        scoreLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        aiStatusLabel = new Label("AI PLAY - press A to stop");
        aiStatusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #33ccff; -fx-font-weight: bold;");
        aiStatusLabel.setVisible(false);

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

        VBox scoreBox = new VBox(2, scoreLabel, aiStatusLabel);

        BorderPane top = new BorderPane();
        top.setLeft(scoreBox);
        top.setRight(backButton);
        BorderPane.setMargin(scoreBox, new Insets(8));
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
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    public void startGame() {
        controller.start();
        gameOverLabel.setVisible(false);
        pausedLabel.setVisible(false);
        aiStatusLabel.setVisible(controller.isAiEnabled());

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                controller.tick(now);
                draw();
            }
        };
        gameLoop.start();
        SoundManager.getInstance().playMusic();

        draw();
        root.requestFocus();
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        SoundManager.getInstance().stopMusic();
    }

    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case P -> {
                controller.handlePauseKey();
                if (controller.isPaused()) {
                    SoundManager.getInstance().playEffect(SoundManager.Sound.PAUSE);
                    SoundManager.getInstance().pauseMusic();
                } else {
                    SoundManager.getInstance().resumeMusic();
                }
            }
            case M -> {
                SoundManager soundManager = SoundManager.getInstance();
                boolean musicEnabled = !soundManager.isMusicEnabled();
                soundManager.setMusicEnabled(musicEnabled);
                // Do not play music while the game is paused.
                if (musicEnabled && controller.isPaused()) {
                    soundManager.pauseMusic();
                }
            }
            case S -> {
                SoundManager soundManager = SoundManager.getInstance();
                soundManager.setSfxEnabled(!soundManager.isSfxEnabled());
            }
            case LEFT -> controller.handleMoveLeft();
            case RIGHT -> controller.handleMoveRight();
            case DOWN -> controller.handleSoftDrop();
            case UP -> controller.handleRotate();
            case A -> {
                controller.setAiEnabled(!controller.isAiEnabled());
                aiStatusLabel.setVisible(controller.isAiEnabled());
            }
            default -> { /* ignore */ }
        }
        pausedLabel.setVisible(controller.isPaused());
        draw();
    }

    private void onGameOver() {
        gameOverLabel.setVisible(true);
        SoundManager.getInstance().playEffect(SoundManager.Sound.GAME_OVER);
        stopGame();

        // showAndWait() isn't allowed while the AnimationTimer's pulse is
        // still running - this callback fires from inside that pulse, so
        // the dialog has to wait for the next JavaFX event loop turn.
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("Player");
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Final score: " + controller.getScore());
            dialog.setContentText("Enter your name for the high score list:");

            Optional<String> name = dialog.showAndWait();
            name.ifPresent(playerName -> recordHighScore(playerName, controller.getScore()));
        });
    }

    // TODO(Tasmia): wire this to real high-score persistence once it exists
    private void recordHighScore(String playerName, int finalScore) {
        System.out.println("[TODO high score persistence] " + playerName + " scored " + finalScore);
    }

    private void handleBack() {
        boolean wasPlaying = !controller.isPaused() && !controller.isGameOver();
        if (wasPlaying) {
            controller.handlePauseKey(); // freeze gameplay while the dialog is open
            SoundManager.getInstance().pauseMusic();
        }

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
        } else if (wasPlaying) {
            controller.handlePauseKey(); // FR15: cancel - resume where we left off
            SoundManager.getInstance().resumeMusic();
        }
        pausedLabel.setVisible(controller.isPaused());
    }

    private void draw() {
        Color[][] grid = controller.getBoard().getGrid();
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                Color locked = grid[r][c];
                cellRects[r][c].setFill(locked != null ? locked : Color.web("#111111"));
            }
        }

        Tetromino currentPiece = controller.getCurrentPiece();
        if (currentPiece != null) {
            for (int[] cell : currentPiece.getCurrentCells()) {
                int r = currentPiece.getRow() + cell[0];
                int c = currentPiece.getCol() + cell[1];
                if (r >= 0 && r < gridHeight && c >= 0 && c < gridWidth) {
                    cellRects[r][c].setFill(currentPiece.getType().getColor());
                }
            }
        }

        scoreLabel.setText("Score: " + controller.getScore());
    }

    public StackPane getRoot() {
        return root;
    }
}
