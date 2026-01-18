package com.cgvsu.triangulation;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelLoader {
    public static TriangulatedModel loadModel(String filename) throws IOException, ObjReaderException {
        // Загружаем модель как NonTriangulatedModel
        Model model = loadModelFromObj(filename);

        // Триангулируем модель
        TriangulatedModel triangulatedModel = triangulateModel(model);

        // Вычисляем нормали вершин
        calculateVertexNormals(triangulatedModel);

        return triangulatedModel;
    }

    private static Model loadModelFromObj(String filename) throws IOException, ObjReaderException {
        System.out.println("Loading model from: " + filename);

        // Читаем содержимое файла
        String fileContent = Files.readString(Path.of(filename));

        // Используем существующий ObjReader для загрузки модели
        return ObjReader.read(fileContent);
    }

    private static TriangulatedModel triangulateModel(Model model) {
        // Создаем новый TriangulatedModel
        TriangulatedModel triangulatedModel = new TriangulatedModel();

        // Копируем базовые данные
        triangulatedModel.setVertices(model.getVertices());
        triangulatedModel.setTextureVertices(model.getTextureVertices());
        triangulatedModel.setNormals(model.getNormals());

        // Триангулируем полигоны
        List<Polygon> triangles = new ArrayList<>();
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            int vertexCount = vertexIndices.size();

            // Если полигон уже треугольник, просто добавляем его
            if (vertexCount == 3) {
                triangles.add(polygon);
            }
            // Если полигон имеет более 3 вершин, используем веерную триангуляцию
            else if (vertexCount > 3) {
                for (int i = 1; i < vertexCount - 1; i++) {
                    Polygon triangle = new Polygon();

                    // Вершины треугольника
                    List<Integer> triangleVertexIndices = new ArrayList<>();
                    triangleVertexIndices.add(vertexIndices.get(0));
                    triangleVertexIndices.add(vertexIndices.get(i));
                    triangleVertexIndices.add(vertexIndices.get(i + 1));
                    triangle.setVertexIndices((ArrayList<Integer>) triangleVertexIndices);

                    // Текстурные координаты (если есть)
                    if (textureIndices != null && textureIndices.size() == vertexCount) {
                        List<Integer> triangleTextureIndices = new ArrayList<>();
                        triangleTextureIndices.add(textureIndices.get(0));
                        triangleTextureIndices.add(textureIndices.get(i));
                        triangleTextureIndices.add(textureIndices.get(i + 1));
                        triangle.setTextureVertexIndices((ArrayList<Integer>) triangleTextureIndices);
                    }

                    // Нормали (если есть)
                    if (normalIndices != null && normalIndices.size() == vertexCount) {
                        List<Integer> triangleNormalIndices = new ArrayList<>();
                        triangleNormalIndices.add(normalIndices.get(0));
                        triangleNormalIndices.add(normalIndices.get(i));
                        triangleNormalIndices.add(normalIndices.get(i + 1));
                        triangle.setNormalIndices((ArrayList<Integer>) triangleNormalIndices);
                    }

                    triangles.add(triangle);
                }
            }
        }

        triangulatedModel.setPolygons(triangles);
        return triangulatedModel;
    }

    private static void calculateVertexNormals(TriangulatedModel model) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();

        // Инициализируем нормали нулевыми векторами
        List<Vector3f> normals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3f(0.0f, 0.0f, 0.0f));
        }

        // Для каждого треугольника вычисляем нормаль и добавляем к каждой вершине
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() != 3) {
                continue;
            }

            // Получаем вершины треугольника
            Vector3f v0 = vertices.get(vertexIndices.get(0));
            Vector3f v1 = vertices.get(vertexIndices.get(1));
            Vector3f v2 = vertices.get(vertexIndices.get(2));

            // Вычисляем нормаль треугольника
            Vector3f edge1 = v1.sub(v0);
            Vector3f edge2 = v2.sub(v0);
            Vector3f normal = edge1.cross(edge2).normalizeV();

            // Добавляем нормаль к каждой вершине треугольника
            for (int i = 0; i < 3; i++) {
                int vertexIndex = vertexIndices.get(i);
                Vector3f currentNormal = normals.get(vertexIndex);
                normals.set(vertexIndex, currentNormal.add(normal));
            }
        }

        // Нормализуем все нормали
        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);
            float length = (float) Math.sqrt(normal.getX() * normal.getX() +
                    normal.getY() * normal.getY() +
                    normal.getZ() * normal.getZ());
            if (length > 0) {
                normals.set(i, new Vector3f(
                        normal.getX() / length,
                        normal.getY() / length,
                        normal.getZ() / length
                ));
            }
        }

        // Устанавливаем нормали в модель
        model.setNormals(normals);

        // Устанавливаем индексы нормалей для полигонов
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            polygon.setNormalIndices(new ArrayList<>(vertexIndices));
        }
    }

    // Создаем тестовый куб (для тестирования)
    private static TriangulatedModel createTestCubeModel() {
        // Вершины куба
        List<Vector3f> vertices = Arrays.asList(
                new Vector3f(-1.0f, -1.0f,  1.0f),  // 0
                new Vector3f( 1.0f, -1.0f,  1.0f),  // 1
                new Vector3f( 1.0f,  1.0f,  1.0f),  // 2
                new Vector3f(-1.0f,  1.0f,  1.0f),  // 3
                new Vector3f(-1.0f, -1.0f, -1.0f),  // 4
                new Vector3f( 1.0f, -1.0f, -1.0f),  // 5
                new Vector3f( 1.0f,  1.0f, -1.0f),  // 6
                new Vector3f(-1.0f,  1.0f, -1.0f)   // 7
        );

        // Текстурные координаты (пока пустые)
        List<Vector2f> textureVertices = new ArrayList<>();

        // Нормали (будут вычислены)
        List<Vector3f> normals = new ArrayList<>();

        // Полигоны куба (треугольники)
        List<Polygon> polygons = new ArrayList<>();

        // Индексы вершин для граней куба
        int[][] cubeFaces = {
                {0, 1, 2, 3},  // передняя
                {1, 5, 6, 2},  // правая
                {5, 4, 7, 6},  // задняя
                {4, 0, 3, 7},  // левая
                {3, 2, 6, 7},  // верхняя
                {4, 5, 1, 0}   // нижняя
        };

        // Создаем треугольники для каждой грани
        for (int[] face : cubeFaces) {
            // Первый треугольник грани
            Polygon triangle1 = new Polygon();
            triangle1.setVertexIndices(new ArrayList<>(Arrays.asList(
                    face[0], face[1], face[2]
            )));
            polygons.add(triangle1);

            // Второй треугольник грани
            Polygon triangle2 = new Polygon();
            triangle2.setVertexIndices(new ArrayList<>(Arrays.asList(
                    face[0], face[2], face[3]
            )));
            polygons.add(triangle2);
        }

        TriangulatedModel model = new TriangulatedModel(
                vertices, textureVertices, normals, "Test Cube", polygons
        );

        // Вычисляем нормали
        calculateVertexNormals(model);

        return model;
    }
}