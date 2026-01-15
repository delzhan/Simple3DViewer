package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelInstance;
import com.cgvsu.model.Scene;
import com.cgvsu.objreader.ObjReader;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Scene scene = new Scene();

    private Timeline timeline;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            scene.getCamera().setAspectRatio((float) (width / height));

            RenderEngine.renderScene(canvas.getGraphicsContext2D(), scene, (int) width, (int) height);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);

            ModelInstance instance = new ModelInstance(model);
            scene.addModelInstance(instance);

        } catch (IOException exception) {
            System.err.println("Ошибка загрузки файла: " + exception.getMessage());
            exception.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ошибка парсинга модели: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        scene.getCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void handleSelectFirstModel(ActionEvent actionEvent) {
        if (scene.getModelCount() > 0) {
            scene.selectModel(0);
        }
    }

    @FXML
    public void handleSelectSecondModel(ActionEvent actionEvent) {
        if (scene.getModelCount() > 1) {
            scene.selectModel(1);
        }
    }

    @FXML
    public void handleAddSecondToSelection(ActionEvent actionEvent) {
        if (scene.getModelCount() > 1) {
            scene.addToSelection(1);
        }
    }

    @FXML
    public void handleClearSelection(ActionEvent actionEvent) {
        scene.clearSelection();
    }

    @FXML
    public void handleSelectAllModels(ActionEvent actionEvent) {
        scene.selectAll();
    }

    @FXML
    public void handleRotateSelectedX(ActionEvent actionEvent) {
        scene.rotateSelectedX(10.0);
    }

    @FXML
    public void handleRotateSelectedY(ActionEvent actionEvent) {
        scene.rotateSelectedY(10.0);
    }

    @FXML
    public void handleRotateSelectedZ(ActionEvent actionEvent) {
        scene.rotateSelectedZ(10.0);
    }

    @FXML
    public void handleScaleSelectedIncrease(ActionEvent actionEvent) {
        scene.scaleSelected(1.1);
    }

    @FXML
    public void handleScaleSelectedDecrease(ActionEvent actionEvent) {
        scene.scaleSelected(0.9);
    }

    @FXML
    public void handleTranslateSelectedXPos(ActionEvent actionEvent) {
        scene.translateSelectedX(5.0);
    }

    @FXML
    public void handleTranslateSelectedXNeg(ActionEvent actionEvent) {
        scene.translateSelectedX(-5.0);
    }

    @FXML
    public void handleTranslateSelectedYPos(ActionEvent actionEvent) {
        scene.translateSelectedY(5.0);
    }

    @FXML
    public void handleTranslateSelectedYNeg(ActionEvent actionEvent) {
        scene.translateSelectedY(-5.0);
    }

    @FXML
    public void handleTranslateSelectedZPos(ActionEvent actionEvent) {
        scene.translateSelectedZ(5.0);
    }

    @FXML
    public void handleTranslateSelectedZNeg(ActionEvent actionEvent) {
        scene.translateSelectedZ(-5.0);
    }

    @FXML
    public void handleResetSelectedTransformations(ActionEvent actionEvent) {
        scene.resetSelectedTransformations();
    }

    @FXML
    public void handleClearScene(ActionEvent actionEvent) { //Очищает сцену (удаляет все модели)
        scene.clear();
    }

    @FXML
    public void handleChangeBackground(ActionEvent actionEvent) { // Изменяет цвет фона сцены
        // Используем Vector3f для установки цвета
        scene.setBackgroundColor(new Vector3f(0.1f, 0.1f, 0.3f)); // Темно-синий
    }

    @FXML
    public void handleRemoveSelectedModels(ActionEvent actionEvent) { // Удаляет выбранные модели
        scene.removeSelectedModels();
    }

    public String getSceneInfo() { // Возвращает информацию о текущем состоянии сцены
        return String.format("Моделей на сцене: %d, Выбрано: %d",
                scene.getModelCount(),
                scene.getSelectedIndices().size());
    }
}