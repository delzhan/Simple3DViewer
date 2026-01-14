package com.cgvsu.render_engine;

import java.util.ArrayList;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;

import com.cgvsu.model.Model;
import com.cgvsu.math.Matrix4f;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {
        // Создаем единичную матрицу модели
        Matrix4f modelMatrix = new Matrix4f(1);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Создаем копию modelMatrix и умножаем матрицы
        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

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

    /**
     * Умножает матрицу 4x4 на вектор 3D.
     * Вектор расширяется до однородных координат (x, y, z, 1).
     */
    private static Vector3f multiplyMatrix4ByVector3(Matrix4f matrix, Vector3f vector) {
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