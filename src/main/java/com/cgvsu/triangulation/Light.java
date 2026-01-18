package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;

import java.awt.*;

public class Light {
    public enum LightType {
        DIRECTIONAL,
        POINT,
        SPOT
    }

    private String name;
    private LightType type;
    private Vector3f position;
    private Vector3f direction;
    private Color ambientColor;
    private Color diffuseColor;
    private Color specularColor;
    private float intensity;
    private float constantAttenuation;
    private float linearAttenuation;
    private float quadraticAttenuation;
    private float cutoffAngle;
    private boolean enabled;

    public Light(String name, LightType type) {
        this.name = name;
        this.type = type;
        this.position = new Vector3f(0, 0, 0);
        this.direction = new Vector3f(0, 0, -1);
        this.ambientColor = new Color(50, 50, 50);
        this.diffuseColor = new Color(200, 200, 200);
        this.specularColor = new Color(255, 255, 255);
        this.intensity = 1.0f;
        this.constantAttenuation = 1.0f;
        this.linearAttenuation = 0.09f;
        this.quadraticAttenuation = 0.032f;
        this.cutoffAngle = 45.0f;
        this.enabled = true;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LightType getType() { return type; }
    public void setType(LightType type) { this.type = type; }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Vector3f getDirection() { return direction; }
    public void setDirection(Vector3f direction) { this.direction = direction; }

    public Color getAmbientColor() { return ambientColor; }
    public void setAmbientColor(Color ambientColor) { this.ambientColor = ambientColor; }

    public Color getDiffuseColor() { return diffuseColor; }
    public void setDiffuseColor(Color diffuseColor) { this.diffuseColor = diffuseColor; }

    public Color getSpecularColor() { return specularColor; }
    public void setSpecularColor(Color specularColor) { this.specularColor = specularColor; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) { this.intensity = intensity; }

    public float getConstantAttenuation() { return constantAttenuation; }
    public void setConstantAttenuation(float constantAttenuation) { this.constantAttenuation = constantAttenuation; }

    public float getLinearAttenuation() { return linearAttenuation; }
    public void setLinearAttenuation(float linearAttenuation) { this.linearAttenuation = linearAttenuation; }

    public float getQuadraticAttenuation() { return quadraticAttenuation; }
    public void setQuadraticAttenuation(float quadraticAttenuation) { this.quadraticAttenuation = quadraticAttenuation; }

    public float getCutoffAngle() { return cutoffAngle; }
    public void setCutoffAngle(float cutoffAngle) { this.cutoffAngle = cutoffAngle; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // Метод для получения цвета с учетом интенсивности
    public Color getEffectiveAmbientColor() {
        return scaleColor(ambientColor, intensity);
    }

    public Color getEffectiveDiffuseColor() {
        return scaleColor(diffuseColor, intensity);
    }

    public Color getEffectiveSpecularColor() {
        return scaleColor(specularColor, intensity);
    }

    private Color scaleColor(Color color, float scale) {
        int r = (int) (color.getRed() * scale);
        int g = (int) (color.getGreen() * scale);
        int b = (int) (color.getBlue() * scale);
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));
        return new Color(r, g, b);
    }

    // Создание стандартных источников света
    public static Light createDirectionalLight(String name, Vector3f direction) {
        Light light = new Light(name, LightType.DIRECTIONAL);
        light.setDirection(direction);
        return light;
    }

    public static Light createPointLight(String name, Vector3f position) {
        Light light = new Light(name, LightType.POINT);
        light.setPosition(position);
        return light;
    }

    public static Light createCameraLight(String name, Camera camera) {
        Light light = new Light(name, LightType.DIRECTIONAL);
        // Используем математику из первого кода:
        Vector3f camDir = camera.getTarget().sub(camera.getPosition());
        camDir.normalize();
        light.setDirection(camDir);
        return light;
    }

    private static float[] normalize(float[] v) {
        float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (length > 0) {
            return new float[]{v[0] / length, v[1] / length, v[2] / length};
        }
        return v;
    }

    private static float[] subtract(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }
}