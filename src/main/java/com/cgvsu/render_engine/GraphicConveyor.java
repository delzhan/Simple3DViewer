package com.cgvsu.render_engine;
import javax.vecmath.*;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.render_engine.AffineTransformations;

import static java.lang.Math.*;


public class GraphicConveyor {

    /**
     * Аффинные преобразования.
     * Пока я пыталась моздать все это, я поседела.
     */
    public static Matrix4f scaleRotateTranslate(Vector3f rotate, Vector3f scale, Vector3f translate) {
        Matrix4f t = AffineTransformations.translate(new Vector3f(translate.getX(), translate.getY(), translate.getZ()));
        Matrix4f r = AffineTransformations.rotate(rotate.getX(), rotate.getY(), rotate.getZ());
        Matrix4f s = AffineTransformations.scale(scale.getX(), scale.getY(), scale.getZ());
        return t.multiplyNew(r.multiplyNew(s));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    /**
     * Система корд камеры +перевод всех объектов сцены в систему координат камеры
     */

    private static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX;
        Vector3f resultY;
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX = up.cross(resultZ);
        resultY = resultZ.cross(resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        float[] matrix = new float[]{
                resultX.getX(), resultY.getX(), resultZ.getX(), 0,
                resultX.getY(), resultY.getY(), resultZ.getY(), 0,
                resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1};
        return new Matrix4f(matrix);
    }

    /**
     * Матрица перспективы. я хз что с ней делать, вроде как живое, но я гарантировать не могу
     */

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        float tangentMinusOnDegree = (float) (1.0F / (tan(fov * 0.5F))); // fov задается как половина от всего угла
        float[] res = {
                tangentMinusOnDegree / aspectRatio, 0, 0, 0,
                0, tangentMinusOnDegree, 0, 0,
                0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), 1.0F,
                0, 0, 2 * (nearPlane * farPlane) / (nearPlane - farPlane), 0
        };
        return new Matrix4f(res);
    }

    /**
     * Получаем последний вектор нормализованный вектор v для трансформации вершины и отображения последней на экране
     */
    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        Vector4f res = matrix.multiply(new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1));
        float w = res.getW();
        return new Vector3f(res.getX() / w, res.getY() / w, res.getZ() / w);
    }


    /**
     * Возвращает матрицу модель-представление-проекция.
     */
    public static Matrix4f calculateModelViewProjectionMatrix(Camera camera, Vector3f rotate, Vector3f scale, Vector3f translate) {
        Matrix4f modelMatrix = GraphicConveyor.scaleRotateTranslate(rotate, scale, translate);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        modelMatrix.transposition();
        viewMatrix.transposition();
        projectionMatrix.transposition();

        Matrix4f mvpMatrix = new Matrix4f(projectionMatrix);
        mvpMatrix.multiply(viewMatrix);
        mvpMatrix.multiply(modelMatrix);
        return mvpMatrix;
    }


    /**
     * Преобразует вершину в экранные координаты.
     */
    public static Point2f vertexToPoint(Vector3f vertex, int width, int height) {
        float x = (vertex.getX() + 1) * 0.5f * width;
        float y = (1 - (vertex.getY() + 1) * 0.5f) * height;
        return new Point2f(x, y);
    }
}