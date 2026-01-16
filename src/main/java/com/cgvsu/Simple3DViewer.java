package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Simple3DViewer extends Application {

    private GuiController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Simple3DViewer.class.getResource("/com/cgvsu/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 800);
        controller = fxmlLoader.getController();

        setupHotkeys(scene);

        stage.setTitle("3D Viewer CGVSU Project");
        stage.setScene(scene);

        // ОГРАНИЧИВАЕМ ИЗМЕНЕНИЕ РАЗМЕРА ОКНА
        stage.setMinWidth(1400); // Минимальная ширина
        stage.setMinHeight(800); // Минимальная высота
        stage.setMaxWidth(1800); // Максимальная ширина
        stage.setMaxHeight(1000); // Максимальная высота

        // ИЛИ ЗАФИКСИРОВАТЬ РАЗМЕР ОКНА ВООБЩЕ
        // stage.setResizable(false);

        stage.show();
    }

    private void setupHotkeys(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case UP -> controller.onCameraUpClick(null);
                    case DOWN -> controller.onCameraDownClick(null);
                    case LEFT -> controller.onCameraLeftClick(null);
                    case RIGHT -> controller.onCameraRightClick(null);
                    case EQUALS, ADD -> controller.onCameraForwardClick(null);
                    case MINUS, SUBTRACT -> controller.onCameraBackwardClick(null);
                }
            }

            switch (event.getCode()) {
                case O -> {
                    if (event.isControlDown()) controller.onOpenModelMenuItemClick(null);
                }
                case S -> {
                    if (event.isControlDown()) controller.onSaveModelMenuItemClick(null);
                }
                case DELETE -> controller.onRemoveSelectedModelsClick(null);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}