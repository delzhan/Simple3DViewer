package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NormalCalculator {

    public static void calculateNormals(TriangulatedModel model) {
        List<Float> vertices = model.getVertices();
        List<Polygon> triangles = model.getPolygons();
        List<Float> normals = new ArrayList<>();

        // Инициализируем нормали нулями
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(0.0f);
        }

        for (Polygon triangle : triangles) {
            List<Integer> vertexIndices = triangle.getVertexIndices();

            Vector3f v0 = getVertex(vertices, vertexIndices.get(0));
            Vector3f v1 = getVertex(vertices, vertexIndices.get(1));
            Vector3f v2 = getVertex(vertices, vertexIndices.get(2));

            Vector3f normal = calculateTriangleNormal(v0, v1, v2);

            for (int vertexIndex : vertexIndices) {
                int baseIndex = vertexIndex * 3;
                normals.set(baseIndex, normals.get(baseIndex) + normal.getX());
                normals.set(baseIndex + 1, normals.get(baseIndex + 1) + normal.getY());
                normals.set(baseIndex + 2, normals.get(baseIndex + 2) + normal.getZ());
            }
        }

        // Нормализуем
        for (int i = 0; i < normals.size(); i += 3) {
            float x = normals.get(i);
            float y = normals.get(i + 1);
            float z = normals.get(i + 2);

            float length = (float) Math.sqrt(x * x + y * y + z * z);
            if (length > 0) {
                normals.set(i, x / length);
                normals.set(i + 1, y / length);
                normals.set(i + 2, z / length);
            }
        }

        model.setNormals(normals);
    }

    private static Vector3f getVertex(List<Float> vertices, int index) {
        return new Vector3f(
                vertices.get(index * 3),
                vertices.get(index * 3 + 1),
                vertices.get(index * 3 + 2)
        );
    }

    private static Vector3f calculateTriangleNormal(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f u = v1.sub(v0);
        Vector3f vVec = v2.sub(v0);

        Vector3f normal = u.cross(vVec);
        return normal;
    }
}