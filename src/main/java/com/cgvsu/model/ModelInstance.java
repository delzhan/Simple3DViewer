package com.cgvsu.model;

import com.cgvsu.render_engine.TransformationParameters;
import javafx.scene.paint.Color;

public class ModelInstance {
    private Model model;
    private TransformationParameters transformationParams;
    private Color modelColor;

    public ModelInstance(Model model) {
        this.model = model;
        this.transformationParams = new TransformationParameters();
        transformationParams.setScaleX(1.0);
        transformationParams.setScaleY(1.0);
        transformationParams.setScaleZ(1.0);
        this.modelColor = Color.WHITE;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public TransformationParameters getTransformationParams() {
        return transformationParams;
    }

    public void setTransformationParams(TransformationParameters params) {
        this.transformationParams = params;
    }

    public Color getModelColor() {
        return modelColor;
    }

    public void setModelColor(Color modelColor) {
        this.modelColor = modelColor;
    }

    // Методы трансформаций
    public void rotateX(double angle) {
        transformationParams.setAlpha(transformationParams.getAlpha() + angle);
    }

    public void rotateY(double angle) {
        transformationParams.setBeta(transformationParams.getBeta() + angle);
    }

    public void rotateZ(double angle) {
        transformationParams.setGamma(transformationParams.getGamma() + angle);
    }

    public void scale(double factor) {
        transformationParams.setScaleX(transformationParams.getScaleX() * factor);
        transformationParams.setScaleY(transformationParams.getScaleY() * factor);
        transformationParams.setScaleZ(transformationParams.getScaleZ() * factor);
    }

    public void scaleX(double factor) {
        transformationParams.setScaleX(transformationParams.getScaleX() * factor);
    }

    public void scaleY(double factor) {
        transformationParams.setScaleY(transformationParams.getScaleY() * factor);
    }

    public void scaleZ(double factor) {
        transformationParams.setScaleZ(transformationParams.getScaleZ() * factor);
    }

    public void translateX(double delta) {
        transformationParams.setTranslationX(transformationParams.getTranslationX() + delta);
    }

    public void translateY(double delta) {
        transformationParams.setTranslationY(transformationParams.getTranslationY() + delta);
    }

    public void translateZ(double delta) {
        transformationParams.setTranslationZ(transformationParams.getTranslationZ() + delta);
    }

    public void reset() {
        transformationParams.setAlpha(0);
        transformationParams.setBeta(0);
        transformationParams.setGamma(0);
        transformationParams.setTranslationX(0);
        transformationParams.setTranslationY(0);
        transformationParams.setTranslationZ(0);
        transformationParams.setScaleX(1.0);
        transformationParams.setScaleY(1.0);
        transformationParams.setScaleZ(1.0);
    }
}