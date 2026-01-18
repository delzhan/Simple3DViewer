package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.model.ModelInstance;
import com.cgvsu.model.Scene;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RenderEngine {

    private static boolean wireframeMode = true;
    private static boolean textureMode = false;
    private static boolean lightingMode = false;

    private static Map<Model, Image> modelTextures = new HashMap<>();

    private static Vector3f lightDirection = new Vector3f(0, 1, 1);
    private static float ambientIntensity = 0.3f;
    private static float diffuseIntensity = 0.7f;

    static {
        lightDirection = lightDirection.normalizeV();
    }

    public static void setWireframeMode(boolean enabled) {
        wireframeMode = enabled;
        System.out.println("Wireframe mode: " + (enabled ? "ON" : "OFF"));
    }

    public static void setTextureMode(boolean enabled) {
        textureMode = enabled;
        System.out.println("Texture mode: " + (enabled ? "ON" : "OFF"));
    }

    public static void setLightingMode(boolean enabled) {
        lightingMode = enabled;
        System.out.println("Lighting mode: " + (enabled ? "ON" : "OFF"));
    }

    // Загрузка текстуры
    public static void loadTexture(Model model, String texturePath) {
        try {
            Image texture = new Image("file:" + texturePath);
            modelTextures.put(model, texture);
            System.out.println("Texture loaded for model: " + texturePath);
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + texturePath);
            modelTextures.remove(model);
        }
    }

    public static void renderScene(
            final GraphicsContext graphicsContext,
            final Scene scene,
            final int width,
            final int height) {

        // Очищаем canvas цветом фона сцены
        Vector3f bgColor = scene.getBackgroundColor();
        graphicsContext.setFill(javafx.scene.paint.Color.color(bgColor.getX(), bgColor.getY(), bgColor.getZ()));
        graphicsContext.fillRect(0, 0, width, height);

        // Рендерим все модели на сцене
        for (int i = 0; i < scene.getModelCount(); i++) {
            ModelInstance modelInstance = scene.getModelInstance(i);
            Model model = modelInstance.getModel();
            Camera camera = scene.getCamera();

            // Получаем цвет из модели
            Color renderColor = modelInstance.getModelColor();
            boolean isSelected = scene.isModelSelected(i);

            // Применяем трансформации из ModelInstance
            Matrix4f modelMatrix = buildModelMatrix(modelInstance);

            // Получаем текстуру для модели (если есть)
            Image texture = modelTextures.get(model);

            // Рендерим модель
            if (wireframeMode) {
                renderWireframe(graphicsContext, camera, model, modelMatrix, renderColor, isSelected, width, height);
            } else if (textureMode && texture != null) {
                renderTextured(graphicsContext, camera, model, modelMatrix, texture, renderColor, isSelected, width, height);
            } else {
                renderSolid(graphicsContext, camera, model, modelMatrix, renderColor, isSelected, width, height);
            }
        }
    }

    // Рендеринг каркаса с поддержкой цвета
    private static void renderWireframe(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final Color renderColor,
            final boolean isSelected,
            final int width,
            final int height) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = multiplyMatrices(modelMatrix, viewMatrix);
        modelViewProjectionMatrix = multiplyMatrices(modelViewProjectionMatrix, projectionMatrix);

        // Устанавливаем цвет и толщину линии из параметров
        graphicsContext.setStroke(renderColor);
        graphicsContext.setLineWidth(isSelected ? 2.0 : 1.0);

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
                Vector3f vertex = mesh.getVertices().get(vertexIndex);

                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                Vector2f resultPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
                resultPoints.add(resultPoint);
            }

            // Рисуем линии
            for (int i = 1; i < nVerticesInPolygon; i++) {
                graphicsContext.strokeLine(
                        resultPoints.get(i - 1).getX(),
                        resultPoints.get(i - 1).getY(),
                        resultPoints.get(i).getX(),
                        resultPoints.get(i).getY());
            }

            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).getX(),
                        resultPoints.get(nVerticesInPolygon - 1).getY(),
                        resultPoints.get(0).getX(),
                        resultPoints.get(0).getY());
            }
        }
    }

    // Рендеринг с текстурами
    private static void renderTextured(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final Image texture,
            final Color renderColor,
            final boolean isSelected,
            final int width,
            final int height) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f modelViewMatrix = multiplyMatrices(modelMatrix, viewMatrix);
        Matrix4f modelViewProjectionMatrix = multiplyMatrices(modelViewMatrix, projectionMatrix);

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Vector2f> screenPoints = new ArrayList<>();
            ArrayList<Vector2f> textureCoords = new ArrayList<>();

            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();

            // Собираем вершины и текстурные координаты
            for (int i = 0; i < nVerticesInPolygon; i++) {
                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(i);
                Vector3f vertex = mesh.getVertices().get(vertexIndex);

                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                Vector2f screenPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
                screenPoints.add(screenPoint);

                // Текстурные координаты (если есть)
                if (i < mesh.getPolygons().get(polygonInd).getTextureVertexIndices().size()) {
                    int texIndex = mesh.getPolygons().get(polygonInd).getTextureVertexIndices().get(i);
                    if (texIndex < mesh.getTextureVertices().size()) {
                        Vector2f texCoord = mesh.getTextureVertices().get(texIndex);
                        textureCoords.add(texCoord);
                    }
                }
            }

            // Рисуем полигон с текстурой
            if (screenPoints.size() >= 3) {
                double[] xPoints = new double[screenPoints.size()];
                double[] yPoints = new double[screenPoints.size()];

                for (int i = 0; i < screenPoints.size(); i++) {
                    xPoints[i] = screenPoints.get(i).getX();
                    yPoints[i] = screenPoints.get(i).getY();
                }

                // Если есть текстура, используем ее для заливки
                if (texture != null) {
                    graphicsContext.setFill(renderColor);
                    graphicsContext.fillPolygon(xPoints, yPoints, screenPoints.size());
                } else {
                    // Без текстуры - просто заливаем цветом
                    graphicsContext.setFill(renderColor);
                    graphicsContext.fillPolygon(xPoints, yPoints, screenPoints.size());
                }

                // Рисуем контур
                graphicsContext.setStroke(renderColor.darker());
                graphicsContext.setLineWidth(isSelected ? 2.0 : 1);
                graphicsContext.strokePolygon(xPoints, yPoints, screenPoints.size());
            }
        }
    }

    // Рендеринг с освещением
    private static void renderSolid(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final Color renderColor,
            final boolean isSelected,
            final int width,
            final int height) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f modelViewMatrix = multiplyMatrices(modelMatrix, viewMatrix);
        Matrix4f modelViewProjectionMatrix = multiplyMatrices(modelViewMatrix, projectionMatrix);

        // Преобразуем направление света в пространство камеры
        Vector3f lightDirInCameraSpace = transformDirection(modelViewMatrix, lightDirection);
        lightDirInCameraSpace = lightDirInCameraSpace.normalizeV();

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            // Триангулируем полигон
            ArrayList<Triangle> triangles = triangulatePolygon(mesh, polygonInd);

            for (Triangle triangle : triangles) {
                ArrayList<Vector2f> screenPoints = new ArrayList<>();
                ArrayList<Vector3f> cameraSpacePoints = new ArrayList<>();

                // Преобразуем вершины треугольника
                for (int i = 0; i < 3; i++) {
                    Vector3f vertex = triangle.vertices[i];

                    // В пространство камеры (для вычисления нормали)
                    Vector3f cameraSpaceVertex = multiplyMatrix4ByVector3(modelViewMatrix, vertex);
                    cameraSpacePoints.add(cameraSpaceVertex);

                    // В экранные координаты (для отрисовки)
                    Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                    Vector2f screenPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
                    screenPoints.add(screenPoint);
                }

                // Вычисляем нормаль треугольника
                Vector3f edge1 = cameraSpacePoints.get(1).sub(cameraSpacePoints.get(0));
                Vector3f edge2 = cameraSpacePoints.get(2).sub(cameraSpacePoints.get(0));
                Vector3f normal = edge1.cross(edge2);
                normal = normal.normalizeV();

                // Вычисляем интенсивность освещения
                float polygonIntensity = 1.0f;
                if (lightingMode) {
                    float diffuse = Math.max(0, normal.dot(lightDirInCameraSpace));
                    polygonIntensity = ambientIntensity + diffuse * diffuseIntensity;
                    polygonIntensity = Math.max(0.2f, Math.min(1.0f, polygonIntensity));
                }

                // Рисуем треугольник
                if (screenPoints.size() == 3) {
                    double[] xPoints = new double[3];
                    double[] yPoints = new double[3];

                    for (int i = 0; i < 3; i++) {
                        xPoints[i] = screenPoints.get(i).getX();
                        yPoints[i] = screenPoints.get(i).getY();
                    }

                    // Применяем освещение к цвету модели
                    Color finalColor = renderColor;
                    if (lightingMode) {
                        finalColor = Color.color(
                                Math.min(1.0, renderColor.getRed() * polygonIntensity),
                                Math.min(1.0, renderColor.getGreen() * polygonIntensity),
                                Math.min(1.0, renderColor.getBlue() * polygonIntensity)
                        );
                    }

                    // Заливка треугольника
                    graphicsContext.setFill(finalColor);
                    graphicsContext.fillPolygon(xPoints, yPoints, 3);

                    // Рисуем контур
                    graphicsContext.setStroke(finalColor.darker());
                    graphicsContext.setLineWidth(isSelected ? 2.0 : 1);
                    graphicsContext.strokePolygon(xPoints, yPoints, 3);
                }
            }
        }
    }

    private static ArrayList<Triangle> triangulatePolygon(Model mesh, int polygonIndex) {
        ArrayList<Triangle> triangles = new ArrayList<>();

        ArrayList<Integer> vertexIndices = mesh.getPolygons().get(polygonIndex).getVertexIndices();
        int nVertices = vertexIndices.size();

        if (nVertices == 3) {
            Triangle triangle = new Triangle(
                    mesh.getVertices().get(vertexIndices.get(0)),
                    mesh.getVertices().get(vertexIndices.get(1)),
                    mesh.getVertices().get(vertexIndices.get(2))
            );
            triangles.add(triangle);
        } else if (nVertices >= 3) {
            Vector3f firstVertex = mesh.getVertices().get(vertexIndices.get(0));

            for (int i = 1; i < nVertices - 1; i++) {
                Triangle triangle = new Triangle(
                        firstVertex,
                        mesh.getVertices().get(vertexIndices.get(i)),
                        mesh.getVertices().get(vertexIndices.get(i + 1))
                );
                triangles.add(triangle);
            }
        }

        return triangles;
    }

    private static class Triangle {
        Vector3f[] vertices;

        Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
            vertices = new Vector3f[3];
            vertices[0] = v1;
            vertices[1] = v2;
            vertices[2] = v3;
        }
    }

    // Преобразование направления
    private static Vector3f transformDirection(Matrix4f matrix, Vector3f direction) {
        float[][] m = matrix.getElements();
        float x = direction.getX();
        float y = direction.getY();
        float z = direction.getZ();

        float resultX = m[0][0] * x + m[0][1] * y + m[0][2] * z;
        float resultY = m[1][0] * x + m[1][1] * y + m[1][2] * z;
        float resultZ = m[2][0] * x + m[2][1] * y + m[2][2] * z;

        return new Vector3f(resultX, resultY, resultZ);
    }

    private static Matrix4f buildModelMatrix(ModelInstance modelInstance) {
        TransformationParameters params = modelInstance.getTransformationParams();

        // Масштабирование
        Matrix4f scaleMatrix = new Matrix4f(1);
        scaleMatrix.getElements()[0][0] = (float) params.getScaleX();
        scaleMatrix.getElements()[1][1] = (float) params.getScaleY();
        scaleMatrix.getElements()[2][2] = (float) params.getScaleZ();

        // Вращение вокруг X
        Matrix4f rotationX = new Matrix4f(1);
        float cosAlpha = (float) Math.cos(Math.toRadians(params.getAlpha()));
        float sinAlpha = (float) Math.sin(Math.toRadians(params.getAlpha()));
        rotationX.getElements()[1][1] = cosAlpha;
        rotationX.getElements()[1][2] = -sinAlpha;
        rotationX.getElements()[2][1] = sinAlpha;
        rotationX.getElements()[2][2] = cosAlpha;

        // Вращение вокруг Y
        Matrix4f rotationY = new Matrix4f(1);
        float cosBeta = (float) Math.cos(Math.toRadians(params.getBeta()));
        float sinBeta = (float) Math.sin(Math.toRadians(params.getBeta()));
        rotationY.getElements()[0][0] = cosBeta;
        rotationY.getElements()[0][2] = sinBeta;
        rotationY.getElements()[2][0] = -sinBeta;
        rotationY.getElements()[2][2] = cosBeta;

        // Вращение вокруг Z
        Matrix4f rotationZ = new Matrix4f(1);
        float cosGamma = (float) Math.cos(Math.toRadians(params.getGamma()));
        float sinGamma = (float) Math.sin(Math.toRadians(params.getGamma()));
        rotationZ.getElements()[0][0] = cosGamma;
        rotationZ.getElements()[0][1] = -sinGamma;
        rotationZ.getElements()[1][0] = sinGamma;
        rotationZ.getElements()[1][1] = cosGamma;

        // Перемещение
        Matrix4f translationMatrix = new Matrix4f(1);
        translationMatrix.getElements()[0][3] = (float) params.getTranslationX();
        translationMatrix.getElements()[1][3] = (float) params.getTranslationY();
        translationMatrix.getElements()[2][3] = (float) params.getTranslationZ();

        // Комбинируем трансформации: T * Rz * Ry * Rx * S
        Matrix4f result = new Matrix4f(1);
        result = multiplyMatrices(result, translationMatrix);
        result = multiplyMatrices(result, rotationZ);
        result = multiplyMatrices(result, rotationY);
        result = multiplyMatrices(result, rotationX);
        result = multiplyMatrices(result, scaleMatrix);

        return result;
    }

    private static Matrix4f multiplyMatrices(Matrix4f a, Matrix4f b) {
        Matrix4f result = new Matrix4f();
        float[][] aData = a.getElements();
        float[][] bData = b.getElements();
        float[][] rData = result.getElements();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                rData[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    rData[i][j] += aData[i][k] * bData[k][j];
                }
            }
        }

        return result;
    }

    private static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vector) {
        float[][] m = matrix.getElements();
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();

        float resultX = m[0][0] * x + m[0][1] * y + m[0][2] * z + m[0][3];
        float resultY = m[1][0] * x + m[1][1] * y + m[1][2] * z + m[1][3];
        float resultZ = m[2][0] * x + m[2][1] * y + m[2][2] * z + m[2][3];
        float resultW = m[3][0] * x + m[3][1] * y + m[3][2] * z + m[3][3];

        if (Math.abs(resultW) > 1e-9) {
            resultX /= resultW;
            resultY /= resultW;
            resultZ /= resultW;
        }

        return new Vector3f(resultX, resultY, resultZ);
    }
