package com.cgvsu;

import com.cgvsu.controller.Theme;
import com.cgvsu.controller.ThemeManager;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.objwriter.ObjWriterException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelInstance;
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
    @FXML private ColorPicker modelColorPicker;
    @FXML private ColorPicker mashColorPicker;

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

    @FXML private RadioMenuItem lightThemeItem;
    @FXML private RadioMenuItem darkThemeItem;

    private com.cgvsu.model.Scene modelScene = new com.cgvsu.model.Scene();

    private Timeline timeline;
    private ThemeManager themeManager;

    private boolean isLeftButtonPressed = false;
    private boolean isRightButtonPressed = false;
    private boolean isCtrlPressed = false;
    private double lastMouseX, lastMouseY;
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;
    private boolean spacePressed = false;
    private boolean shiftPressed = false;

    @FXML
    public void initialize() {
        System.out.println("GuiController initialized");

        fixPanelsSize();

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        // === ИНИЦИАЛИЗАЦИЯ КНОПОК РЕНДЕРИНГА ===
        wireframeToggle.setSelected(true);
        textureToggle.setSelected(false);
        lightingToggle.setSelected(false);

        // Устанавливаем начальные режимы рендеринга
        RenderEngine.setWireframeMode(true);
        RenderEngine.setTextureMode(false);
        RenderEngine.setLightingMode(false);

        // Подключаем обработчики кнопок
        wireframeToggle.setOnAction(e -> {
            boolean selected = wireframeToggle.isSelected();
            RenderEngine.setWireframeMode(selected);
            updateStatus("Wireframe mode: " + (selected ? "ON" : "OFF"));

            // Если включаем каркас, выключаем текстуры
            if (selected) {
                textureToggle.setSelected(false);
                RenderEngine.setTextureMode(false);
            }
        });

        textureToggle.setOnAction(e -> {
            boolean selected = textureToggle.isSelected();
            RenderEngine.setTextureMode(selected);
            updateStatus("Texture mode: " + (selected ? "ON" : "OFF"));

            // Если включаем текстуры, выключаем каркас
            if (selected) {
                wireframeToggle.setSelected(false);
                RenderEngine.setWireframeMode(false);
            }
        });

        lightingToggle.setOnAction(e -> {
            boolean selected = lightingToggle.isSelected();
            RenderEngine.setLightingMode(selected);
            updateStatus("Lighting mode: " + (selected ? "ON" : "OFF"));
        });

        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        rotateXField.setText("0");
        rotateYField.setText("0");
        rotateZField.setText("0");
        translateXField.setText("0");
        translateYField.setText("0");
        translateZField.setText("0");

        modelListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Камеры
        cameraListView.getItems().add("Camera 1");

        ToggleGroup themeToggleGroup = new ToggleGroup();
        lightThemeItem.setToggleGroup(themeToggleGroup);
        darkThemeItem.setToggleGroup(themeToggleGroup);

        // Инициализируем ThemeManager и применяем светлую тему по умолчанию
        themeManager = new ThemeManager();
        if (mainPane.getScene() != null) {
            themeManager.applyTheme(mainPane.getScene());
        } else {
            // Если сцена еще не доступна, подождать и применить позже
            Platform.runLater(() -> themeManager.applyTheme(mainPane.getScene()));
        }

        lightThemeItem.setSelected(true);

        // Иниц. обработчиков мыши
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnScroll(this::handleOnScroll);

        // Иниц. обработчиков клавиатуры
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(this::handleKeyPressed);
        canvas.setOnKeyReleased(this::handleKeyReleased);

        // Инициализация ColorPicker
        modelColorPicker.setOnAction(event -> {
            Color newColor = modelColorPicker.getValue();
            setColorToSelectedModels(newColor);
        });

        startRendering();

        updateStatus();
    }

    //WASD
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case W -> wPressed = true;
            case A -> aPressed = true;
            case S -> sPressed = true;
            case D -> dPressed = true;
            case SPACE -> spacePressed = true;
            case SHIFT -> shiftPressed = true;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        switch (event.getCode()) {
            case W -> wPressed = false;
            case A -> aPressed = false;
            case S -> sPressed = false;
            case D -> dPressed = false;
            case SPACE -> spacePressed = false;
            case SHIFT -> shiftPressed = false;
        }
    }

    // мышь обработка
    private void handleMousePressed(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            isLeftButtonPressed = true;
        }
        if (event.isSecondaryButtonDown()) {
            isRightButtonPressed = true;
        }
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        isCtrlPressed = event.isControlDown();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            isLeftButtonPressed = false;
        }
        if (!event.isSecondaryButtonDown()) {
            isRightButtonPressed = false;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isLeftButtonPressed) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;

            if (!isCtrlPressed) {
                panCamera(deltaX, deltaY);
            }

            lastMouseX = event.getX();
            lastMouseY = event.getY();
        }

        if (isRightButtonPressed) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;

            rotateCamera(deltaX, deltaY);  // вращение камеры

            lastMouseX = event.getX();
            lastMouseY = event.getY();
        }
    }

    private void handleOnScroll(ScrollEvent event) {
        double delta = event.getDeltaY();
        float zoomSensitivity = 0.1f;

        // приближение/отдаление
        com.cgvsu.render_engine.Camera camera = modelScene.getCamera();
        float distance = camera.getDistance();
        distance -= (float) (delta * zoomSensitivity);
        distance = Math.max(10.0f, Math.min(1000.0f, distance));
        camera.setDistance(distance);
    }

    //Мышь методы
    private void panCamera(double deltaX, double deltaY) {
        float panSensitivity = 0.05f;
        com.cgvsu.render_engine.Camera camera = modelScene.getCamera();

        Vector3f direction = camera.getTarget().sub(camera.getPosition());
        direction.normalize();

        Vector3f right = direction.cross(new Vector3f(0, 1, 0));
        right.normalize();
        right.multiply((float) deltaX * panSensitivity);

        Vector3f up = new Vector3f(0, 1, 0);
        up.multiply((float) deltaY * panSensitivity);

        //перемещение цели камеры
        camera.setTarget(camera.getTarget().add(right).add(up));
    }

    private void rotateCamera(double deltaX, double deltaY) {
        float sensitivity = 0.5f;
        com.cgvsu.render_engine.Camera camera = modelScene.getCamera();

        float azimuth = camera.getAzimuth();
        float elevation = camera.getElevation();

        azimuth += (float) (deltaX * sensitivity);
        elevation += (float) (deltaY * sensitivity);

        elevation = Math.max(-87, Math.min(87, elevation));
        azimuth = azimuth % 360;
        if (azimuth < 0) azimuth += 360;

        camera.setAzimuth(azimuth);
        camera.setElevation(elevation);
    }

    // движ в реальном времени
    private void updateCameraMovement() {
        float speed = TRANSLATION * 0.5f; // Скорость движения

        if (wPressed) modelScene.getCamera().movePosition(new Vector3f(0, 0, -speed));
        if (sPressed) modelScene.getCamera().movePosition(new Vector3f(0, 0, speed));
        if (aPressed) modelScene.getCamera().movePosition(new Vector3f(speed, 0, 0));
        if (dPressed) modelScene.getCamera().movePosition(new Vector3f(-speed, 0, 0));
        if (spacePressed) modelScene.getCamera().movePosition(new Vector3f(0, speed, 0));
        if (shiftPressed) modelScene.getCamera().movePosition(new Vector3f(0, -speed, 0));
    }

    private void fixPanelsSize() {
        leftPanel.setMinWidth(250);
        leftPanel.setMaxWidth(250);
        leftPanel.setPrefWidth(250);

        rightPanel.setMinWidth(300);
        rightPanel.setMaxWidth(300);
        rightPanel.setPrefWidth(300);

        canvasContainer.setMinWidth(600);
    }

    private void startRendering() {
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(16), event -> {
            updateCameraMovement();
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            if (width > 0 && height > 0) {
                canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
                modelScene.getCamera().setAspectRatio((float) (width / height));
                RenderEngine.renderScene(canvas.getGraphicsContext2D(), modelScene, (int) width, (int) height);
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
        if (file == null) {
            System.out.println("File not selected");
            return;
        }

        try {
            System.out.println("=== \n" + "START FILE LOADING ===");
            System.out.println("File: " + file.getAbsolutePath());
            System.out.println("Size: " + file.length() + " byte");

            String fileContent = new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("Characters read: " + fileContent.length());

            System.out.println("Parsing an OBJ file...");
            Model model = ObjReader.read(fileContent);

            System.out.println("Model loaded successfully!");
            System.out.println("Statistics:");
            System.out.println("  Vertex: " + model.getVertices().size());
            System.out.println("  Texture coordinates: " + model.getTextureVertices().size());
            System.out.println("  Normal: " + model.getNormals().size());
            System.out.println("  Polygons: " + model.getPolygons().size());

            if (!model.getPolygons().isEmpty()) {
                Polygon firstPolygon = model.getPolygons().get(0);
                System.out.println("First polygon:");
                System.out.println("  Vertex indices: " + firstPolygon.getVertexIndices());
                if (firstPolygon.getTextureVertexIndices() != null) {
                    System.out.println("  Texture indexes: " + firstPolygon.getTextureVertexIndices());
                }
                if (firstPolygon.getNormalIndices() != null) {
                    System.out.println("  Normal indices: " + firstPolygon.getNormalIndices());
                }
            }

            ModelInstance instance = new ModelInstance(model);
            modelScene.addModelInstance(instance);

            // Пытаемся загрузить текстуру (если есть файл с тем же именем .png или .jpg)
            String modelPath = file.getAbsolutePath();
            String baseName = modelPath.substring(0, modelPath.lastIndexOf('.'));

            // Проверяем различные форматы текстур
            String[] textureExtensions = {".png", ".jpg", ".jpeg", ".bmp"};
            for (String ext : textureExtensions) {
                File textureFile = new File(baseName + ext);
                if (textureFile.exists()) {
                    RenderEngine.loadTexture(model, textureFile.getAbsolutePath());
                    break;
                }
            }

            updateModelList();
            int lastIndex = modelScene.getModelCount() - 1;
            modelListView.getSelectionModel().select(lastIndex);
            modelScene.selectModel(lastIndex);

            updateStatus("Model loaded: " + file.getName());
            System.out.println("=== DOWNLOAD COMPLETED SUCCESSFULLY ===\n");

        } catch (com.cgvsu.objreader.ObjReaderException e) {
            System.err.println("=== OBJ PARSING ERROR ===");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Line number: " + e.getLineIndex());
            e.printStackTrace();
            System.err.println("=== END OF ERROR ===");

            showError("OBJ Parsing Error",
                    "Failed to parse OBJ file at line " + e.getLineIndex() + ":\n" +
                            e.getMessage());

        } catch (Exception e) {
            System.err.println("=== GENERAL LOADING ERROR ===");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Messagt: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END OF ERROR ===");

            showError("Error loading model",
                    "Failed to load model: " + e.getClass().getSimpleName() +
                            "\n" + e.getMessage());
        }
    }

    @FXML
    public void onSaveModelMenuItemClick(ActionEvent event) {
        Set<Integer> selectedIndices = modelScene.getSelectedIndices();
        if (selectedIndices.isEmpty()) {
            showError("Нет выбранной модели", "Пожалуйста, выберите модель для сохранения.");
            return;
        }

        int modelIndex = selectedIndices.iterator().next();
        ModelInstance instance = modelScene.getModelInstance(modelIndex);

        if (instance == null) {
            showError("Ошибка", "Не удалось получить выбранную модель.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить модель как OBJ");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ Files", "*.obj"));

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());

        if (file != null) {
            try {
                Model modelToSave = instance.getModel();
                ObjWriter.write(modelToSave, file.getAbsolutePath());
                updateStatus("Модель успешно сохранена: " + file.getName());

            } catch (ObjWriterException e) {
                showError("Ошибка сохранения OBJ",
                        "Строка " + e.getLineIndex() + ": " + e.getMessage());
            } catch (Exception e) {
                showError("Ошибка", "Произошла ошибка при сохранении:\n" + e.getMessage());
            }
        }
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
        modelScene.clear();
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
        if (mainPane.getScene() != null) {
            themeManager.setTheme(mainPane.getScene(), Theme.LIGHT);
            lightThemeItem.setSelected(true);
            updateStatus("Light theme applied");
        }
    }

    @FXML
    public void onDarkThemeClick(ActionEvent event) {
        if (mainPane.getScene() != null) {
            themeManager.setTheme(mainPane.getScene(), Theme.DARK);
            darkThemeItem.setSelected(true);
            updateStatus("Dark theme applied");
        }
    }

    // === Левая панель - Управление моделями ===

    @FXML
    public void onModelListSelectionChanged() {
        modelScene.clearSelection();
        for (int index : modelListView.getSelectionModel().getSelectedIndices()) {
            modelScene.addToSelection(index);
        }

        updateModelSelectionVisuals();

        updateStatus("Selected " + modelScene.getSelectedIndices().size() + " model(s)");
    }

    @FXML
    public void onSelectAllModelsClick(ActionEvent event) {
        modelScene.clearSelection();

        for (int i = 0; i < modelScene.getModelCount(); i++) {
            modelScene.addToSelection(i);
        }

        modelListView.getSelectionModel().selectAll();

        updateStatus("Все модели выбраны (" + modelScene.getModelCount() + " шт.)");
    }

    @FXML
    public void onDeleteAllSelectionClick(ActionEvent actionEvent) {
        if (modelScene.getModelCount() == 0) {
            updateStatus("Нет моделей для удаления");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Удаление всех моделей");
        confirmAlert.setHeaderText("Вы уверены, что хотите удалить все модели?");
        confirmAlert.setContentText("Будет удалено " + modelScene.getModelCount() + " моделей. Это действие нельзя отменить.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            modelScene.clear();
            updateModelList();
            resetTransformationFields();
            updateStatus("Все модели удалены");
        } else {
            updateStatus("Удаление отменено");
        }
    }

    @FXML
    public void onResetSelectionBtn(ActionEvent actionEvent) {
        modelListView.getSelectionModel().clearSelection();
        modelScene.clearSelection();
        updateStatus("Выделение сброшено");
    }

    private void resetTransformationFields() {
        scaleXField.setText("1.0");
        scaleYField.setText("1.0");
        scaleZField.setText("1.0");
        rotateXField.setText("0");
        rotateYField.setText("0");
        rotateZField.setText("0");
        translateXField.setText("0");
        translateYField.setText("0");
        translateZField.setText("0");
    }

    @FXML
    public void onRemoveSelectedModelsClick(ActionEvent event) {
        Set<Integer> selectedIndices = modelScene.getSelectedIndices();
        if (selectedIndices.isEmpty()) {
            updateStatus("Нет выбранных моделей для удаления");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Удаление выбранных моделей");
        confirmAlert.setHeaderText("Вы уверены, что хотите удалить выбранные модели?");
        confirmAlert.setContentText("Будет удалено " + selectedIndices.size() + " моделей. Это действие нельзя отменить.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            modelScene.removeSelectedModels();
            updateModelList();
            resetTransformationFields();
            updateStatus("Выбранные модели удалены (" + selectedIndices.size() + " шт.)");
        } else {
            updateStatus("Удаление отменено");
        }
    }

    @FXML
    public void onRemoveVertexClick(ActionEvent event) {
        updateStatus("Remove vertex - not implemented yet");
    }

    @FXML
    public void onRemovePolygonClick(ActionEvent event) {
        updateStatus("Remove polygon - not implemented yet");
    }

    @FXML
    private ColorPicker meshColorPicker;


    // === Правая панель - Управление камерой ===

    @FXML
    public void onCameraForwardClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void onCameraBackwardClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void onCameraLeftClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void onCameraRightClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void onCameraUpClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void onCameraDownClick(ActionEvent event) {
        modelScene.getCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
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

    // === Правая панель - Управление освещением ===

    @FXML
    public void onLoadTextureClick(ActionEvent event) {
        Set<Integer> selectedIndices = modelScene.getSelectedIndices();
        if (selectedIndices.isEmpty()) {
            showError("No model selected", "Please select a model to load texture");
            return;
        }

        int modelIndex = selectedIndices.iterator().next();
        Model model = modelScene.getModelInstance(modelIndex).getModel();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null) {
            RenderEngine.loadTexture(model, file.getAbsolutePath());
            updateStatus("Texture loaded: " + file.getName());

            // Включаем режим текстур
            textureToggle.setSelected(true);
            RenderEngine.setTextureMode(true);
            wireframeToggle.setSelected(false);
            RenderEngine.setWireframeMode(false);
        }
    }

    // === Правая панель - Трансформации ===

    @FXML
    public void onApplyScaleClick(ActionEvent event) {
        try {
            double scaleX = Double.parseDouble(scaleXField.getText());
            double scaleY = Double.parseDouble(scaleYField.getText());
            double scaleZ = Double.parseDouble(scaleZField.getText());

            for (ModelInstance instance : modelScene.getSelectedModelInstances()) {
                instance.scaleX(scaleX);
                instance.scaleY(scaleY);
                instance.scaleZ(scaleZ);
            }

            updateStatus("Scale applied: X=" + scaleX + ", Y=" + scaleY + ", Z=" + scaleZ);

            triggerRender();

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

            for (ModelInstance instance : modelScene.getSelectedModelInstances()) {
                instance.rotateX(rotateX);
                instance.rotateY(rotateY);
                instance.rotateZ(rotateZ);
            }

            updateStatus("Rotation applied: X=" + rotateX + "°, Y=" + rotateY + "°, Z=" + rotateZ + "°");

            triggerRender();

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

            for (ModelInstance instance : modelScene.getSelectedModelInstances()) {
                instance.translateX(translateX);
                instance.translateY(translateY);
                instance.translateZ(translateZ);
            }

            updateStatus("Translation applied: X=" + translateX + ", Y=" + translateY + ", Z=" + translateZ);

            triggerRender();

        } catch (NumberFormatException e) {
            showError("Invalid translation values", "Please enter valid numbers");
        }
    }

    @FXML
    public void onResetTransformationsClick(ActionEvent event) {
        modelScene.resetSelectedTransformations();

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

        triggerRender();
    }

    private void triggerRender() {
        renderImmediately();
    }

    private void renderImmediately() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (width > 0 && height > 0) {
            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            modelScene.getCamera().setAspectRatio((float) (width / height));
            RenderEngine.renderScene(canvas.getGraphicsContext2D(), modelScene, (int) width, (int) height);
        }
    }

    // === Вспомогательные методы ===

    private void updateModelList() {
        modelListView.getItems().clear();
        for (int i = 0; i < modelScene.getModelCount(); i++) {
            modelListView.getItems().add("Model " + (i + 1));
        }
    }

    private void updateStatus() {
        updateStatus("Ready");
    }

    private void updateStatus(String message) {
        String modelInfo = " | Models: " + modelScene.getModelCount() +
                " | Selected: " + modelScene.getSelectedIndices().size();
        statusLabel.setText(message + modelInfo);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setColorToSelectedModels(Color color) {
        Set<Integer> selectedIndices = modelScene.getSelectedIndices();

        if (selectedIndices.isEmpty()) {
            updateStatus("Нет выделенных моделей для изменения цвета");
            return;
        }

        // Применяем цвет ко всем выделенным моделям
        for (Integer index : selectedIndices) {
            ModelInstance instance = modelScene.getModelInstance(index);
            if (instance != null) {
                instance.setModelColor(color);
            }
        }

        renderImmediately();
        updateStatus("Цвет изменен для " + selectedIndices.size() + " модели(ей)");
    }

    private void updateModelSelectionVisuals() {
        Set<Integer> selectedIndices = modelScene.getSelectedIndices();

        // 1. Все невыделенные модели делаем белыми
        for (int i = 0; i < modelScene.getModelCount(); i++) {
            if (!selectedIndices.contains(i)) {
                ModelInstance instance = modelScene.getModelInstance(i);
                if (instance != null) {
                    instance.setModelColor(Color.WHITE);
                }
            }
        }

        // 2. Для выделенных моделей:
        //    - Если цвет белый (новое выделение) - делаем красным
        //    - Если цвет уже другой (пользовательский) - оставляем как есть
        for (Integer index : selectedIndices) {
            ModelInstance instance = modelScene.getModelInstance(index);
            if (instance != null) {
                // Если модель белая (только что выделили), делаем красной
                if (instance.getModelColor().equals(Color.WHITE)) {
                    instance.setModelColor(Color.RED);
                }
            }
        }

        // 3. Обновляем ColorPicker цветом первой выделенной модели
        if (!selectedIndices.isEmpty() && modelColorPicker != null) {
            ModelInstance firstSelected = modelScene.getModelInstance(selectedIndices.iterator().next());
            modelColorPicker.setValue(firstSelected.getModelColor());
        }

        renderImmediately();
    }
}