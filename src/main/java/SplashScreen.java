import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen {

    public static void show(Stage stage) {


        // BACKGROUND
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #111827, #020617);"
        );


        // GAME TITLE
        Label title = new Label("TETRIS");

        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 72));
        title.setTextFill(Color.WHITE);

        // Glow effect using CSS
        title.setStyle(
                "-fx-effect: dropshadow(gaussian, #38bdf8, 20, 0.7, 0, 0);"
        );


        Label subtitle = new Label("CLASSIC PUZZLE GAME");

        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        subtitle.setTextFill(Color.LIGHTGRAY);


        // TETRIS BLOCKS
        HBox blocks = new HBox(5);
        blocks.setAlignment(Pos.CENTER);

        Color[] blockColors = {
                Color.CYAN,
                Color.YELLOW,
                Color.ORANGE,
                Color.LIMEGREEN,
                Color.HOTPINK,
                Color.DODGERBLUE,
                Color.ORANGERED
        };

        for (Color color : blockColors) {

            Rectangle block = new Rectangle(35, 35);

            block.setFill(color);

            block.setArcWidth(6);
            block.setArcHeight(6);

            block.setStyle(
                    "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 5, 0.5, 0, 0);"
            );

            blocks.getChildren().add(block);
        }

        // ANIMATE THE BLOCKS
        for (int i = 0; i < blocks.getChildren().size(); i++) {

            Rectangle block = (Rectangle) blocks.getChildren().get(i);

            TranslateTransition animation =
                    new TranslateTransition(Duration.seconds(0.7), block);

            animation.setFromY(0);
            animation.setToY(15);

            animation.setAutoReverse(true);
            animation.setCycleCount(Animation.INDEFINITE);

            // Different delay for each block
            animation.setDelay(Duration.millis(i * 100));

            animation.play();
        }

        // LOADING TEXT
        Label loadingText = new Label("LOADING GAME...");

        loadingText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        loadingText.setTextFill(Color.LIGHTGRAY);



        // LOADING BAR
        Rectangle loadingBackground = new Rectangle(300, 12);

        loadingBackground.setArcWidth(10);
        loadingBackground.setArcHeight(10);

        loadingBackground.setFill(Color.web("#374151"));


        Rectangle loadingBar = new Rectangle(0, 12);

        loadingBar.setArcWidth(10);
        loadingBar.setArcHeight(10);

        loadingBar.setFill(Color.CYAN);


        StackPane loadingPane = new StackPane();

        loadingPane.getChildren().addAll(
                loadingBackground,
                loadingBar
        );

        loadingPane.setAlignment(Pos.CENTER_LEFT);


        // Animate loading bar
        Timeline loadingAnimation = new Timeline(

                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(loadingBar.widthProperty(), 0)
                ),

                new KeyFrame(
                        Duration.seconds(2.5),
                        new KeyValue(loadingBar.widthProperty(), 300)
                )
        );

        loadingAnimation.play();

        // PRESS START
        Label startText = new Label("PRESS START");

        startText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        startText.setTextFill(Color.WHITE);

        // Blinking animation
        FadeTransition blink =
                new FadeTransition(Duration.seconds(0.7), startText);

        blink.setFromValue(1);
        blink.setToValue(0.2);

        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);

        blink.play();


        // TEAM MEMBERS
        Label teamTitle = new Label("DEVELOPED BY");

        teamTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        teamTitle.setTextFill(Color.CYAN);


        Label teamMembers = new Label(
                "Tasmia Faizal Aboo\n" +
                        "Aiden Lucas\n" +
                        "Thi Tuong Vy Truong"
        );

        teamMembers.setFont(Font.font("Arial", 13));
        teamMembers.setTextFill(Color.LIGHTGRAY);

        teamMembers.setAlignment(Pos.CENTER);


        // CENTER CONTENT
        VBox centerContent = new VBox(18);

        centerContent.setAlignment(Pos.CENTER);

        centerContent.getChildren().addAll(
                title,
                subtitle,
                blocks,
                loadingText,
                loadingPane,
                startText
        );


        // TEAM SECTION
        VBox teamSection = new VBox(6);

        teamSection.setAlignment(Pos.CENTER);

        teamSection.getChildren().addAll(
                teamTitle,
                teamMembers
        );

        teamSection.setPadding(
                new javafx.geometry.Insets(0, 0, 25, 0)
        );


        root.setCenter(centerContent);
        root.setBottom(teamSection);


        // SCENE
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();

        // MOVE TO MAIN MENU
        PauseTransition pause =
                new PauseTransition(Duration.seconds(7));

        pause.setOnFinished(event -> MainMenu.show(stage));

        pause.play();
    }
}