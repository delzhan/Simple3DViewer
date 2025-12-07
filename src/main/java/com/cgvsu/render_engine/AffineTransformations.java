package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Класс для аффинных преобразований (перемещение, вращение, масштабирование)
 * Я едва понимаю,  что именно творю, без осуждения. оно живое и уже хорошо.
 */
public class AffineTransformations {

    /**
     * Создает матрицу вращения вокруг осей X, Y, Z (углы Эйлера)
     * Порядок вращения: Z * Y * X (стандартный для 3D графики)
     *
     * @param angleX угол вращения вокруг оси X (в градусах)
     * @param angleY угол вращения вокруг оси Y (в градусах)
     * @param angleZ угол вращения вокруг оси Z (в градусах)
     * @return матрица вращения 4x4
     */
    public static Matrix4f rotate(float angleX, float angleY, float angleZ) {
        // конверт градусы в радианы
        float radX = (float) Math.toRadians(angleX);
        float radY = (float) Math.toRadians(angleY);
        float radZ = (float) Math.toRadians(angleZ);

        // вычисление синусы и косинусы
        float cosX = (float) cos(radX);
        float sinX = (float) sin(radX);
        float cosY = (float) cos(radY);
        float sinY = (float) sin(radY);
        float cosZ = (float) cos(radZ);
        float sinZ = (float) sin(radZ);

        // Матрица вращения вокруг X
        Matrix4f rotX = new Matrix4f(
                1,     0,      0,    0,
                0,     cosX,   sinX, 0,
                0,    -sinX,   cosX, 0,
                0,     0,      0,    1
        );

        // Матрица вращения вокруг Y
        Matrix4f rotY = new Matrix4f(
                cosY,  0,    -sinY, 0,
                0,     1,     0,    0,
                sinY,  0,     cosY, 0,
                0,     0,     0,    1
        );

        // Матрица вращения вокруг Z
        Matrix4f rotZ = new Matrix4f(
                cosZ,  sinZ,  0,    0,
                -sinZ, cosZ,  0,    0,
                0,     0,     1,    0,
                0,     0,     0,    1
        );

        // умножение: Z * Y * X
        Matrix4f result = rotZ.multiplyNew(rotY);
        result = result.multiplyNew(rotX);

        return result;
    }

    /**
     * Создает матрицу вращения вокруг произвольной оси
     * @return матрица вращения 4x4
     */
    public static Matrix4f rotateAroundAxis(Vector3f axis, float angle) {
        // Нормализуем ось на всякий случай
        axis = axis.normalize();
        float rad = (float) Math.toRadians(angle);

        float cosA = (float) cos(rad);
        float sinA = (float) sin(rad);
        float oneMinusCosA = 1 - cosA;

        float x = axis.x;
        float y = axis.y;
        float z = axis.z;

        // фрмула Родригеса для вращения вокруг произвольной оси
        //я почти перестаю понимать, что я творю, но работает - не трогай
        return new Matrix4f(
                cosA + x*x*oneMinusCosA,     x*y*oneMinusCosA - z*sinA, x*z*oneMinusCosA + y*sinA, 0,
                y*x*oneMinusCosA + z*sinA,   cosA + y*y*oneMinusCosA,   y*z*oneMinusCosA - x*sinA, 0,
                z*x*oneMinusCosA - y*sinA,   z*y*oneMinusCosA + x*sinA, cosA + z*z*oneMinusCosA,   0,
                0,                           0,                         0,                         1
        );
    }

