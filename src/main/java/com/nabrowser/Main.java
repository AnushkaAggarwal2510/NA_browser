package com.nabrowser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private double xOffset;
    private double yOffset;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
            Main.class.getResource("browser.fxml")
        );

        // Load the existing browser interface
        VBox root = new VBox();

        HBox titleBar = createTitleBar(stage);

        root.getChildren().add(titleBar);
        root.getChildren().add(loader.load());

        VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);

        scene.getStylesheets().add(
            Main.class.getResource("browser.css").toExternalForm()
        );

        // Remove the default Windows title bar
        stage.initStyle(StageStyle.UNDECORATED);

        stage.setTitle("NA Browser");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.setResizable(true);

        stage.show();
    }

    private HBox createTitleBar(Stage stage) {

        HBox titleBar = new HBox(12);
        titleBar.getStyleClass().add("custom-title-bar");

        titleBar.setAlignment(Pos.CENTER_LEFT);

        Label appIcon = new Label("✿");
        appIcon.getStyleClass().add("title-icon");

        Label title = new Label("NA Browser");
        title.getStyleClass().add("title-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().add("window-button");
        minimizeButton.setOnAction(event -> stage.setIconified(true));

        Button maximizeButton = new Button("□");
        maximizeButton.getStyleClass().add("window-button");

        maximizeButton.setOnAction(event -> {
            stage.setMaximized(!stage.isMaximized());
            maximizeButton.setText(
                stage.isMaximized() ? "❐" : "□"
            );
        });

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(event -> stage.close());

        titleBar.getChildren().addAll(
            appIcon,
            title,
            spacer,
            minimizeButton,
            maximizeButton,
            closeButton
        );

        // Allow dragging the custom title bar
        titleBar.setOnMousePressed((MouseEvent event) -> {
            if (!stage.isMaximized()) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Double-click title bar to maximize/restore
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        return titleBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
