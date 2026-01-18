package com.cgvsu.triangulation;

import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseModel {
    protected List<Float> vertices;
    protected List<Float> textureVertices;
    protected List<Float> normals;
    protected String name;

    public BaseModel() {
        vertices = new ArrayList<>();
        textureVertices = new ArrayList<>();
        normals = new ArrayList<>();
        name = "";
    }

    public BaseModel(List<Float> vertices, List<Float> textureVertices,
                     List<Float> normals, String name) {
        this.vertices = vertices;
        this.textureVertices = textureVertices;
        this.normals = normals;
        this.name = name;
    }

    public List<Float> getVertices() {
        return vertices;
    }

    public void setVertices(List<Float> vertices) {
        this.vertices = vertices;
    }

    public List<Float> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(List<Float> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public List<Float> getNormals() {
        return normals;
    }

    public void setNormals(List<Float> normals) {
        this.normals = normals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract List<Polygon> getPolygons();

    public abstract void setPolygons(List<Polygon> polygons);

    public abstract boolean isTriangulated();
}