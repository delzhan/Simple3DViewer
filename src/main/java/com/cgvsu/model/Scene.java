package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Scene {
    private List<ModelInstance> modelInstances;
    private Set<Integer> selectedModelIndices; // Множественный выбор
    private Camera camera;
    private Vector3f backgroundColor;

    public Scene() {
        this.modelInstances = new ArrayList<>();
        this.selectedModelIndices = new HashSet<>();
        this.camera = new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1, 0.01F, 100
        );
        this.backgroundColor = new Vector3f(0.2f, 0.2f, 0.2f);
    }

    // Выбирает одну модель (снимает предыдущий выбор)
    public void selectModel(int index) {
        if (index >= 0 && index < modelInstances.size()) {
            selectedModelIndices.clear();
            selectedModelIndices.add(index);
        }
    }

    // Добавляет модель к выбору (множественный выбор)
    public void addToSelection(int index) {
        if (index >= 0 && index < modelInstances.size()) {
            selectedModelIndices.add(index);
        }
    }

    // Снимает выделение с модели
    public void removeFromSelection(int index) {
        selectedModelIndices.remove(index);
    }

    // Очищает весь выбор
    public void clearSelection() {
        selectedModelIndices.clear();
    }

    // Переключает выбор модели
    public void toggleSelection(int index) {
        if (selectedModelIndices.contains(index)) {
            removeFromSelection(index);
        } else {
            addToSelection(index);
        }
    }

    // Выбирает все модели
    public void selectAll() {
        selectedModelIndices.clear();
        for (int i = 0; i < modelInstances.size(); i++) {
            selectedModelIndices.add(i);
        }
    }

    // Получает список выбранных моделей
    public List<ModelInstance> getSelectedModelInstances() {
        List<ModelInstance> selected = new ArrayList<>();
        for (int index : selectedModelIndices) {
            if (index >= 0 && index < modelInstances.size()) {
                selected.add(modelInstances.get(index));
            }
        }
        return selected;
    }

    // Получает первую выбранную модель (для удобства)
    public ModelInstance getFirstSelectedModelInstance() {
        if (!selectedModelIndices.isEmpty()) {
            int firstIndex = selectedModelIndices.iterator().next();
            return modelInstances.get(firstIndex);
        }
        return null;
    }

    public Set<Integer> getSelectedIndices() {
        return new HashSet<>(selectedModelIndices);
    }

    public boolean isModelSelected(int index) {
        return selectedModelIndices.contains(index);
    }

    public void addModelInstance(ModelInstance modelInstance) {
        modelInstances.add(modelInstance);
        // При добавлении новой модели автоматически выбираем её
        selectModel(modelInstances.size() - 1);
    }

    public void removeModelInstance(int index) {
        if (index >= 0 && index < modelInstances.size()) {
            selectedModelIndices.remove(index);
            // Смещаем остальные индексы в выборе
            Set<Integer> newSelection = new HashSet<>();
            for (int selectedIndex : selectedModelIndices) {
                if (selectedIndex > index) {
                    newSelection.add(selectedIndex - 1);
                } else {
                    newSelection.add(selectedIndex);
                }
            }
            selectedModelIndices = newSelection;

            modelInstances.remove(index);
        }
    }

    public void removeSelectedModels() {
        // Удаляем в обратном порядке, чтобы индексы не сбивались
        List<Integer> sortedIndices = new ArrayList<>(selectedModelIndices);
        sortedIndices.sort((a, b) -> b - a); // Сортируем по убыванию

        for (int index : sortedIndices) {
            removeModelInstance(index);
        }
        selectedModelIndices.clear();
    }

    public void rotateSelectedX(double angle) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.rotateX(angle);
        }
    }

    public void rotateSelectedY(double angle) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.rotateY(angle);
        }
    }

    public void rotateSelectedZ(double angle) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.rotateZ(angle);
        }
    }

    public void scaleSelected(double factor) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.scale(factor);
        }
    }

    public void translateSelectedX(double delta) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.translateX(delta);
        }
    }

    public void translateSelectedY(double delta) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.translateY(delta);
        }
    }

    public void translateSelectedZ(double delta) {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.translateZ(delta);
        }
    }

    public void resetSelectedTransformations() {
        for (ModelInstance instance : getSelectedModelInstances()) {
            instance.reset();
        }
    }

    public List<ModelInstance> getModelInstances() {
        return modelInstances;
    }

    public ModelInstance getModelInstance(int index) {
        if (index >= 0 && index < modelInstances.size()) {
            return modelInstances.get(index);
        }
        return null;
    }

    public int getModelCount() {
        return modelInstances.size();
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Vector3f getBackgroundColor() {
        return backgroundColor;
    }

    public void clear() {
        modelInstances.clear();
        selectedModelIndices.clear();
    }
}