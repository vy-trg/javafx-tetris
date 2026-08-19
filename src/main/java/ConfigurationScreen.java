import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ConfigurationScreen {

    public static void show(Stage stage) {

        // TITLE
        Label title = new Label("CONFIGURATION");

        title.setFont(
                Font.font("Arial", FontWeight.EXTRA_BOLD, 40)
        );

        title.setTextFill(Color.WHITE);

        title.setStyle(
                "-fx-effect: dropshadow(gaussian, #38bdf8, 15, 0.6, 0, 0);"
        );


        // BOARD SETTINGs
        Label boardSettingsTitle =
                new Label("GAME BOARD");

        boardSettingsTitle.setFont(
                Font.font("Arial", FontWeight.BOLD, 22)
        );

        boardSettingsTitle.setTextFill(Color.CYAN);


        Label widthLabel =
                new Label("Board Width:");

        Label heightLabel =
                new Label("Board Height:");

        widthLabel.setTextFill(Color.WHITE);
        heightLabel.setTextFill(Color.WHITE);


        Spinner<Integer> widthSpinner =
                new Spinner<>(5, 15, 10);

        Spinner<Integer> heightSpinner =
                new Spinner<>(15, 30, 20);



        // GAME SETTINGS
        Label gameSettingsTitle =
                new Label("GAME SETTINGS");

        gameSettingsTitle.setFont(
                Font.font("Arial", FontWeight.BOLD, 22)
        );

        gameSettingsTitle.setTextFill(Color.CYAN);


        Label levelLabel =
                new Label("Game Level:");

        levelLabel.setTextFill(Color.WHITE);


        Spinner<Integer> levelSpinner =
                new Spinner<>(1, 10, 1);


        // CHECKBOXES
        CheckBox musicCheckBox =
                new CheckBox("Background Music");

        CheckBox soundCheckBox =
                new CheckBox("Sound Effects");

        CheckBox aiCheckBox =
                new CheckBox("AI Play");

        CheckBox extendCheckBox =
                new CheckBox("Extend Mode");


        musicCheckBox.setSelected(true);
        soundCheckBox.setSelected(true);


        CheckBox[] checkBoxes = {
                musicCheckBox,
                soundCheckBox,
                aiCheckBox,
                extendCheckBox
        };


        for (CheckBox checkBox : checkBoxes) {

            checkBox.setTextFill(Color.WHITE);

            checkBox.setFont(
                    Font.font("Arial", 14)
            );
        }

        // SETTINGS GRID
        GridPane settingsGrid =
                new GridPane();

        settingsGrid.setHgap(25);
        settingsGrid.setVgap(15);

        settingsGrid.setAlignment(Pos.CENTER);

        settingsGrid.add(widthLabel, 0, 0);
        settingsGrid.add(widthSpinner, 1, 0);

        settingsGrid.add(heightLabel, 0, 1);
        settingsGrid.add(heightSpinner, 1, 1);

        settingsGrid.add(levelLabel, 0, 2);
        settingsGrid.add(levelSpinner, 1, 2);


        // CHECKBOX CONTAINER
        VBox checkboxes =
                new VBox(10);

        checkboxes.setAlignment(Pos.CENTER_LEFT);

        checkboxes.getChildren().addAll(
                musicCheckBox,
                soundCheckBox,
                aiCheckBox,
                extendCheckBox
        );

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
                new VBox(18);

        layout.setAlignment(Pos.CENTER);

        layout.setPadding(
                new Insets(30)
        );

        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );


        layout.getChildren().addAll(
                title,
                boardSettingsTitle,
                settingsGrid,
                gameSettingsTitle,
                checkboxes,
                backButton
        );

        // SCENE
        Scene scene =
                new Scene(layout, 800, 600);

        stage.setTitle("Tetris - Configuration");

        stage.setScene(scene);

        stage.show();
    }
}