package com.cgvsu.controller;

public enum Theme {
    LIGHT("/css/light.css"),
    DARK("/css/dark.css");

    private final String stylesheetPath;

    Theme(String stylesheetPath) {
        this.stylesheetPath = stylesheetPath;
    }

    public String getStylesheetPath() {
        return stylesheetPath;
    }
}