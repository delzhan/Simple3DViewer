package com.cgvsu.triangulation;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Triangulator {
    public TriangulatedModel triangulate(Model model) {
        TriangulatedModel triangulatedModel = new TriangulatedModel();

        // Копируем базовые данные
        triangulatedModel.setVertices(model.getVertices());
        triangulatedModel.setTextureVertices(model.getTextureVertices());
        triangulatedModel.setNormals(model.getNormals());

        // Триангулируем
        List<Polygon> triangles = new ArrayList<>();
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            int vertexCount = vertexIndices.size();

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

                    // Текстурные координаты
                    if (textureIndices != null && textureIndices.size() == vertexCount) {
                        List<Integer> triangleTextureIndices = new ArrayList<>();
                        triangleTextureIndices.add(textureIndices.get(0));
                        triangleTextureIndices.add(textureIndices.get(i));
                        triangleTextureIndices.add(textureIndices.get(i + 1));
                        triangle.setTextureVertexIndices((ArrayList<Integer>) triangleTextureIndices);
                    }

                    // Нормали
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
}