// Старый метод render (оставлен для совместимости)
//    public static void render(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height) {
//        Matrix4f modelMatrix = new Matrix4f(1);
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
//        modelViewProjectionMatrix.multiply(viewMatrix);
//        modelViewProjectionMatrix.multiply(projectionMatrix);
//
//        final int nPolygons = mesh.getPolygons().size();
//        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
//            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();
//
//            ArrayList<Vector2f> resultPoints = new ArrayList<>();
//            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
//                Vector3f vertex = mesh.getVertices().get(vertexIndex - 1);
//
//                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
//                Vector2f resultPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
//                resultPoints.add(resultPoint);
//            }
//
//            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(vertexInPolygonInd - 1).getX(),
//                        resultPoints.get(vertexInPolygonInd - 1).getY(),
//                        resultPoints.get(vertexInPolygonInd).getX(),
//                        resultPoints.get(vertexInPolygonInd).getY());
//            }
//
//            if (nVerticesInPolygon > 0) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(nVerticesInPolygon - 1).getX(),
//                        resultPoints.get(nVerticesInPolygon - 1).getY(),
//                        resultPoints.get(0).getX(),
//                        resultPoints.get(0).getY());
//            }
//        }
//    }

//    private static void renderModelWithTransformations(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final Matrix4f modelMatrix,
//            final int width,
//            final int height) {
//
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = multiplyMatrices(modelMatrix, viewMatrix);
//        modelViewProjectionMatrix = multiplyMatrices(modelViewProjectionMatrix, projectionMatrix);
//
//        final int nPolygons = mesh.getPolygons().size();
//        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
//            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();
//
//            ArrayList<Vector2f> resultPoints = new ArrayList<>();
//            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
//                Vector3f vertex = mesh.getVertices().get(vertexIndex);
//
//                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
//                Vector2f resultPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
//                resultPoints.add(resultPoint);
//            }
//
//            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(vertexInPolygonInd - 1).getX(),
//                        resultPoints.get(vertexInPolygonInd - 1).getY(),
//                        resultPoints.get(vertexInPolygonInd).getX(),
//                        resultPoints.get(vertexInPolygonInd).getY());
//            }
//
//            if (nVerticesInPolygon > 0) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(nVerticesInPolygon - 1).getX(),
//                        resultPoints.get(nVerticesInPolygon - 1).getY(),
//                        resultPoints.get(0).getX(),
//                        resultPoints.get(0).getY());
//            }
//        }
//    }

// Метод для вычисления матрицы нормалей
//    private static Matrix4f calculateNormalMatrix(Matrix4f modelViewMatrix) {
//        Matrix4f normalMatrix = new Matrix4f(1);
//        float[][] m = modelViewMatrix.getElements();
//        float[][] n = normalMatrix.getElements();
//
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                n[i][j] = m[i][j];
//            }
//        }
//
//        return normalMatrix;
//    }
}