package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RenderGUI {
    private Vector3f lightDirection = new Vector3f(0, 0, -1);
    private Vector3f viewDir = new Vector3f(0, 0, -1);
    private Vector3f cameraPosition = new Vector3f(0, 0, 5);
    private BufferedImage texture;
    private List<Light> lights = new ArrayList<>();
    private boolean useTexture = false;
    private boolean useSmoothNormals = true;
    private boolean usePhongShading = true;
    private float ambientStrength = 0.1f;
    private float diffuseStrength = 0.7f;
    private float specularStrength = 0.2f;
    private float shininess = 32.0f;

    public void setLightDirection(Vector3f direction) {
        this.lightDirection = direction.clone();
        this.lightDirection.normalizeV();
    }

    public void setViewDirection(Vector3f viewDir) {
        this.viewDir = viewDir.clone();
        this.viewDir.normalizeV();
    }

    public void setCameraPosition(Vector3f cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public void setUseSmoothNormals(boolean useSmoothNormals) {
        this.useSmoothNormals = useSmoothNormals;
    }

    public void setUsePhongShading(boolean usePhongShading) {
        this.usePhongShading = usePhongShading;
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    public void clearLights() {
        this.lights.clear();
    }

    public Color shadePixel(float u, float v, Vector3f normal, Color baseColor) {
        Color texColor = baseColor;

        // Применяем текстуру
        if (texture != null && useTexture) {
            texColor = getTextureColor(u, v);
        }

        // Если сглаживание нормалей отключено и нормаль не задана,
        // используем дефолтную (направленную к камере)
        if (normal == null || (normal.getX() == 0 && normal.getY() == 0 && normal.getZ() == 0)) {
            normal = viewDir;
        } else {
            normal = normal.normalizeV();
        }

        // Нормализуем вектор взгляда
        Vector3f normalizedViewDir = viewDir.normalizeV();

        // Начинаем с ambient компонента
        Color ambient = multiplyColor(texColor, ambientStrength);
        Color result = ambient;

        // Добавляем вклад от каждого источника света
        for (Light light : lights) {
            if (!light.isEnabled()) continue;

            switch (light.getType()) {
                case DIRECTIONAL:
                    result = addColors(result,
                            calculateDirectionalLight(light, normal, normalizedViewDir, cameraPosition, texColor));
                    break;
                case POINT:
                    result = addColors(result,
                            calculatePointLight(light, normal, normalizedViewDir, cameraPosition, texColor));
                    break;
                case SPOT:
                    result = addColors(result,
                            calculateSpotLight(light, normal, normalizedViewDir, cameraPosition, texColor));
                    break;
            }
        }

        return result;
    }

    private Color calculateDirectionalLight(Light light, Vector3f normal,
                                            Vector3f viewDir, Vector3f position,
                                            Color baseColor) {
        // Направление света противоположно направлению луча
        Vector3f lightDir = light.getDirection().clone();
        lightDir.multiplyV(-1);
        lightDir.normalizeV();

        // Diffuse компонент
        float diff = Math.max(normal.dot(lightDir), 0.0f);
        Color diffuse = multiplyColor(baseColor, diff * diffuseStrength);
        diffuse = multiplyColor(diffuse, light.getEffectiveDiffuseColor());

        // Specular компонент (модель Блинна-Фонга)
        Color specular = Color.BLACK;
        if (usePhongShading) {
            Vector3f reflectDir = reflect(lightDir.multiplyV(-1), normal);
            float spec = (float) Math.pow(Math.max(viewDir.dot(reflectDir), 0.0f), shininess);
            specular = multiplyColor(light.getEffectiveSpecularColor(), spec * specularStrength);
        }

        return addColors(diffuse, specular);
    }

    private Color calculatePointLight(Light light, Vector3f normal,
                                      Vector3f viewDir, Vector3f position,
                                      Color baseColor) {
        // Вектор от точки к свету
        Vector3f lightDir = light.getPosition().clone();
        lightDir.subV(position);
        float distance = calculateDistance(position, light.getPosition());
        lightDir.normalizeV();

        // Диффузное затенение
        float diff = Math.max(normal.dot(lightDir), 0.0f);
        Color diffuse = multiplyColor(baseColor, diff * diffuseStrength);
        diffuse = multiplyColor(diffuse, light.getEffectiveDiffuseColor());

        // Specular компонент
        Color specular = Color.BLACK;
        if (usePhongShading) {
            Vector3f reflectDir = reflect(lightDir.multiplyV(-1), normal);
            float spec = (float) Math.pow(Math.max(viewDir.dot(reflectDir), 0.0f), shininess);
            specular = multiplyColor(light.getEffectiveSpecularColor(), spec * specularStrength);
        }

        // Затухание (правильная формула)
        float attenuation = 1.0f / (light.getConstantAttenuation() +
                light.getLinearAttenuation() * distance +
                light.getQuadraticAttenuation() * distance * distance);

        diffuse = multiplyColor(diffuse, attenuation);
        specular = multiplyColor(specular, attenuation);

        return addColors(diffuse, specular);
    }

    private Color calculateSpotLight(Light light, Vector3f normal,
                                     Vector3f viewDir, Vector3f position,
                                     Color baseColor) {
        // Вектор от точки к свету
        Vector3f lightDir = light.getPosition().clone();
        lightDir.subV(position);
        float distance = calculateDistance(position, light.getPosition());
        lightDir.normalizeV();

        // Угол между направлением света и вектором к точке
        Vector3f lightDirectionNeg = light.getDirection().clone();
        lightDirectionNeg.multiplyV(-1);
        lightDirectionNeg.normalizeV();
        float theta = lightDir.dot(lightDirectionNeg);

        float epsilon = (float) Math.cos(Math.toRadians(light.getCutoffAngle())) -
                (float) Math.cos(Math.toRadians(light.getCutoffAngle() * 0.9f));
        float intensity = clamp((theta - (float) Math.cos(Math.toRadians(light.getCutoffAngle()))) / epsilon, 0.0f, 1.0f);

        if (intensity > 0) {
            // Диффузное затенение
            float diff = Math.max(normal.dot(lightDir), 0.0f);
            Color diffuse = multiplyColor(baseColor, diff * diffuseStrength);
            diffuse = multiplyColor(diffuse, light.getEffectiveDiffuseColor());

            // Specular компонент
            Color specular = Color.BLACK;
            if (usePhongShading) {
                Vector3f reflectDir = reflect(lightDir.multiplyV(-1), normal);
                float spec = (float) Math.pow(Math.max(viewDir.dot(reflectDir), 0.0f), shininess);
                specular = multiplyColor(light.getEffectiveSpecularColor(), spec * specularStrength);
            }

            // Затухание (правильная формула)
            float attenuation = 1.0f / (light.getConstantAttenuation() +
                    light.getLinearAttenuation() * distance +
                    light.getQuadraticAttenuation() * distance * distance);

            diffuse = multiplyColor(diffuse, attenuation * intensity);
            specular = multiplyColor(specular, attenuation * intensity);

            return addColors(diffuse, specular);
        }

        return Color.BLACK;
    }

    // Метод для вычисления расстояния между двумя векторами
    private float calculateDistance(Vector3f a, Vector3f b) {
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        float dz = b.getZ() - a.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private Color getTextureColor(float u, float v) {
        if (texture == null) return Color.WHITE;

        u = Math.max(0, Math.min(1, u));
        v = Math.max(0, Math.min(1, v));

        int x = (int) (u * (texture.getWidth() - 1));
        int y = (int) ((1 - v) * (texture.getHeight() - 1));

        return new Color(texture.getRGB(x, y));
    }

    // Вспомогательные математические функции для Vector3f
    private Vector3f reflect(Vector3f v, Vector3f n) {
        float dot = v.dot(n) * 2.0f;
        Vector3f result = n.clone();
        result.multiplyV(dot);
        result.subV(v);
        return result.multiplyV(-1);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private Color multiplyColor(Color color, float factor) {
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));
        return new Color(r, g, b);
    }

    private Color multiplyColor(Color a, Color b) {
        int r = a.getRed() * b.getRed() / 255;
        int g = a.getGreen() * b.getGreen() / 255;
        int b1 = a.getBlue() * b.getBlue() / 255;
        return new Color(r, g, b1);
    }

    private Color addColors(Color a, Color b) {
        int r = Math.min(255, a.getRed() + b.getRed());
        int g = Math.min(255, a.getGreen() + b.getGreen());
        int b1 = Math.min(255, a.getBlue() + b.getBlue());
        return new Color(r, g, b1);
    }
}