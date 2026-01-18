package com.cgvsu;

import com.cgvsu.controller.Theme;
import com.cgvsu.controller.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Screen;

import java.io.IOException;

public class Simple3DViewer extends Application {

    private GuiController controller;
    private ThemeManager themeManager;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Simple3DViewer.class.getResource("/com/cgvsu/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 800);
        controller = fxmlLoader.getController();

        themeManager = new ThemeManager();
        themeManager.applyTheme(scene);

        setupHotkeys(scene);

        stage.setTitle("3D Viewer CGVSU Project");
        stage.setScene(scene);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setMinWidth(1400);
        stage.setMinHeight(800);
        stage.setMaxWidth(bounds.getWidth());
        stage.setMaxHeight(bounds.getHeight());

        stage.setX((bounds.getWidth() - 1400) / 2);
        stage.setY((bounds.getHeight() - 800) / 2);

        stage.show();
    }

    private void setupHotkeys(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case UP -> {
                        if (controller != null) controller.onCameraUpClick(null);
                    }
                    case DOWN -> {
                        if (controller != null) controller.onCameraDownClick(null);
                    }
                    case LEFT -> {
                        if (controller != null) controller.onCameraLeftClick(null);
                    }
                    case RIGHT -> {
                        if (controller != null) controller.onCameraRightClick(null);
                    }
                    case EQUALS, ADD -> {
                        if (controller != null) controller.onCameraForwardClick(null);
                    }
                    case MINUS, SUBTRACT -> {
                        if (controller != null) controller.onCameraBackwardClick(null);
                    }
                }
            }

            switch (event.getCode()) {
                case O -> {
                    if (event.isControlDown() && controller != null)
                        controller.onOpenModelMenuItemClick(null);
                }
                case S -> {
                    if (event.isControlDown() && controller != null)
                        controller.onSaveModelMenuItemClick(null);
                }
                case DELETE -> {
                    if (controller != null) controller.onRemoveSelectedModelsClick(null);
                }
                case L -> {
                    if (event.isControlDown() && event.isShiftDown()) {
                        // Ctrl+Shift+L - светлая тема
                        if (controller != null && scene != null) {
                            themeManager.setTheme(scene, Theme.LIGHT);
                        }
                    }
                }
                case D -> {
                    if (event.isControlDown() && event.isShiftDown()) {
                        // Ctrl+Shift+D - темная тема
                        if (controller != null && scene != null) {
                            themeManager.setTheme(scene, Theme.DARK);
                        }
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        System.out.println("Loading 3D Viewer...");
        launch(args);
    }
}