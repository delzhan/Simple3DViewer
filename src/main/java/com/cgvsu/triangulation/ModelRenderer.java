package com.cgvsu.triangulation;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ModelRenderer {
    private TriangulatedModel model;
    private RenderSettings settings;

    public static class RenderSettings {
        public boolean drawWireframe = false;
        public boolean useTexture = false;
        public boolean useLighting = true;
        public Color staticColor = Color.WHITE;
        public Vector3f cameraPosition = new Vector3f(0, 0, 5);
        public Vector3f cameraTarget = new Vector3f(0, 0, 0);
        public float fov = 60.0f;
        public float nearPlane = 0.1f;
        public float farPlane = 100.0f;
        public int width = 800;
        public int height = 600;
    }

    public ModelRenderer() {
        this.settings = new RenderSettings();
    }

    public void setModel(TriangulatedModel model) {
        this.model = model;
    }

    public void setSettings(RenderSettings settings) {
        this.settings = settings;
    }

    public BufferedImage render() {
        if (model == null || !model.isValid()) {
            return createEmptyImage();
        }

        BufferedImage image = new BufferedImage(settings.width, settings.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Очищаем фон
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, settings.width, settings.height);

        // Рендерим модель
        renderModel(g2d);

        g2d.dispose();
        return image;
    }

    private BufferedImage createEmptyImage() {
        BufferedImage image = new BufferedImage(settings.width, settings.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, settings.width, settings.height);
        g2d.setColor(Color.WHITE);
        g2d.drawString("No model loaded", 10, 20);
        g2d.dispose();
        return image;
    }

    private void renderModel(Graphics2D g2d) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() != 3) {
                continue; // Пропускаем не-треугольники
            }

            // Преобразуем вершины в экранные координаты
            Vector2f[] screenPoints = new Vector2f[3];
            for (int i = 0; i < 3; i++) {
                Vector3f vertex = vertices.get(vertexIndices.get(i));
                screenPoints[i] = projectToScreen(vertex);
            }

            // Рисуем треугольник
            if (settings.drawWireframe) {
                drawWireframe(g2d, screenPoints);
            } else {
                drawFilledTriangle(g2d, screenPoints, polygon);
            }
        }
    }

    private Vector2f projectToScreen(Vector3f vertex) {
        // Простая ортографическая проекция (пока без перспективы)
        float aspect = (float) settings.width / settings.height;

        // Проекция на экран
        float screenX = (vertex.getX() * 100 + settings.width / 2);
        float screenY = (-vertex.getY() * 100 + settings.height / 2);

        return new Vector2f(screenX, screenY);
    }

    private void drawWireframe(Graphics2D g2d, Vector2f[] points) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));

        for (int i = 0; i < 3; i++) {
            int next = (i + 1) % 3;
            g2d.drawLine(
                    (int) points[i].getX(), (int) points[i].getY(),
                    (int) points[next].getX(), (int) points[next].getY()
            );
        }
    }

    private void drawFilledTriangle(Graphics2D g2d, Vector2f[] points, Polygon polygon) {
        // Простая заливка цветом
        Color color = settings.staticColor;

        // Если включено освещение, вычисляем цвет
        if (settings.useLighting && model.hasNormals()) {
            color = calculateTriangleColor(polygon);
        }

        g2d.setColor(color);

        // Создаем полигон для заливки
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        for (int i = 0; i < 3; i++) {
            xPoints[i] = (int) points[i].getX();
            yPoints[i] = (int) points[i].getY();
        }

        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private Color calculateTriangleColor(Polygon polygon) {
        List<Integer> normalIndices = polygon.getNormalIndices();
        if (normalIndices == null || normalIndices.size() != 3) {
            return settings.staticColor;
        }

        // Вычисляем среднюю нормаль треугольника
        Vector3f avgNormal = new Vector3f(0, 0, 0);
        for (int i = 0; i < 3; i++) {
            Vector3f normal = model.getNormals().get(normalIndices.get(i));
            avgNormal = avgNormal.add(normal);
        }
        avgNormal = avgNormal.normalizeV();

        // Направление света (от камеры к объекту)
        Vector3f lightDir = settings.cameraTarget.sub(settings.cameraPosition).normalizeV();

        // Вычисляем интенсивность
        float intensity = Math.max(0, avgNormal.dot(lightDir));
        intensity = 0.3f + intensity * 0.7f; // ambient + diffuse

        // Применяем интенсивность к цвету
        int r = Math.min(255, (int) (settings.staticColor.getRed() * intensity));
        int g = Math.min(255, (int) (settings.staticColor.getGreen() * intensity));
        int b = Math.min(255, (int) (settings.staticColor.getBlue() * intensity));

        return new Color(r, g, b);
    }
}