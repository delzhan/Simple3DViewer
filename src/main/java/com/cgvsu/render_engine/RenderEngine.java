package com.cgvsu.render_engine;

import java.util.ArrayList;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;

import com.cgvsu.model.Model;
import com.cgvsu.math.Matrix4f;
import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
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

            ArrayList<javax.vecmath.Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                int vertexIndex = mesh.getPolygons().get(polygonInd).getVertexIndices().get(vertexInPolygonInd);
                Vector3f vertex = mesh.getVertices().get(vertexIndex - 1);

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(
                        vertex.getX(),
                        vertex.getY(),
                        vertex.getZ());

                javax.vecmath.Matrix4f matrix4f = convertToVecmathMatrix4f(modelViewProjectionMatrix);

                javax.vecmath.Point2f resultPoint = vertexToPoint(
                        multiplyMatrix4ByVector3(matrix4f, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
            }

            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
        }
    }

    private static javax.vecmath.Matrix4f convertToVecmathMatrix4f(Matrix4f matrix) {
        float[][] elements = matrix.getElements();
        return new javax.vecmath.Matrix4f(
                elements[0][0], elements[0][1], elements[0][2], elements[0][3],
                elements[1][0], elements[1][1], elements[1][2], elements[1][3],
                elements[2][0], elements[2][1], elements[2][2], elements[2][3],
                elements[3][0], elements[3][1], elements[3][2], elements[3][3]);
    }
}