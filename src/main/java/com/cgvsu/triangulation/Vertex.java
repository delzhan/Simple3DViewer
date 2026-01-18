package com.cgvsu.triangulation;

public class Vertex {
    public float x, y, z;
    public int color;

    public Vertex(float x, float y, float z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public Vertex(float x, float y, float z) {
        this(x, y, z, 0xFFFFFFFF);
    }
}