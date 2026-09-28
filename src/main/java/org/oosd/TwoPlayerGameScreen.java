package org.oosd;

import org.oosd.audio.SoundManager;
import org.oosd.controller.ExternalPlayer;
import org.oosd.controller.GameController;
import org.oosd.controller.TetrominoSequence;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Optional;

// Two-Player Mode ("Extend Mode"). Two GameControllers/Boards run side by
// side, both pulling pieces from the same TetrominoSequence so they always
// get the same piece order. Controls: Player 1 = , . Space L, Player 2 =
// arrow keys, 1/2 toggle AI for each player, P pauses both.

//if only Player 1 is human, arrow keys drive Player 1 too. AI/External players ignore keys
public class TwoPlayerGameScreen {

    private static final int CELL_SIZE = 24;

    private final Runnable onBackToMenu;
    private final PlayerPanel player1;
    private final PlayerPanel player2;

    private final StackPane root;
    private final Label pausedLabel;

    private AnimationTimer gameLoop;
    private boolean matchEnded;
    private boolean externalFailureHandled;

    public TwoPlayerGameScreen(Runnable onBackToMenu) {
        this(onBackToMenu, Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT);
    }

    public TwoPlayerGameScreen(Runnable onBackToMenu, int gridWidth, int gridHeight) {
        this.onBackToMenu = onBackToMenu;

        TetrominoSequence sharedSequence = new TetrominoSequence();
        player1 = new PlayerPanel("Player 1", gridWidth, gridHeight, sharedSequence);
        player2 = new PlayerPanel("Player 2", gridWidth, gridHeight, sharedSequence);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleBack());

        Label title = new Label("TWO-PLAYER MODE");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");

        BorderPane top = new BorderPane();
        top.setLeft(title);
        top.setRight(backButton);
        BorderPane.setMargin(title, new Insets(8));
        BorderPane.setMargin(backButton, new Insets(8));

        HBox boards = new HBox(24, player1.root, player2.root);
        boards.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane();
        layout.setTop(top);
        layout.setCenter(boards);
        BorderPane.setAlignment(boards, Pos.CENTER);

