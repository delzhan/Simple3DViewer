package com.cgvsu.triangulation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Rasterizer {
    private float[][] zBuffer;
    private int[][] colorBuffer;
    private int width;
    private int height;

    public Rasterizer(int width, int height) {
        this.width = width;
        this.height = height;
        this.zBuffer = new float[height][width];
        this.colorBuffer = new int[height][width];
        clearBuffers();
    }

    public void clearBuffers() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                zBuffer[y][x] = Float.MAX_VALUE;
                colorBuffer[y][x] = Color.BLACK.getRGB();
            }
        }
    }

    public void clear() {
        clearBuffers();
    }

    public void rasterizeTriangle(float[] v0, float[] v1, float[] v2,
                                  Color[] colors, float[] zValues) {
        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.min(Math.min(v0[0], v1[0]), v2[0]));
        int maxX = (int) Math.min(width - 1, Math.max(Math.max(v0[0], v1[0]), v2[0]));
        int minY = (int) Math.max(0, Math.min(Math.min(v0[1], v1[1]), v2[1]));
        int maxY = (int) Math.min(height - 1, Math.max(Math.max(v0[1], v1[1]), v2[1]));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float[] p = {x + 0.5f, y + 0.5f};

                // Барицентрические координаты
                float area = edgeFunction(v0, v1, v2);
                float w0 = edgeFunction(v1, v2, p) / area;
                float w1 = edgeFunction(v2, v0, p) / area;
                float w2 = edgeFunction(v0, v1, p) / area;

                if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                    // Интерполяция глубины
                    float z = w0 * zValues[0] + w1 * zValues[1] + w2 * zValues[2];

                    // Z-буфер
                    if (z < zBuffer[y][x]) {
                        zBuffer[y][x] = z;

                        // Интерполяция цвета
                        Color c0 = colors[0];
                        Color c1 = colors[1];
                        Color c2 = colors[2];

                        int r = (int)(w0 * c0.getRed() + w1 * c1.getRed() + w2 * c2.getRed());
                        int g = (int)(w0 * c0.getGreen() + w1 * c1.getGreen() + w2 * c2.getGreen());
                        int b = (int)(w0 * c0.getBlue() + w1 * c1.getBlue() + w2 * c2.getBlue());

                        r = Math.min(255, Math.max(0, r));
                        g = Math.min(255, Math.max(0, g));
                        b = Math.min(255, Math.max(0, b));

                        colorBuffer[y][x] = new Color(r, g, b).getRGB();
                    }
                }
            }
        }
    }

    public void rasterizeTriangle(Vertex v0, Vertex v1, Vertex v2) {
        float[] fv0 = {v0.x, v0.y};
        float[] fv1 = {v1.x, v1.y};
        float[] fv2 = {v2.x, v2.y};
        float[] depths = {v0.z, v1.z, v2.z};
        Color[] colors = {new Color(v0.color), new Color(v1.color), new Color(v2.color)};

        rasterizeTriangle(fv0, fv1, fv2, colors, depths);
    }

    private float edgeFunction(float[] a, float[] b, float[] c) {
        return (c[0] - a[0]) * (b[1] - a[1]) - (c[1] - a[1]) * (b[0] - a[0]);
    }

    public BufferedImage getImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, colorBuffer[y][x]);
            }
        }
        return image;
    }

    public void drawWireframe(float[] v0, float[] v1, float[] v2, Color color) {
        drawLineBresenham(v0, v1, color);
        drawLineBresenham(v1, v2, color);
        drawLineBresenham(v2, v0, color);
    }

    void drawLineBresenham(float[] start, float[] end, Color color) {
        int x0 = (int) start[0];
        int y0 = (int) start[1];
        int x1 = (int) end[0];
        int y1 = (int) end[1];

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                colorBuffer[y0][x0] = color.getRGB();
            }

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}