package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NonTriangulatedModel extends BaseModel {
    private List<Polygon> polygons;

    public NonTriangulatedModel() {
        super();
        polygons = new ArrayList<>();
    }

    public NonTriangulatedModel(List<Float> vertices, List<Float> textureVertices,
                                List<Float> normals, String name,
                                List<Polygon> polygons) {
        super(vertices, textureVertices, normals, name);
        this.polygons = polygons;
    }

    @Override
    public List<Polygon> getPolygons() {
        return polygons;
    }

    @Override
    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    @Override
    public boolean isTriangulated() {
        for (Polygon polygon : polygons) {
            if (!polygon.isTriangle()) {
                return false;
            }
        }
        return true;
    }

    public int getPolygonCount() {
        return polygons.size();
    }
}