        pausedLabel = new Label("PAUSED - press P to resume");
        pausedLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffcc00; "
                + "-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 16px;");
        pausedLabel.setVisible(false);

        root = new StackPane(layout, pausedLabel);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #111827, #020617);");
        StackPane.setAlignment(pausedLabel, Pos.CENTER);
        root.setFocusTraversable(true);
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }
    public TwoPlayerGameScreen(Runnable onBackToMenu, int gridWidth, int gridHeight,
                               int startingLevel,
                               ConfigurationManager.PlayerType player1Type,
                               ConfigurationManager.PlayerType player2Type) {
        this(onBackToMenu, gridWidth, gridHeight);
        player1.applyConfig(startingLevel, player1Type);
        player2.applyConfig(startingLevel, player2Type);
    }

    public void startGame() {
        matchEnded = false;
        externalFailureHandled = false;
        player1.controller.start();
        player2.controller.start();
        player1.reset();
        player2.reset();
        pausedLabel.setVisible(false);

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!player1.controller.isGameOver()) {
                    player1.controller.tick(now);
                }
                if (!player2.controller.isGameOver()) {
                    player2.controller.tick(now);
                }
                player1.draw();
                player2.draw();
                if (checkForExternalFailure()) {
                    return;
                }
                checkForMatchEnd();
            }
        };
        gameLoop.start();
        SoundManager.getInstance().playMusic();

        player1.draw();
        player2.draw();
        root.requestFocus();
    }

    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        SoundManager.getInstance().stopMusic();
        player1.controller.shutdownExternal();
        player2.controller.shutdownExternal();
    }

    //if either TetrisServer connection drops mid-game, end match, tell player, and go back to  menu.
    private boolean checkForExternalFailure() {
        if (externalFailureHandled || matchEnded) {
            return externalFailureHandled;
        }
        String reason = null;
        if (player1.controller.isExternalFailed()) {
            reason = "Player 1: " + player1.controller.getExternalFailureReason();
        } else if (player2.controller.isExternalFailed()) {
            reason = "Player 2: " + player2.controller.getExternalFailureReason();
        }
        if (reason == null) {
            return false;
        }
        externalFailureHandled = true;
        matchEnded = true;
        stopGame();
        String message = reason;
        //can't showAndWait() from inside animation pulse
        Platform.runLater(() -> {
            Alert error = new Alert(AlertType.ERROR);
            error.setTitle("External Player Disconnected");
            error.setHeaderText("TetrisServer connection lost");
            error.setContentText(message + "\n\nThe game has been stopped. Make sure TetrisServer.jar "
                    + "is running on " + ExternalPlayer.HOST + ":" + ExternalPlayer.PORT + " and try again.");
            error.showAndWait();
            onBackToMenu.run();
        });
        return true;
    }

    private void handleKeyPress(KeyEvent event) {
        //only human-controlled player reacts to keyboard
        //AI/external player driven by own logic, stray key presses don't nudge it
        boolean p1Human = player1.isHumanControlled();
        boolean p2Human = player2.isHumanControlled();
        //arrow keys are Player 2's keys, but when Player 2 is AI/External and
        //player 1 human, arrows drive Player 1 so human never stuck using , . Space or L keys
        PlayerPanel arrowTarget = (!p2Human && p1Human) ? player1 : player2;
        boolean arrowsActive = arrowTarget.isHumanControlled();

        switch (event.getCode()) {
            case COMMA -> { if (p1Human) player1.controller.handleMoveLeft(); }
            case PERIOD -> { if (p1Human) player1.controller.handleMoveRight(); }
            case SPACE -> { if (p1Human) player1.controller.handleSoftDrop(); }
            case L -> { if (p1Human) player1.controller.handleRotate(); }
            case LEFT -> { if (arrowsActive) arrowTarget.controller.handleMoveLeft(); }
            case RIGHT -> { if (arrowsActive) arrowTarget.controller.handleMoveRight(); }
            case DOWN -> { if (arrowsActive) arrowTarget.controller.handleSoftDrop(); }
            case UP -> { if (arrowsActive) arrowTarget.controller.handleRotate(); }
            case DIGIT1 -> player1.toggleAi();
            case DIGIT2 -> player2.toggleAi();
            case P -> {
                player1.controller.handlePauseKey();
                player2.controller.handlePauseKey();

                if (player1.controller.isPaused() || player2.controller.isPaused()) {
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
                if (musicEnabled && (player1.controller.isPaused() || player2.controller.isPaused())) {
                    soundManager.pauseMusic();
                }
            }
            case S -> {
                SoundManager soundManager = SoundManager.getInstance();
                soundManager.setSfxEnabled(!soundManager.isSfxEnabled());
            }
            default -> { /* ignore */ }
        }
        pausedLabel.setVisible(player1.controller.isPaused() || player2.controller.isPaused());
        player1.draw();
        player2.draw();
    }

    private void checkForMatchEnd() {
        if (matchEnded) {
            return;
        }
        if (player1.controller.isGameOver() && player2.controller.isGameOver()) {
            matchEnded = true;
            stopGame();
            // same reason as GameScreen.onGameOver() - can't showAndWait() mid-pulse
            Platform.runLater(this::announceResult);
        }
    }

    private void announceResult() {
        int score1 = player1.controller.getScore();
        int score2 = player2.controller.getScore();

        String verdict;
        if (score1 > score2) {
            verdict = "Player 1 wins!";
        } else if (score2 > score1) {
            verdict = "Player 2 wins!";
        } else {
            verdict = "It's a tie!";
        }

        Alert result = new Alert(AlertType.INFORMATION);
        result.setTitle("Match Over");
        result.setHeaderText(verdict);
        result.setContentText("Player 1: " + score1 + " points\nPlayer 2: " + score2 + " points");
        result.showAndWait();

        promptNameAndRecord("Player 1", score1);
        promptNameAndRecord("Player 2", score2);
    }

    private void promptNameAndRecord(String defaultName, int score) {
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Game Over");
        dialog.setHeaderText(defaultName + " - final score: " + score);
        dialog.setContentText("Enter name for the high score list:");

        Optional<String> name = dialog.showAndWait();
        name.ifPresent(playerName ->
                HighScoreManager.getInstance().addScore(playerName, score));
    }

    private void handleBack() {
        boolean wasRunning = !matchEnded;
        if (wasRunning) {
            SoundManager.getInstance().pauseMusic();
            if (!player1.controller.isPaused()) {
                player1.controller.handlePauseKey();
            }
            if (!player2.controller.isPaused()) {
                player2.controller.handlePauseKey();
            }
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Stop Game");
        confirm.setHeaderText("Are you sure you want to stop the current match?");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        confirm.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            stopGame();
            onBackToMenu.run();
        } else if (wasRunning) {
            if (!player1.controller.isGameOver()) {
                player1.controller.handlePauseKey();
            }
            if (!player2.controller.isGameOver()) {
                player2.controller.handlePauseKey();
            }
            SoundManager.getInstance().resumeMusic();
        }
        pausedLabel.setVisible(player1.controller.isPaused() || player2.controller.isPaused());
    }

    public StackPane getRoot() {
        return root;
    }

    // One player's board + score + AI status + game-over overlay.
    private static class PlayerPanel {

        private final GameController controller;
        private final int gridWidth;
        private final int gridHeight;

        private final StackPane root;
        private final Rectangle[][] cellRects;
        private final Label scoreLabel;
        private final Label levelLabel;
        private final Label linesLabel;
        private final Label aiStatusLabel;
        private final Label gameOverLabel;

        PlayerPanel(String name, int gridWidth, int gridHeight, TetrominoSequence sharedSequence) {
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            this.controller = new GameController(gridWidth, gridHeight, sharedSequence);
            this.controller.setOnGameOver(() ->
                    SoundManager.getInstance().playEffect(SoundManager.Sound.GAME_OVER));
            this.controller.setOnMove(() ->
                    SoundManager.getInstance().playEffect(SoundManager.Sound.MOVE));
            this.controller.setOnRotate(() ->
                    SoundManager.getInstance().playEffect(SoundManager.Sound.ROTATE));
            this.controller.setOnLock(() ->
                    SoundManager.getInstance().playEffect(SoundManager.Sound.LOCK));
            this.controller.setOnLinesCleared(rows ->
                    SoundManager.getInstance().playEffect(SoundManager.Sound.LINE_CLEAR));
            this.cellRects = new Rectangle[gridHeight][gridWidth];

            Pane boardPane = new Pane();
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

            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaaaaa; -fx-font-weight: bold;");

            scoreLabel = new Label("Score: 0");
            scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

            levelLabel = new Label("Level: 1");
            levelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

            linesLabel = new Label("Lines: 0");
            linesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

            aiStatusLabel = new Label("AI PLAY");
            aiStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #33ccff; -fx-font-weight: bold;");
            aiStatusLabel.setVisible(false);

            gameOverLabel = new Label("GAME OVER");
            gameOverLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; "
                    + "-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 12px;");
            gameOverLabel.setVisible(false);

            VBox header = new VBox(2, nameLabel, scoreLabel, levelLabel, linesLabel, aiStatusLabel);
            header.setAlignment(Pos.CENTER);

            VBox column = new VBox(6, header, boardPane);
            column.setAlignment(Pos.CENTER);

            root = new StackPane(column, gameOverLabel);
            StackPane.setAlignment(gameOverLabel, Pos.CENTER);
        }

        void reset() {
            gameOverLabel.setVisible(false);
            aiStatusLabel.setVisible(controller.isAiEnabled() || controller.isExternalEnabled());
        }

        boolean isHumanControlled() {
            return !controller.isAiEnabled() && !controller.isExternalEnabled();
        }

        void toggleAi() {
            if (controller.isExternalEnabled()) {
                return; //external player driven by server, not 1/2 keys
            }
            controller.setAiEnabled(!controller.isAiEnabled());
            aiStatusLabel.setVisible(controller.isAiEnabled());
        }
        void applyConfig(int startingLevel, ConfigurationManager.PlayerType type) {
            controller.setStartingLevel(startingLevel);
            boolean aiEnabled = (type == ConfigurationManager.PlayerType.AI);
            boolean externalEnabled = (type == ConfigurationManager.PlayerType.EXTERNAL);
            controller.setAiEnabled(aiEnabled);
            controller.setExternalEnabled(externalEnabled);
            aiStatusLabel.setText(externalEnabled ? "EXTERNAL PLAY" : "AI PLAY");
            aiStatusLabel.setVisible(aiEnabled || externalEnabled);
        }

        void draw() {
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
            levelLabel.setText("Level: " + controller.getLevel());
            linesLabel.setText("Lines: " + controller.getLinesCleared());
            gameOverLabel.setVisible(controller.isGameOver());
        }
    }
}
