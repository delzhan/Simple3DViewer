package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.model.ModelInstance;
import com.cgvsu.model.Scene;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class RenderEngine {
    //Возможно не понадобится, он нигде не используется, я хз, Настя, почекай пж, нужен ли он, если внизу есть рендерСцене
//    public static void render(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height) {
//        // Создаем единичную матрицу модели
//        Matrix4f modelMatrix = new Matrix4f(1);
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        // Создаем копию modelMatrix и умножаем матрицы
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
//                // Преобразуем вершину с использованием матрицы трансформации
//                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
//
//                // Преобразуем в экранные координаты
//                Vector2f resultPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
//                resultPoints.add(resultPoint);
//            }
//
//            // Рисуем линии между вершинами полигона
//            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(vertexInPolygonInd - 1).getX(),
//                        resultPoints.get(vertexInPolygonInd - 1).getY(),
//                        resultPoints.get(vertexInPolygonInd).getX(),
//                        resultPoints.get(vertexInPolygonInd).getY());
//            }
//
//            // Замыкаем полигон (последняя вершина с первой)
//            if (nVerticesInPolygon > 0) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(nVerticesInPolygon - 1).getX(),
//                        resultPoints.get(nVerticesInPolygon - 1).getY(),
//                        resultPoints.get(0).getX(),
//                        resultPoints.get(0).getY());
//            }
//        }
//    }

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

            // Устанавливаем цвет в зависимости от выбора
            if (scene.isModelSelected(i)) {
                graphicsContext.setStroke(javafx.scene.paint.Color.RED);
                graphicsContext.setLineWidth(2.0);
            } else {
                graphicsContext.setStroke(javafx.scene.paint.Color.WHITE);
                graphicsContext.setLineWidth(1.0);
            }

            // Применяем трансформации из ModelInstance
            Matrix4f modelMatrix = buildModelMatrix(modelInstance);

            // Рендерим модель с учетом её трансформаций
            renderModelWithTransformations(graphicsContext, camera, model, modelMatrix, width, height);
        }
    }

    private static Matrix4f buildModelMatrix(ModelInstance modelInstance) {
        // Получаем параметры трансформации
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

    private static Matrix4f multiplyMatrices(Matrix4f a, Matrix4f b) { // Умножает две матрицы 4x4 и возвращает результат
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

    private static void renderModelWithTransformations( // Рендерит модель с применением матрицы трансформаций
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final Matrix4f modelMatrix,
            final int width,
            final int height) {

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Комбинируем матрицы: Projection * View * Model
        Matrix4f modelViewProjectionMatrix = multiplyMatrices(modelMatrix, viewMatrix);
        modelViewProjectionMatrix = multiplyMatrices(modelViewProjectionMatrix, projectionMatrix);

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.getPolygons().get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
                Vector3f vertex = mesh.getVertices().get(vertexIndex - 1);

                // Преобразуем вершину с использованием матрицы трансформации
                Vector3f transformedVertex = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);

                // Преобразуем в экранные координаты
                Vector2f resultPoint = GraphicConveyor.vertexToPoint(transformedVertex, width, height);
                resultPoints.add(resultPoint);
            }

            // Рисуем линии между вершинами полигона
            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).getX(),
                        resultPoints.get(vertexInPolygonInd - 1).getY(),
                        resultPoints.get(vertexInPolygonInd).getX(),
                        resultPoints.get(vertexInPolygonInd).getY());
            }

            // Замыкаем полигон (последняя вершина с первой)
            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).getX(),
                        resultPoints.get(nVerticesInPolygon - 1).getY(),
                        resultPoints.get(0).getX(),
                        resultPoints.get(0).getY());
            }
        }
    }

    private static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vector) {
        // Умножает матрицу 4x4 на вектор 3D.
        // Вектор расширяется до однородных координат (x, y, z, 1).

        float[][] m = matrix.getElements();
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();

        // Умножаем матрицу на вектор в однородных координатах
        float resultX = m[0][0] * x + m[0][1] * y + m[0][2] * z + m[0][3];
        float resultY = m[1][0] * x + m[1][1] * y + m[1][2] * z + m[1][3];
        float resultZ = m[2][0] * x + m[2][1] * y + m[2][2] * z + m[2][3];
        float resultW = m[3][0] * x + m[3][1] * y + m[3][2] * z + m[3][3];

        // Выполняем перспективное деление
        if (Math.abs(resultW) > 1e-9) {
            resultX /= resultW;
            resultY /= resultW;
            resultZ /= resultW;
        }

        return new Vector3f(resultX, resultY, resultZ);
    }
}