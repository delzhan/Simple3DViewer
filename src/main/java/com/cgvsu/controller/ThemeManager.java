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
        themes.put(Theme.LIGHT, "/css/light.css");
        themes.put(Theme.DARK, "/css/dark.css");

        currentTheme = Theme.LIGHT;
    }

    public void applyTheme(Scene scene) {
        if (scene == null || currentTheme == null) return;

        URL resourceUrl = getClass().getResource(themes.get(currentTheme));
        if (resourceUrl == null) {
            System.err.println("Stylesheet not found: " + themes.get(currentTheme));
            return;
        }

        String url = resourceUrl.toExternalForm();

        scene.getStylesheets().clear();
        scene.getStylesheets().add(url);

        // принудительный перерасчет макета
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