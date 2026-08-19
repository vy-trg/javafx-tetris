import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HighScoresScreen {

    public static void show(Stage stage) {


        // TITLE
        Label title =
                new Label("HIGH SCORES");

        title.setFont(
                Font.font("Arial", FontWeight.EXTRA_BOLD, 40)
        );

        title.setTextFill(Color.WHITE);

        title.setStyle(
                "-fx-effect: dropshadow(gaussian, #38bdf8, 15, 0.6, 0, 0);"
        );



        // TABLE
        TableView<Score> table =
                new TableView<>();


        // Player column
        TableColumn<Score, String> playerColumn =
                new TableColumn<>("PLAYER");

        playerColumn.setCellValueFactory(
                new PropertyValueFactory<>("player")
        );


        // Score column
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


        // PLACEHOLDER SCORES
        table.getItems().addAll(
                new Score("Player 1", 10000),
                new Score("Player 2", 9000),
                new Score("Player 3", 8000),
                new Score("Player 4", 7000),
                new Score("Player 5", 6000),
                new Score("Player 6", 5000),
                new Score("Player 7", 4000),
                new Score("Player 8", 3000),
                new Score("Player 9", 2000),
                new Score("Player 10", 1000)
        );


        // TABLE STYLING
        table.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #38bdf8;" +
                        "-fx-border-width: 2;"
        );


        table.setPrefWidth(520);
        table.setPrefHeight(350);


        // BACK BUTTON
        Button backButton =
                new Button("BACK");

        backButton.setPrefWidth(150);
        backButton.setPrefHeight(40);

        backButton.setFont(
                Font.font("Arial", FontWeight.BOLD, 15)
        );

        backButton.setTextFill(Color.WHITE);

        backButton.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #38bdf8;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );


        backButton.setOnMouseEntered(event ->
                backButton.setStyle(
                        "-fx-background-color: #0e7490;" +
                                "-fx-border-color: #67e8f9;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;"
                )
        );


        backButton.setOnMouseExited(event ->
                backButton.setStyle(
                        "-fx-background-color: #1e293b;" +
                                "-fx-border-color: #38bdf8;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;"
                )
        );


        backButton.setOnAction(event ->
                MainMenu.show(stage)
        );


        // LAYOUT
        VBox layout =
                new VBox(20);

        layout.setAlignment(Pos.CENTER);

        layout.setPadding(
                new Insets(30)
        );

        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );


        layout.getChildren().addAll(
                title,
                table,
                backButton
        );


        // SCENE
        Scene scene =
                new Scene(layout, 800, 600);

        stage.setTitle("Tetris - High Scores");

        stage.setScene(scene);

        stage.show();
    }


    // SCORE CLASS
    public static class Score {

        private final String player;
        private final int score;


        public Score(String player, int score) {

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