    /**
     * Создает матрицу перемещения
     */
    public static Matrix4f translate(float tx, float ty, float tz) {
        return new Matrix4f(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    /**
     * Создает матрицу перемещения из вектора
     */
    public static Matrix4f translate(Vector3f translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    /**
     * Создает матрицу масштабирования
     */
    public static Matrix4f scale(float scaleX, float scaleY, float scaleZ) {
        return new Matrix4f(
                scaleX, 0,      0,      0,
                0,      scaleY, 0,      0,
                0,      0,      scaleZ, 0,
                0,      0,      0,      1
        );
    }

    /**
     * Создает матрицу масштабирования из вектора
     */
    public static Matrix4f scale(Vector3f scale) {
        return scale(scale.x, scale.y, scale.z);
    }

    /**
     * Создает единую матрицу преобразования
     */
    public static Matrix4f createTransformMatrix(
            Vector3f translation,
            Vector3f rotation,
            Vector3f scale
    ) {
        Matrix4f scaleMat = scale(scale.x, scale.y, scale.z);
        Matrix4f rotationMat = rotate(rotation.x, rotation.y, rotation.z);
        Matrix4f translationMat = translate(translation.x, translation.y, translation.z);

        // Порядок: M = T * R * S
        Matrix4f result = translationMat.multiplyNew(rotationMat);
        result = result.multiplyNew(scaleMat);

        return result;
    }

    /**
     * Создает матрицу вида (look-at матрицу) для камеры
     */
    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f zAxis = eye.sub(target).normalize();  // Направление "вперед"
        Vector3f xAxis = up.cross(zAxis).normalize();  // Направление "вправо"
        Vector3f yAxis = zAxis.cross(xAxis);           // Направление "вверх"

        // Матрица вида
        return new Matrix4f(
                xAxis.x, xAxis.y, xAxis.z, -xAxis.dot(eye),
                yAxis.x, yAxis.y, yAxis.z, -yAxis.dot(eye),
                zAxis.x, zAxis.y, zAxis.z, -zAxis.dot(eye),
                0,       0,       0,       1
        );
    }

    /**
     * Создает матрицу перспективной проекции
     */
    public static Matrix4f perspective(float fov, float aspect, float near, float far) {
        float fovRad = (float) Math.toRadians(fov);
        float f = 1.0f / (float) Math.tan(fovRad / 2.0f);
        float rangeInv = 1.0f / (near - far);

        return new Matrix4f(
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (near + far) * rangeInv, 2 * near * far * rangeInv,
                0, 0, -1, 0
        );
    }

    /**
     * Создает матрицу ортографической проекции
     */
    public static Matrix4f orthographic(
            float left, float right,
            float bottom, float top,
            float near, float far
    ) {
        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        return new Matrix4f(
                2/(right-left), 0, 0, tx,
                0, 2/(top-bottom), 0, ty,
                0, 0, -2/(far-near), tz,
                0, 0, 0, 1
        );
    }

    /**
     * Вспомогательный метод для интерполяции между двумя вращениями
     */
    public static Vector3f lerpRotation(Vector3f start, Vector3f end, float t) {
        // Простая линейная интерполяция каждого угла
        float x = start.x + (end.x - start.x) * t;
        float y = start.y + (end.y - start.y) * t;
        float z = start.z + (end.z - start.z) * t;

        return new Vector3f(x, y, z);
    }

    /**
     * Преобразует матрицу вращения обратно в углы Эйлера (в градусах)
     * Z * Y * X!!!!!!!!!
     */
    public static Vector3f matrixToEulerAngles(Matrix4f rotationMatrix) {
        // Извлекаем элементы матрицы
        float m00 = rotationMatrix.getElement(0, 0);
        float m01 = rotationMatrix.getElement(0, 1);
        float m02 = rotationMatrix.getElement(0, 2);
        float m10 = rotationMatrix.getElement(1, 0);
        float m11 = rotationMatrix.getElement(1, 1);
        float m12 = rotationMatrix.getElement(1, 2);
        float m20 = rotationMatrix.getElement(2, 0);
        float m21 = rotationMatrix.getElement(2, 1);
        float m22 = rotationMatrix.getElement(2, 2);

        // вычисление углов Эйлера
        float yaw, pitch, roll;

        if (Math.abs(m20) < 0.9999999f) {
            yaw = (float) Math.atan2(m10, m00);
            pitch = (float) Math.asin(-m20);
            roll = (float) Math.atan2(m21, m22);
        } else {
            //вертикально
            yaw = 0;
            pitch = (float) (Math.PI / 2 * -Math.signum(m20));
            roll = (float) Math.atan2(-m01, m11);
        }

        // конверт в градусы
        return new Vector3f(
                (float) Math.toDegrees(pitch),
                (float) Math.toDegrees(yaw),
                (float) Math.toDegrees(roll)
        );
    }
}