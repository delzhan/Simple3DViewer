package com.cgvsu.triangulation;

public class Triangulator {
    public TriangulatedModel triangulate(NonTriangulatedModel model) {
        // Пока простая реализация - возвращаем пустую модель
        return new TriangulatedModel(
                model.getVertices(),
                model.getTextureVertices(),
                model.getNormals(),
                model.getName(),
                model.getPolygons()
        );
    }
}