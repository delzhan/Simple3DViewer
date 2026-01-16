package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

    final private float TRANSLATION = 2.0F;

    @FXML private BorderPane mainPane;
    @FXML private VBox canvasContainer;
    @FXML private Canvas canvas;
    @FXML private VBox leftPanel;
    @FXML private VBox rightPanel;
    @FXML private HBox statusBar;
    @FXML private Label statusLabel;

    // Левая панель
    @FXML private ListView<String> modelListView;

    // Правая панель
    @FXML private ListView<String> cameraListView;
    @FXML private ComboBox<String> renderModeCombo;
    @FXML private ToggleButton wireframeToggle;
    @FXML private ToggleButton textureToggle;
    @FXML private ToggleButton lightingToggle;

    // Трансформации
    @FXML private TextField scaleXField;
    @FXML private TextField scaleYField;
    @FXML private TextField scaleZField;
    @FXML private TextField rotateXField;
    @FXML private TextField rotateYField;
    @FXML private TextField rotateZField;
    @FXML private TextField translateXField;
    @FXML private TextField translateYField;
    @FXML private TextField translateZField;
    @FXML private CheckBox applyTransformOnSaveCheck;

    // Тема
    @FXML private RadioMenuItem lightThemeItem;
    @FXML private RadioMenuItem darkThemeItem;

    private Scene scene = new Scene();
    private Timeline timeline;
    private boolean isDarkTheme = false;

    // Добавляем слушатели для изменения размера
    private ChangeListener<Number> windowResizeListener;

    @FXML
    public void initialize() {
        // ФИКСИРУЕМ РАЗМЕР ПАНЕЛЕЙ
         fixPanelsSize();

        // Привязка размеров Canvas к размерам контейнера
        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        modelListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        renderModeCombo.getItems().addAll("Wireframe", "Solid", "Textured", "Shaded");
        renderModeCombo.setValue("Wireframe");

        // Настройка значений по умолчанию
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        rotateXField.setText("0");
        rotateYField.setText("0");
        rotateZField.setText("0");
        translateXField.setText("0");
        translateYField.setText("0");
        translateZField.setText("0");

        // Камеры
        cameraListView.getItems().add("Camera 1 (Main)");

        startRendering();
        applyLightTheme();
        updateStatus();
    }

    private void fixPanelsSize() {
        // Жестко фиксируем размеры панелей
        leftPanel.setMinWidth(250);
        leftPanel.setMaxWidth(250);
        leftPanel.setPrefWidth(250);

        rightPanel.setMinWidth(300);
        rightPanel.setMaxWidth(300);
        rightPanel.setPrefWidth(300);

        // Центральная область будет занимать всё оставшееся пространство
        canvasContainer.setMinWidth(600); // Минимальная ширина для канваса
    }

    private void startRendering() {
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(16), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            if (width > 0 && height > 0) {
                canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
                scene.getCamera().setAspectRatio((float) (width / height));
                RenderEngine.renderScene(canvas.getGraphicsContext2D(), scene, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    // === File Menu ===

    @FXML
    public void onOpenModelMenuItemClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("3D Models (*.obj)", "*.obj"));
        fileChooser.setTitle("Open 3D Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) return;

        try {
            String fileContent = Files.readString(Path.of(file.getAbsolutePath()));
            Model model = ObjReader.read(fileContent);
            ModelInstance instance = new ModelInstance(model);
            scene.addModelInstance(instance);

            updateModelList();
            int lastIndex = scene.getModelCount() - 1;
            modelListView.getSelectionModel().select(lastIndex);
            scene.selectModel(lastIndex);

            updateStatus("Model loaded: " + file.getName());
        } catch (Exception e) {
            showError("Error loading model", e.getMessage());
        }
    }

    @FXML
    public void onSaveModelMenuItemClick(ActionEvent event) {
        updateStatus("Save model - not implemented yet");
    }

    @FXML
    public void onExitMenuItemClick(ActionEvent event) {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    // === Edit Menu ===

    @FXML
    public void onDeleteVertexMenuItemClick(ActionEvent event) {
        updateStatus("Delete vertex - not implemented yet");
    }

    @FXML
    public void onDeletePolygonMenuItemClick(ActionEvent event) {
        updateStatus("Delete polygon - not implemented yet");
    }

    @FXML
    public void onClearSceneMenuItemClick(ActionEvent event) {
        scene.clear();
        updateModelList();
        updateStatus("Scene cleared");
    }

    // === View Menu ===

    @FXML
    public void onWireframeModeClick(ActionEvent event) {
        updateStatus("Wireframe mode selected");
    }

    @FXML
    public void onTextureModeClick(ActionEvent event) {
        updateStatus("Texture mode selected");
    }

    @FXML
    public void onLightingModeClick(ActionEvent event) {
        updateStatus("Lighting mode selected");
    }

    @FXML
    public void onLightThemeClick(ActionEvent event) {
        applyLightTheme();
        updateStatus("Light theme applied");
    }

    @FXML
    public void onDarkThemeClick(ActionEvent event) {
        applyDarkTheme();
        updateStatus("Dark theme applied");
    }

    // === Левая панель - Управление моделями ===

    @FXML
    public void onModelListSelectionChanged() {
        scene.clearSelection();
        for (int index : modelListView.getSelectionModel().getSelectedIndices()) {
            scene.addToSelection(index);
        }
        updateStatus("Selected " + scene.getSelectedIndices().size() + " model(s)");
    }

    @FXML
    public void onSelectAllModelsClick(ActionEvent event) {
        modelListView.getSelectionModel().selectAll();
    }

    @FXML
    public void onClearSelectionClick(ActionEvent event) {
        modelListView.getSelectionModel().clearSelection();
    }

    @FXML
    public void onRemoveSelectedModelsClick(ActionEvent event) {
        scene.removeSelectedModels();
        updateModelList();
        updateStatus("Selected models removed");
    }

    @FXML
    public void onRemoveVertexClick(ActionEvent event) {
        updateStatus("Remove vertex - not implemented yet");
    }

    @FXML
    public void onRemovePolygonClick(ActionEvent event) {
        updateStatus("Remove polygon - not implemented yet");
    }

    // === Правая панель - Управление камерой ===

    @FXML
    public void onCameraForwardClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void onCameraBackwardClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void onCameraLeftClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void onCameraRightClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void onCameraUpClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void onCameraDownClick(ActionEvent event) {
        scene.getCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void onAddCameraClick(ActionEvent event) {
        int cameraCount = cameraListView.getItems().size() + 1;
        cameraListView.getItems().add("Camera " + cameraCount);
        updateStatus("Camera " + cameraCount + " added");
    }

    @FXML
    public void onRemoveCameraClick(ActionEvent event) {
        int selectedIndex = cameraListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            cameraListView.getItems().remove(selectedIndex);
            updateStatus("Camera removed");
        }
    }

    @FXML
    public void onRenderModeChanged(ActionEvent event) {
        String mode = renderModeCombo.getValue();
        updateStatus("Render mode changed to: " + mode);
    }

    // === Правая панель - Трансформации ===

    @FXML
    public void onApplyScaleClick(ActionEvent event) {
        try {
            double scaleX = Double.parseDouble(scaleXField.getText());
            double scaleY = Double.parseDouble(scaleYField.getText());
            double scaleZ = Double.parseDouble(scaleZField.getText());

            for (ModelInstance instance : scene.getSelectedModelInstances()) {
                instance.scaleX(scaleX);
                instance.scaleY(scaleY);
                instance.scaleZ(scaleZ);
            }

            updateStatus("Scale applied: X=" + scaleX + ", Y=" + scaleY + ", Z=" + scaleZ);
        } catch (NumberFormatException e) {
            showError("Invalid scale values", "Please enter valid numbers");
        }
    }

    @FXML
    public void onApplyRotationClick(ActionEvent event) {
        try {
            double rotateX = Double.parseDouble(rotateXField.getText());
            double rotateY = Double.parseDouble(rotateYField.getText());
            double rotateZ = Double.parseDouble(rotateZField.getText());

            for (ModelInstance instance : scene.getSelectedModelInstances()) {
                instance.rotateX(rotateX);
                instance.rotateY(rotateY);
                instance.rotateZ(rotateZ);
            }

            updateStatus("Rotation applied: X=" + rotateX + "°, Y=" + rotateY + "°, Z=" + rotateZ + "°");
        } catch (NumberFormatException e) {
            showError("Invalid rotation values", "Please enter valid numbers");
        }
    }

    @FXML
    public void onApplyTranslationClick(ActionEvent event) {
        try {
            double translateX = Double.parseDouble(translateXField.getText());
            double translateY = Double.parseDouble(translateYField.getText());
            double translateZ = Double.parseDouble(translateZField.getText());

            for (ModelInstance instance : scene.getSelectedModelInstances()) {
                instance.translateX(translateX);
                instance.translateY(translateY);
                instance.translateZ(translateZ);
            }

            updateStatus("Translation applied: X=" + translateX + ", Y=" + translateY + ", Z=" + translateZ);
        } catch (NumberFormatException e) {
            showError("Invalid translation values", "Please enter valid numbers");
        }
    }

    @FXML
    public void onResetTransformationsClick(ActionEvent event) {
        scene.resetSelectedTransformations();

        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        rotateXField.setText("0");
        rotateYField.setText("0");
        rotateZField.setText("0");
        translateXField.setText("0");
        translateYField.setText("0");
        translateZField.setText("0");

        updateStatus("Transformations reset");
    }

    // === Вспомогательные методы ===

    private void updateModelList() {
        modelListView.getItems().clear();
        for (int i = 0; i < scene.getModelCount(); i++) {
            modelListView.getItems().add("Model " + (i + 1));
        }
    }

    private void updateStatus() {
        updateStatus("Ready");
    }

    private void updateStatus(String message) {
        String modelInfo = " | Models: " + scene.getModelCount() +
                " | Selected: " + scene.getSelectedIndices().size();
        statusLabel.setText(message + modelInfo);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyLightTheme() {
        isDarkTheme = false;
        mainPane.setStyle("-fx-background-color: #f8f9fa;");
        leftPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-padding: 10;");
        rightPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-padding: 10;");
        canvasContainer.setStyle("-fx-border-color: #adb5bd; -fx-border-width: 1; -fx-border-radius: 4;");
        canvas.setStyle("-fx-background-color: #e9ecef;");
        statusBar.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        statusLabel.setStyle("-fx-text-fill: #495057; -fx-font-size: 12px;");
    }

    private void applyDarkTheme() {
        isDarkTheme = true;
        mainPane.setStyle("-fx-background-color: #212529;");
        leftPanel.setStyle("-fx-background-color: #343a40; -fx-border-color: #495057; -fx-border-width: 1; -fx-padding: 10;");
        rightPanel.setStyle("-fx-background-color: #343a40; -fx-border-color: #495057; -fx-border-width: 1; -fx-padding: 10;");
        canvasContainer.setStyle("-fx-border-color: #495057; -fx-border-width: 1; -fx-border-radius: 4;");
        canvas.setStyle("-fx-background-color: #1a1a2e;");
        statusBar.setStyle("-fx-background-color: #343a40; -fx-border-color: #495057; -fx-border-width: 1 0 0 0;");
        statusLabel.setStyle("-fx-text-fill: #f8f9fa; -fx-font-size: 12px;");
    }
}