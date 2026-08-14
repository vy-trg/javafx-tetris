package org.oosd;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private StackPane root;
    private Scene scene;
    private AnimationTimer timer;

    private final double fieldWidth = 800;
    private final double fieldHeight = 600;

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();
        scene = new Scene(root, fieldWidth, fieldHeight);

        showMainScreen();

        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMainScreen() {
        VBox mainScreen = new VBox(10);
        mainScreen.setPadding(new Insets(20));

        // Main Screen Label
        Label title = new Label("Main Menu");

        Button startButton = new Button("Start Game");
        Button configButton = new Button("Configuration");
        Button highScoreButton = new Button("High Scores");
        Button exitButton = new Button("Exit");

        startButton.setOnAction(e -> showGameScreen());
        configButton.setOnAction(e -> showConfigScreen());
        highScoreButton.setOnAction(e -> showHighScoreScreen());
        exitButton.setOnAction(e -> javafx.application.Platform.exit());

        mainScreen.getChildren().add(title);
        mainScreen.getChildren().add(startButton);
        mainScreen.getChildren().add(configButton);
        mainScreen.getChildren().add(highScoreButton);
        mainScreen.getChildren().add(exitButton);

        root.getChildren().setAll(mainScreen);
    }

    private void showGameScreen() {
        Pane gamePane = new Pane();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            showMainScreen();
        });

        gamePane.getChildren().add(backButton);

        root.getChildren().setAll(gamePane);
    }

    private void showConfigScreen() {
        VBox configScreen = new VBox(10);
        configScreen.setPadding(new Insets(20));

        Label configLabel = new Label("Configuration");

        Label widthLabel = new Label("Field Width (No. of Cells)");
        Slider configWidth = new Slider(5,15,10);
        configWidth.setShowTickMarks(true);
        configWidth.setShowTickLabels(true);
        configWidth.setMajorTickUnit(1f);

        Label heightLabel = new Label("Field Height (No. of Cells)");
        Slider configHeight = new Slider(15,30,20);
        configHeight.setShowTickMarks(true);
        configHeight.setShowTickLabels(true);
        configHeight.setMajorTickUnit(1f);

        Label levelLabel = new Label("Game Level");
        Slider configLevel = new Slider(1,10,1);
        configLevel.setShowTickMarks(true);
        configLevel.setShowTickLabels(true);
        configLevel.setMajorTickUnit(1f);

        CheckBox configMusic = new CheckBox("Music (ON/OFF)");
        CheckBox configSound = new CheckBox("Sound Effects (ON/OFF)");
        CheckBox configAI = new CheckBox("AI Play (ON/OFF)");
        CheckBox configExtend = new CheckBox("Extend Mode (ON/OFF)");

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            showMainScreen();
        });

        configScreen.getChildren().add(configLabel);

        configScreen.getChildren().add(widthLabel);
        configScreen.getChildren().add(configWidth);

        configScreen.getChildren().add(heightLabel);
        configScreen.getChildren().add(configHeight);

        configScreen.getChildren().add(levelLabel);
        configScreen.getChildren().add(configLevel);

        configScreen.getChildren().add(configMusic);
        configScreen.getChildren().add(configSound);
        configScreen.getChildren().add(configAI);
        configScreen.getChildren().add(configExtend);

        configScreen.getChildren().add(backButton);
        root.getChildren().setAll(configScreen);
    }

    private void showHighScoreScreen() {
        VBox highScoreScreen = new VBox(10);
        highScoreScreen.setPadding(new Insets(20));

        Label configLabel = new Label("High Scores");

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            showMainScreen();
        });

        highScoreScreen.getChildren().add(configLabel);
        highScoreScreen.getChildren().add(backButton);

        root.getChildren().setAll(highScoreScreen);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
