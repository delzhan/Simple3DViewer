package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel {
    private List<Vector3f> vertices;
    private List<Vector2f> textureVertices;
    private List<Vector3f> normals;
    private List<Polygon> triangles;
    private String name;

    public TriangulatedModel() {
        this.vertices = new ArrayList<>();
        this.textureVertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.name = "";
    }

    public TriangulatedModel(List<Vector3f> vertices, List<Vector2f> textureVertices,
                             List<Vector3f> normals, String name,
                             List<Polygon> triangles) {
        this.vertices = vertices != null ? new ArrayList<>(vertices) : new ArrayList<>();
        this.textureVertices = textureVertices != null ? new ArrayList<>(textureVertices) : new ArrayList<>();
        this.normals = normals != null ? new ArrayList<>(normals) : new ArrayList<>();
        this.name = name != null ? name : "";
        this.triangles = triangles != null ? new ArrayList<>(triangles) : new ArrayList<>();

        // Проверяем, что все полигоны - треугольники
        for (Polygon polygon : this.triangles) {
            if (!polygon.isTriangle()) {
                throw new IllegalArgumentException("Все полигоны должны быть треугольниками");
            }
        }
    }

    // Геттеры и сеттеры для вершин
    public List<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vector3f> vertices) {
        this.vertices = vertices != null ? new ArrayList<>(vertices) : new ArrayList<>();
    }

    // Геттеры и сеттеры для текстурных координат
    public List<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(List<Vector2f> textureVertices) {
        this.textureVertices = textureVertices != null ? new ArrayList<>(textureVertices) : new ArrayList<>();
    }

    // Геттеры и сеттеры для нормалей
    public List<Vector3f> getNormals() {
        return normals;
    }

    // Два варианта setNormals - выбираем один
    public void setNormals(List<Vector3f> normals) {
        this.normals = normals != null ? new ArrayList<>(normals) : new ArrayList<>();
    }

    // Альтернативный метод для установки нормалей из List<Float>
    public void setNormalsFromFloatList(List<Float> floatNormals) {
        this.normals = new ArrayList<>();
        if (floatNormals != null) {
            for (int i = 0; i < floatNormals.size(); i += 3) {
                if (i + 2 < floatNormals.size()) {
                    normals.add(new Vector3f(
                            floatNormals.get(i),
                            floatNormals.get(i + 1),
                            floatNormals.get(i + 2)
                    ));
                }
            }
        }
    }

    // Геттеры и сеттеры для полигонов (треугольников)
    public List<Polygon> getPolygons() {
        return triangles;
    }

    public void setPolygons(List<Polygon> polygons) {
        if (polygons != null) {
            // Проверяем, что все полигоны - треугольники
            for (Polygon polygon : polygons) {
                if (!polygon.isTriangle()) {
                    throw new IllegalArgumentException("Все полигоны должны быть треугольниками. Найден полигон с " +
                            polygon.getVertexIndices().size() + " вершинами.");
                }
            }
            this.triangles = new ArrayList<>(polygons);
        } else {
            this.triangles = new ArrayList<>();
        }
    }

    // Геттер и сеттер для имени
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    // Методы для удобства
    public boolean isTriangulated() {
        return true;
    }

    public int getTriangleCount() {
        return triangles.size();
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public int getTextureVertexCount() {
        return textureVertices.size();
    }

    public int getNormalCount() {
        return normals.size();
    }

    /**
     * Проверяет, имеет ли модель текстурные координаты
     */
    public boolean hasTextureCoordinates() {
        return !textureVertices.isEmpty();
    }

    /**
     * Проверяет, имеет ли модель нормали
     */
    public boolean hasNormals() {
        return !normals.isEmpty();
    }

    /**
     * Проверяет, является ли модель валидной (имеет вершины и треугольники)
     */
    public boolean isValid() {
        return !vertices.isEmpty() && !triangles.isEmpty();
    }

    /**
     * Получает вершину по индексу (0-based)
     */
    public Vector3f getVertex(int index) {
        if (index < 0 || index >= vertices.size()) {
            throw new IndexOutOfBoundsException("Индекс вершины вне диапазона: " + index);
        }
        return vertices.get(index);
    }

    /**
     * Получает текстуру по индексу (0-based)
     */
    public Vector2f getTextureVertex(int index) {
        if (index < 0 || index >= textureVertices.size()) {
            throw new IndexOutOfBoundsException("Индекс текстуры вне диапазона: " + index);
        }
        return textureVertices.get(index);
    }

    /**
     * Получает нормаль по индексу (0-based)
     */
    public Vector3f getNormal(int index) {
        if (index < 0 || index >= normals.size()) {
            throw new IndexOutOfBoundsException("Индекс нормали вне диапазона: " + index);
        }
        return normals.get(index);
    }

    @Override
    public String toString() {
        return String.format("TriangulatedModel[name='%s', vertices=%d, triangles=%d, texCoords=%d, normals=%d]",
                name, getVertexCount(), getTriangleCount(), getTextureVertexCount(), getNormalCount());
    }
}