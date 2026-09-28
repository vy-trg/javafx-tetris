import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Optional;

import org.oosd.GameScreen;
import org.oosd.TwoPlayerGameScreen;
import org.oosd.ConfigurationManager;
import org.oosd.controller.ExternalPlayer;

public class MainMenu {

    public static void show(Stage stage) {


        // TITLE
        Label title = new Label("TETRIS");

        title.setFont(
                Font.font("Arial", FontWeight.EXTRA_BOLD, 60)
        );

        title.setTextFill(Color.WHITE);

        title.setStyle(
                "-fx-effect: dropshadow(gaussian, #38bdf8, 20, 0.7, 0, 0);"
        );


        Label subtitle = new Label("MAIN MENU");

        subtitle.setFont(
                Font.font("Arial", FontWeight.BOLD, 20)
        );

        subtitle.setTextFill(Color.LIGHTGRAY);

        // BUTTONS
        Button playButton = new Button("PLAY");
        Button twoPlayerButton = new Button("2-PLAYER");
        Button configurationButton = new Button("CONFIGURATION");
        Button highScoresButton = new Button("HIGH SCORES");
        Button exitButton = new Button("EXIT");


        Button[] buttons = {
                playButton,
                twoPlayerButton,
                configurationButton,
                highScoresButton,
                exitButton
        };


        for (Button button : buttons) {

            button.setPrefWidth(280);
            button.setPrefHeight(50);

            button.setFont(
                    Font.font("Arial", FontWeight.BOLD, 16)
            );

            button.setTextFill(Color.WHITE);

            button.setStyle(
                    "-fx-background-color: #1e293b;" +
                            "-fx-border-color: #38bdf8;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;"
            );

            // Hover effect
            button.setOnMouseEntered(event ->
                    button.setStyle(
                            "-fx-background-color: #0e7490;" +
                                    "-fx-border-color: #67e8f9;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 6;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-effect: dropshadow(gaussian, #38bdf8, 10, 0.5, 0, 0);"
                    )
            );

            button.setOnMouseExited(event ->
                    button.setStyle(
                            "-fx-background-color: #1e293b;" +
                                    "-fx-border-color: #38bdf8;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 6;" +
                                    "-fx-background-radius: 6;"
                    )
            );
        }

        // PLAY
        playButton.setOnAction(event -> {

            ConfigurationManager config =
                    ConfigurationManager.getInstance();

            GameScreen gameScreen =
                    new GameScreen(
                            () -> MainMenu.show(stage),
                            config.getWidth(),
                            config.getHeight(),
                            config.getLevel(),
                            config.isAiPlay()
                    );

            int windowWidth =
                    Math.max(
                            800,
                            config.getWidth() * 24 + 260
                    );

            int windowHeight =
                    Math.max(
                            600,
                            config.getHeight() * 24 + 100
                    );

            stage.setScene(
                    new Scene(
                            gameScreen.getRoot(),
                            windowWidth,
                            windowHeight
                    )
            );

            stage.setTitle("Tetris - Game");

            gameScreen.startGame();

            gameScreen.getRoot().requestFocus();
        });

        // 2-PLAYER (DEMO)
        twoPlayerButton.setOnAction(event -> {

            ConfigurationManager config = ConfigurationManager.getInstance();

            ConfigurationManager.PlayerType p1Type = config.isExtendMode()
                    ? config.getPlayer1Type() : ConfigurationManager.PlayerType.HUMAN;
            ConfigurationManager.PlayerType p2Type = config.isExtendMode()
                    ? config.getPlayer2Type() : ConfigurationManager.PlayerType.HUMAN;

            //external players driven by TetrisServer.jar, it must already be running
            if ((p1Type == ConfigurationManager.PlayerType.EXTERNAL
                    || p2Type == ConfigurationManager.PlayerType.EXTERNAL)
                    && !ExternalPlayer.isServerReachable()) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("External Player");
                error.setHeaderText("TetrisServer is not running");
                error.setContentText("A player is set to External, but nothing is listening on "
                        + ExternalPlayer.HOST + ":" + ExternalPlayer.PORT + ".\n\n"
                        + "Start it with:  java -jar TetrisServer.jar\n"
                        + "then press Two-Player again.");
                error.showAndWait();
                MainMenu.show(stage);
                return;
            }

            TwoPlayerGameScreen twoPlayerScreen = new TwoPlayerGameScreen(
                    () -> MainMenu.show(stage),
                    config.getWidth(),
                    config.getHeight(),
                    config.getLevel(),
                    p1Type,
                    p2Type
            );

            int windowWidth = Math.max(1000, config.getWidth() * 2 * 24 + 320);
            int windowHeight = Math.max(700, config.getHeight() * 24 + 150);

            stage.setScene(new Scene(twoPlayerScreen.getRoot(), windowWidth, windowHeight));
            stage.setTitle("Tetris - Two-Player");

            twoPlayerScreen.startGame();
            twoPlayerScreen.getRoot().requestFocus();
        });
        // CONFIGURATION
        configurationButton.setOnAction(event ->
                ConfigurationScreen.show(stage)
        );

        // HIGH SCORES
        highScoresButton.setOnAction(event ->
                HighScoresScreen.show(stage)
        );

        // EXIT
        exitButton.setOnAction(event -> {

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to exit?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirmation.setTitle("Exit");
            confirmation.setHeaderText("Exit Application");

            Optional<ButtonType> result =
                    confirmation.showAndWait();

            if (result.isPresent() &&
                    result.get() == ButtonType.YES) {

                stage.close();
            }
        });

        // LAYOUT
        VBox layout = new VBox(18);

        layout.setAlignment(Pos.CENTER);

        layout.setPadding(
                new Insets(40)
        );

        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );


        layout.getChildren().addAll(
                title,
                subtitle,
                playButton,
                twoPlayerButton,
                configurationButton,
                highScoresButton,
                exitButton
        );

        // SCENE
        Scene scene =
                new Scene(layout, 800, 600);

        stage.setTitle("Tetris - Main Menu");

        stage.setScene(scene);

        stage.show();
    }
}
