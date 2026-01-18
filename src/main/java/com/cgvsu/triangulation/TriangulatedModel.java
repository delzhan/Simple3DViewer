package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel extends BaseModel {
    private List<Polygon> triangles;

    public TriangulatedModel() {
        super();
        triangles = new ArrayList<>();
    }

    public TriangulatedModel(List<Float> vertices, List<Float> textureVertices,
                             List<Float> normals, String name,
                             List<Polygon> triangles) {
        super(vertices, textureVertices, normals, name);
        this.triangles = triangles;
    }

    @Override
    public List<Polygon> getPolygons() {
        return triangles;
    }

    @Override
    public void setPolygons(List<Polygon> polygons) {
        for (Polygon polygon : polygons) {
            if (!polygon.isTriangle()) {
                throw new IllegalArgumentException("Все полигоны должны быть треугольниками");
            }
        }
        this.triangles = polygons;
    }

    @Override
    public boolean isTriangulated() {
        return true;
    }

    public int getTriangleCount() {
        return triangles.size();
    }
}