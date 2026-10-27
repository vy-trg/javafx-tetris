package org.oosd.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.oosd.ConfigurationManager;
import org.oosd.ConfigurationManager.PlayerType;

public class ConfigurationScreen {

    public static void show(Stage stage) {

        ConfigurationManager config =
                ConfigurationManager.getInstance();

        Label title =
                new Label("CONFIGURATION");

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

        Label boardSettingsTitle =
                new Label("GAME BOARD");

        boardSettingsTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        boardSettingsTitle.setTextFill(Color.CYAN);

        Label widthLabel =
                new Label("Board Width:");

        Label heightLabel =
                new Label("Board Height:");

        widthLabel.setTextFill(Color.WHITE);
        heightLabel.setTextFill(Color.WHITE);

        Spinner<Integer> widthSpinner =
                new Spinner<>(
                        5,
                        15,
                        config.getWidth()
                );

        Spinner<Integer> heightSpinner =
                new Spinner<>(
                        15,
                        30,
                        config.getHeight()
                );

        Label gameSettingsTitle =
                new Label("GAME SETTINGS");

        gameSettingsTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        22
                )
        );

        gameSettingsTitle.setTextFill(Color.CYAN);

        Label levelLabel =
                new Label("Game Level:");

        levelLabel.setTextFill(Color.WHITE);

        Spinner<Integer> levelSpinner =
                new Spinner<>(
                        1,
                        10,
                        config.getLevel()
                );

        CheckBox musicCheckBox =
                new CheckBox("Background Music");

        CheckBox soundCheckBox =
                new CheckBox("Sound Effects");

        CheckBox aiCheckBox =
                new CheckBox("AI Play");

        CheckBox extendCheckBox =
                new CheckBox("Extend Mode");

        musicCheckBox.setSelected(
                config.isMusicEnabled()
        );

        soundCheckBox.setSelected(
                config.isSoundEnabled()
        );

        aiCheckBox.setSelected(
                config.isAiPlay()
        );

        extendCheckBox.setSelected(
                config.isExtendMode()
        );

        musicCheckBox.setOnAction(event ->
                config.setMusicEnabled(
                        musicCheckBox.isSelected()
                )
        );

        soundCheckBox.setOnAction(event ->
                config.setSoundEnabled(
                        soundCheckBox.isSelected()
                )
        );

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

        Label player1Label =
                new Label("Player 1:");

        Label player2Label =
                new Label("Player 2:");

        player1Label.setTextFill(Color.WHITE);
        player2Label.setTextFill(Color.WHITE);

        ComboBox<PlayerType> player1Combo =
                new ComboBox<>();

        ComboBox<PlayerType> player2Combo =
                new ComboBox<>();

        player1Combo.getItems().addAll(
                PlayerType.HUMAN,
                PlayerType.AI,
                PlayerType.EXTERNAL
        );

        player2Combo.getItems().addAll(
                PlayerType.HUMAN,
                PlayerType.AI,
                PlayerType.EXTERNAL
        );

        player1Combo.setValue(
                config.getPlayer1Type()
        );

        player2Combo.setValue(
                config.getPlayer2Type()
        );

        player1Combo.setDisable(
                !extendCheckBox.isSelected()
        );

        player2Combo.setDisable(
                !extendCheckBox.isSelected()
        );

        extendCheckBox.setOnAction(event -> {

            boolean enabled =
                    extendCheckBox.isSelected();

            player1Combo.setDisable(!enabled);
            player2Combo.setDisable(!enabled);
        });

        GridPane settingsGrid =
                new GridPane();

        settingsGrid.setHgap(25);
        settingsGrid.setVgap(15);
        settingsGrid.setAlignment(Pos.CENTER);

        settingsGrid.add(
                widthLabel,
                0,
                0
        );

        settingsGrid.add(
                widthSpinner,
                1,
                0
        );

        settingsGrid.add(
                heightLabel,
                0,
                1
        );

        settingsGrid.add(
                heightSpinner,
                1,
                1
        );

        settingsGrid.add(
                levelLabel,
                0,
                2
        );

        settingsGrid.add(
                levelSpinner,
                1,
                2
        );

        VBox checkboxes =
                new VBox(10);

        checkboxes.setAlignment(
                Pos.CENTER_LEFT
        );

        checkboxes.getChildren().addAll(
                musicCheckBox,
                soundCheckBox,
                aiCheckBox,
                extendCheckBox
        );

        GridPane playerGrid =
                new GridPane();

        playerGrid.setHgap(20);
        playerGrid.setVgap(10);
        playerGrid.setAlignment(Pos.CENTER);

        playerGrid.add(
                player1Label,
                0,
                0
        );

        playerGrid.add(
                player1Combo,
                1,
                0
        );

        playerGrid.add(
                player2Label,
                0,
                1
        );

        playerGrid.add(
                player2Combo,
                1,
                1
        );

        Button saveButton =
                new Button("SAVE SETTINGS");

        saveButton.setPrefWidth(180);
        saveButton.setPrefHeight(40);

        saveButton.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        15
                )
        );

        saveButton.setTextFill(Color.WHITE);

        saveButton.setStyle(
                "-fx-background-color: #0e7490;" +
                        "-fx-border-color: #67e8f9;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        saveButton.setOnAction(event -> {

            config.setWidth(
                    widthSpinner.getValue()
            );

            config.setHeight(
                    heightSpinner.getValue()
            );

            config.setLevel(
                    levelSpinner.getValue()
            );

            config.setAiPlay(
                    aiCheckBox.isSelected()
            );

            config.setExtendMode(
                    extendCheckBox.isSelected()
            );

            config.setPlayer1Type(
                    player1Combo.getValue()
            );

            config.setPlayer2Type(
                    player2Combo.getValue()
            );

            config.save();

            MainMenu.show(stage);
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

        VBox layout =
                new VBox(15);

        layout.setAlignment(
                Pos.CENTER
        );

        layout.setPadding(
                new Insets(20)
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
                playerGrid,
                saveButton,
                backButton
        );

        Scene scene =
                new Scene(
                        layout,
                        800,
                        700
                );

        stage.setTitle(
                "Tetris - Configuration"
        );

        stage.setScene(scene);
        stage.show();
    }
}