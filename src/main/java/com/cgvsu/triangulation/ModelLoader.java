//package com.cgvsu.triangulation;
//
//import com.cgvsu.model.Polygon;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class ModelLoader {
//    public static TriangulatedModel loadModel(String filename) throws IOException {
//        // Загружаем модель как NonTriangulatedModel
//        NonTriangulatedModel nonTriangulatedModel = loadModelFromObj(filename);
//
//        // Используем базовую триангуляцию
//        Triangulator triangulator = new Triangulator();
//        TriangulatedModel triangulatedModel = triangulator.triangulate(nonTriangulatedModel);
//
//        // Вычисляем нормали вершин
//        calculateVertexNormals(triangulatedModel);
//
//        return triangulatedModel;
//    }
//
//    private static NonTriangulatedModel loadModelFromObj(String filename) throws IOException {
//        System.out.println("Loading model from: " + filename);
//
//        // Временная заглушка: создаем тестовую модель куба
//        return createTestCubeModel();
//    }
//
//    private static NonTriangulatedModel createTestCubeModel() {
//        // Вершины куба
//        List<Float> vertices = Arrays.asList(
//                // Передняя грань
//                -1.0f, -1.0f,  1.0f,  // 0
//                1.0f, -1.0f,  1.0f,  // 1
//                1.0f,  1.0f,  1.0f,  // 2
//                -1.0f,  1.0f,  1.0f,  // 3
//
//                // Задняя грань
//                -1.0f, -1.0f, -1.0f,  // 4
//                1.0f, -1.0f, -1.0f,  // 5
//                1.0f,  1.0f, -1.0f,  // 6
//                -1.0f,  1.0f, -1.0f   // 7
//        );
//
//        // Текстурные координаты (пока пустые)
//        List<Float> textureVertices = new ArrayList<>();
//
//        // Нормали (пока пустые, будут вычислены позже)
//        List<Float> normals = new ArrayList<>();
//
//        // Полигоны куба (каждая грань состоит из 2 треугольников)
//        List<Polygon> polygons = new ArrayList<>();
//
//        // Индексы вершин для граней куба
//        int[][] cubeFaces = {
//                {0, 1, 2, 3},  // передняя
//                {1, 5, 6, 2},  // правая
//                {5, 4, 7, 6},  // задняя
//                {4, 0, 3, 7},  // левая
//                {3, 2, 6, 7},  // верхняя
//                {4, 5, 1, 0}   // нижняя
//        };
//
//        // Создаем треугольники для каждой грани
//        for (int[] face : cubeFaces) {
//            // Первый треугольник грани
//            Polygon triangle1 = new Polygon();
//            triangle1.setVertexIndices(new ArrayList<>(Arrays.asList(
//                    face[0], face[1], face[2]
//            )));
//            polygons.add(triangle1);
//
//            // Второй треугольник грани
//            Polygon triangle2 = new Polygon();
//            triangle2.setVertexIndices(new ArrayList<>(Arrays.asList(
//                    face[0], face[2], face[3]
//            )));
//            polygons.add(triangle2);
//        }
//
//        return new NonTriangulatedModel(vertices, textureVertices, normals, "Cube", polygons);
//    }
//
//    // Остальные методы без изменений...
//    private static void calculateVertexNormals(TriangulatedModel model) {
//        List<Float> vertices = model.getVertices();
//        List<Polygon> polygons = model.getPolygons();
//
//        List<Float> normals = new ArrayList<>();
//        for (int i = 0; i < vertices.size(); i++) {
//            normals.add(0.0f);
//        }
//
//        for (Polygon polygon : polygons) {
//            List<Integer> vertexIndices = polygon.getVertexIndices();
//            if (vertexIndices.size() != 3) {
//                continue;
//            }
//
//            float[] v0 = getVertex(vertices, vertexIndices.get(0));
//            float[] v1 = getVertex(vertices, vertexIndices.get(1));
//            float[] v2 = getVertex(vertices, vertexIndices.get(2));
//
//            float[] normal = calculateTriangleNormal(v0, v1, v2);
//
//            for (int i = 0; i < 3; i++) {
//                int vertexIndex = vertexIndices.get(i);
//                int normalIndex = vertexIndex * 3;
//                float currentX = normals.get(normalIndex);
//                float currentY = normals.get(normalIndex + 1);
//                float currentZ = normals.get(normalIndex + 2);
//
//                normals.set(normalIndex, currentX + normal[0]);
//                normals.set(normalIndex + 1, currentY + normal[1]);
//                normals.set(normalIndex + 2, currentZ + normal[2]);
//            }
//        }
//
//        for (int i = 0; i < normals.size(); i += 3) {
//            float x = normals.get(i);
//            float y = normals.get(i + 1);
//            float z = normals.get(i + 2);
//            float length = (float) Math.sqrt(x * x + y * y + z * z);
//            if (length > 0) {
//                normals.set(i, x / length);
//                normals.set(i + 1, y / length);
//                normals.set(i + 2, z / length);
//            }
//        }
//
//        model.setNormals(normals);
//
//        for (Polygon polygon : polygons) {
//            List<Integer> vertexIndices = polygon.getVertexIndices();
//            ArrayList<Integer> normalIndices = new ArrayList<>(vertexIndices);
//            polygon.setNormalIndices(normalIndices);
//        }
//    }
//
//    private static float[] getVertex(List<Float> vertices, int index) {
//        float[] vertex = new float[3];
//        vertex[0] = vertices.get(index * 3);
//        vertex[1] = vertices.get(index * 3 + 1);
//        vertex[2] = vertices.get(index * 3 + 2);
//        return vertex;
//    }
//
//    private static float[] calculateTriangleNormal(float[] v0, float[] v1, float[] v2) {
//        float[] vector1 = new float[]{v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
//        float[] vector2 = new float[]{v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
//
//        float[] normal = new float[3];
//        normal[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
//        normal[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
//        normal[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];
//
//        float length = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
//        if (length > 0) {
//            normal[0] /= length;
//            normal[1] /= length;
//            normal[2] /= length;
//        }
//
//        return normal;
//    }
//}