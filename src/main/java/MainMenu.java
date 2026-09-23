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
        Button twoPlayerButton = new Button("2-PLAYER (DEMO)");
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
        playButton.setOnAction(event ->
          {
            GameScreen gameScreen = new GameScreen(() -> MainMenu.show(stage));

            stage.setScene(new Scene(gameScreen.getRoot(), 800, 600));
            stage.setTitle("Tetris - Game");

            gameScreen.startGame();
            gameScreen.getRoot().requestFocus();
        }
        );

        // 2-PLAYER (DEMO)
        twoPlayerButton.setOnAction(event ->
          {
            TwoPlayerGameScreen twoPlayerScreen = new TwoPlayerGameScreen(() -> MainMenu.show(stage));

            stage.setScene(new Scene(twoPlayerScreen.getRoot(), 1000, 700));
            stage.setTitle("Tetris - Two-Player");

            twoPlayerScreen.startGame();
            twoPlayerScreen.getRoot().requestFocus();
        }
        );

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
