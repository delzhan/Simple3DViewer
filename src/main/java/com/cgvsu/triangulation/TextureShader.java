package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextureShader {
    private Vector3f lightDirection = new Vector3f(0, 0, -1);
    private BufferedImage texture;
    private boolean hasTexture = false;

    public TextureShader() {
        // Конструктор по умолчанию
    }

    public TextureShader(String texturePath) {
        loadTexture(texturePath);
    }

    public void setLightDirection(Vector3f direction) {
        if (direction != null) {
            this.lightDirection = direction.clone();
            this.lightDirection.normalize();
        }
    }

    public boolean loadTexture(String texturePath) {
        try {
            File file = new File(texturePath);
            if (!file.exists()) {
                System.err.println("Texture file not found: " + texturePath);
                return false;
            }

            this.texture = ImageIO.read(file);
            this.hasTexture = (texture != null);

            if (hasTexture) {
                System.out.println("Texture loaded successfully: " + texturePath);
                System.out.println("Texture dimensions: " + texture.getWidth() + "x" + texture.getHeight());
            } else {
                System.err.println("Failed to load texture: " + texturePath);
            }

            return hasTexture;
        } catch (IOException e) {
            System.err.println("Error loading texture: " + texturePath);
            e.printStackTrace();
            return false;
        }
    }

    // Загрузка текстуры из файла
    public void setTexture(String texturePath) {
        loadTexture(texturePath);
    }

    // Установка уже загруженной текстуры
    public void setTexture(BufferedImage texture) {
        this.texture = texture;
        this.hasTexture = (texture != null);
    }

    public Color getColor(float u, float v) {
        if (!hasTexture || texture == null) {
            return Color.WHITE;
        }

        // Нормализация координат текстуры
        u = clamp(u, 0.0f, 1.0f);
        v = clamp(v, 0.0f, 1.0f);

        // Преобразование координат текстуры в пиксельные координаты
        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) ((1.0f - v) * (texture.getHeight() - 1));

        // границы массива
        x = Math.max(0, Math.min(x, texture.getWidth() - 1));
        y = Math.max(0, Math.min(y, texture.getHeight() - 1));

        return new Color(texture.getRGB(x, y), true);
    }

    public Color shadePixel(float u, float v, Vector3f normal, Color baseColor) {
        Color texColor = baseColor;

        // Получаем цвет из текстуры, если она есть
        if (hasTexture && texture != null) {
            texColor = getColor(u, v);
        }

        // затемняем, если есть нормаль
        if (normal != null) {
            Vector3f norm = normal.clone();
            float diff = Math.max(norm.dot(lightDirection), 0.1f);

            // диффузное освещение к цвету
            int r = clamp((int) (texColor.getRed() * diff), 0, 255);
            int g = clamp((int) (texColor.getGreen() * diff), 0, 255);
            int b = clamp((int) (texColor.getBlue() * diff), 0, 255);
            int a = texColor.getAlpha();

            return new Color(r, g, b, a);
        }

        return texColor;
    }

    public Color getTextureColor(float u, float v) {
        return getColor(u, v);
    }

    public boolean hasTexture() {
        return hasTexture;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    // Вспомогательные методы
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // Метод для расчета интенсивности освещения
    public float calculateLightIntensity(Vector3f normal) {
        if (normal == null) return 1.0f;

        Vector3f norm = normal.clone();
        return Math.max(norm.dot(lightDirection), 0.1f);
    }
}