package com.cgvsu.controller;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.application.Platform;
import java.net.URL;
import java.util.*;

public class ThemeManager {
    private final Map<Theme, String> themes = new LinkedHashMap<>();
    private Theme currentTheme;

    public ThemeManager() {
        // Регистрируем темы
        themes.put(Theme.LIGHT, "/css/light.css");
        themes.put(Theme.DARK, "/css/dark.css");

        // Светлая тема по умолчанию
        currentTheme = Theme.LIGHT;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public List<Theme> getAvailableThemes() {
        return new ArrayList<>(themes.keySet());
    }

    public void applyTheme(Scene scene) {
        if (scene == null || currentTheme == null) return;

        URL resourceUrl = getClass().getResource(themes.get(currentTheme));
        if (resourceUrl == null) {
            System.err.println("Stylesheet not found: " + themes.get(currentTheme));
            return;
        }

        String url = resourceUrl.toExternalForm();

        // Полностью очищаем все старые стили
        scene.getStylesheets().clear();
        // Добавляем новую тему
        scene.getStylesheets().add(url);

        // 🔧 Важное дополнение: принудительный перерасчет макета
        Platform.runLater(() -> {
            Parent root = scene.getRoot();
            if (root != null) {
                root.applyCss();
                root.layout();
            }
        });
    }

    public void setTheme(Scene scene, Theme theme) {
        if (theme == null || theme == currentTheme) return;

        currentTheme = theme;
        applyTheme(scene);
    }
}