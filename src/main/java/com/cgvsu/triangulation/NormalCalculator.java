package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;

public class NormalCalculator {
    public static void calculateNormals(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        ArrayList<Vector3f> vertices = model.getVertices();
        ArrayList<Polygon> polygons = model.getPolygons();

        if (vertices == null || vertices.isEmpty() || polygons == null || polygons.isEmpty()) {
            return;
        }

        ArrayList<Vector3f> normals = new ArrayList<>();

        // Инициализируем нормали нулевыми векторами
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0, 0, 0));
        }

        // Рассчитываем нормали для каждого полигона
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() < 3) {
                continue;
            }

            // Берем первые три вершины для расчета нормали
            Vector3f v0 = vertices.get(vertexIndices.get(0));
            Vector3f v1 = vertices.get(vertexIndices.get(1));
            Vector3f v2 = vertices.get(vertexIndices.get(2));

            Vector3f normal = calculateTriangleNormal(v0, v1, v2);

            if (normal == null) {
                continue;
            }

            // Добавляем нормаль ко всем вершинам полигона
            for (int vertexIndex : vertexIndices) {
                if (vertexIndex < 0 || vertexIndex >= normals.size()) {
                    continue;
                }

                Vector3f currentNormal = normals.get(vertexIndex);
                Vector3f updatedNormal = currentNormal.add(normal);
                normals.set(vertexIndex, updatedNormal);
            }
        }

        // Нормализуем векторы
        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);

            float length = (float) Math.sqrt(
                    normal.getX() * normal.getX() +
                            normal.getY() * normal.getY() +
                            normal.getZ() * normal.getZ()
            );

            if (length > 0.0001f) {
                Vector3f normalizedNormal = new Vector3f(
                        normal.getX() / length,
                        normal.getY() / length,
                        normal.getZ() / length
                );
                normals.set(i, normalizedNormal);
            } else {
                normals.set(i, new Vector3f(0, 1, 0));
            }
        }

        model.setNormals(normals);
    }

    //расчет нормали
    private static Vector3f calculateTriangleNormal(Vector3f v0, Vector3f v1, Vector3f v2) {
        if (v0 == null || v1 == null || v2 == null) {
            return null;
        }

        Vector3f u = v1.sub(v0);
        Vector3f vVec = v2.sub(v0);

        Vector3f normal = u.cross(vVec);

        // Проверяем, что нормаль не нулевая
        if (normal.getX() == 0 && normal.getY() == 0 && normal.getZ() == 0) {
            return null;
        }

        // Нормализуем нормаль
        float length = (float) Math.sqrt(
                normal.getX() * normal.getX() +
                        normal.getY() * normal.getY() +
                        normal.getZ() * normal.getZ()
        );

        if (length > 0) {
            return new Vector3f(
                    normal.getX() / length,
                    normal.getY() / length,
                    normal.getZ() / length
            );
        }

        return null;
    }
}