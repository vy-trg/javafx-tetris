package org.oosd.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.oosd.HighScoreManager;
import org.oosd.HighScoreManager.ScoreEntry;

public class HighScoresScreen {

    public static void show(Stage stage) {

        Label title =
                new Label("HIGH SCORES");

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.EXTRA_BOLD,
                        40
                )
        );

        title.setTextFill(Color.WHITE);

        title.setStyle(
                "-fx-effect: dropshadow(gaussian, #38bdf8, 15, 0.6, 0, 0);"
        );

        TableView<Score> table =
                new TableView<>();

        TableColumn<Score, String> playerColumn =
                new TableColumn<>("PLAYER");

        playerColumn.setCellValueFactory(
                new PropertyValueFactory<>("player")
        );

        TableColumn<Score, Integer> scoreColumn =
                new TableColumn<>("SCORE");

        scoreColumn.setCellValueFactory(
                new PropertyValueFactory<>("score")
        );

        playerColumn.setPrefWidth(300);
        scoreColumn.setPrefWidth(200);

        table.getColumns().addAll(
                playerColumn,
                scoreColumn
        );

        loadScores(table);

        table.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #38bdf8;" +
                        "-fx-border-width: 2;"
        );

        table.setPrefWidth(520);
        table.setPrefHeight(350);

        Button clearButton =
                new Button("CLEAR ALL SCORES");

        clearButton.setPrefWidth(180);
        clearButton.setPrefHeight(40);

        clearButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        14
                )
        );

        clearButton.setTextFill(Color.WHITE);

        clearButton.setStyle(
                "-fx-background-color: #7f1d1d;" +
                        "-fx-border-color: #f87171;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        clearButton.setOnAction(event -> {

            Alert confirmation =
                    new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Clear all high scores?",
                            ButtonType.YES,
                            ButtonType.NO
                    );

            confirmation.setTitle(
                    "Clear High Scores"
            );

            confirmation.setHeaderText(
                    "Clear All Scores"
            );

            confirmation.showAndWait().ifPresent(
                    result -> {

                        if (result == ButtonType.YES) {

                            HighScoreManager
                                    .getInstance()
                                    .clearScores();

                            loadScores(table);
                        }
                    }
            );
        });

        Button backButton =
                new Button("BACK");

        backButton.setPrefWidth(150);
        backButton.setPrefHeight(40);

        backButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        backButton.setTextFill(Color.WHITE);

        backButton.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #38bdf8;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        backButton.setOnAction(event ->
                MainMenu.show(stage)
        );

        HBox buttons =
                new HBox(
                        15,
                        clearButton,
                        backButton
                );

        buttons.setAlignment(
                Pos.CENTER
        );

        VBox layout =
                new VBox(20);

        layout.setAlignment(
                Pos.CENTER
        );

        layout.setPadding(
                new Insets(30)
        );

        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );

        layout.getChildren().addAll(
                title,
                table,
                buttons
        );

        Scene scene =
                new Scene(
                        layout,
                        800,
                        650
                );

        stage.setTitle(
                "Tetris - High Scores"
        );

        stage.setScene(scene);
        stage.show();
    }

    private static void loadScores(
            TableView<Score> table) {

        table.getItems().clear();

        for (
                ScoreEntry entry :
                HighScoreManager
                        .getInstance()
                        .getTopScores()
        ) {

            table.getItems().add(
                    new Score(
                            entry.player(),
                            entry.score()
                    )
            );
        }
    }

    public static class Score {

        private final String player;
        private final int score;

        public Score(
                String player,
                int score) {

            this.player = player;
            this.score = score;
        }

        public String getPlayer() {
            return player;
        }

        public int getScore() {
            return score;
        }
    }
}