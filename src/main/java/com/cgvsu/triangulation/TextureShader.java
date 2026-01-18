package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextureShader {
    private Vector3f lightDirection = new Vector3f(0, 0, -1);
    private BufferedImage texture;

    public TextureShader() {
        // Конструктор по умолчанию
    }

    public TextureShader(String texturePath) {
        // Загрузка текстуры из файла
        // Пока заглушка
        System.out.println("Loading texture from: " + texturePath);
    }

    public void setLightDirection(Vector3f direction) {
        this.lightDirection = direction.clone();
        this.lightDirection.normalizeV();
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }

    public Color getColor(float u, float v) {
        if (texture != null) {
            u = Math.max(0, Math.min(1, u));
            v = Math.max(0, Math.min(1, v));

            int x = (int) (u * (texture.getWidth() - 1));
            int y = (int) ((1 - v) * (texture.getHeight() - 1));

            return new Color(texture.getRGB(x, y));
        }
        return Color.WHITE;
    }

    public Color shadePixel(float u, float v, Vector3f normal, Color baseColor) {
        // Простой шейдер
        Color texColor = baseColor;

        if (texture != null) {
            texColor = getColor(u, v);
        }

        // Простое затенение
        if (normal != null) {
            Vector3f norm = normal.normalizeV();
            float diff = Math.max(norm.dot(lightDirection), 0.1f);
            int r = (int) (texColor.getRed() * diff);
            int g = (int) (texColor.getGreen() * diff);
            int b = (int) (texColor.getBlue() * diff);

            r = Math.min(255, Math.max(0, r));
            g = Math.min(255, Math.max(0, g));
            b = Math.min(255, Math.max(0, b));

            return new Color(r, g, b);
        }

        return texColor;
    }

    public Color getTextureColor(float u, float v) {
        return getColor(u, v);
    }
}