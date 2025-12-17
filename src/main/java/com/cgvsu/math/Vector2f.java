package com.cgvsu.math;

import java.util.Objects;

public class Vector2f extends AbstractVector {
    private static final float EPSILON = 1e-7f;
    private static final int SIZE = 2;

    // Конструкторы
    public Vector2f(float x, float y) {
        this.components = new float[SIZE];
        this.components[0] = x;
        this.components[1] = y;
        calcLength();
    }

    public Vector2f(float... components) {
        super(components);
        if (components.length != SIZE) {
            throw new IllegalArgumentException("Vector2f must have exactly 2 components");
        }
    }

    public Vector2f() {
        this.components = new float[SIZE];
        calcLength();
    }

    // Геттеры и сеттеры для отдельных компонент
    public float getX() {
        return components[0];
    }

    public void setX(float x) {
        components[0] = x;
        calcLength();
    }

    public float getY() {
        return components[1];
    }

    public void setY(float y) {
        components[1] = y;
        calcLength();
    }

    // Методы сравнения с epsilon
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2f other = (Vector2f) obj;
        return Math.abs(components[0] - other.components[0]) < EPSILON &&
                Math.abs(components[1] - other.components[1]) < EPSILON;
    }

    public boolean equals(Vector2f other) {
        if (other == null) return false;
        return Math.abs(components[0] - other.components[0]) < EPSILON &&
                Math.abs(components[1] - other.components[1]) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Float.hashCode(components[0]),
                Float.hashCode(components[1])
        );
    }

    @Override
    public String toString() {
        return String.format("Vector2f(%.6f, %.6f)", components[0], components[1]);
    }

    // Реализация абстрактных методов
    @Override
    protected int getSize() {
        return SIZE;
    }

    @Override
    protected Vector2f instantiateVector(float[] elements) {
        return new Vector2f(elements);
    }

    // Арифметические операции с правильными типами возврата
    @Override
    public Vector2f add(AbstractVector other) {
        return (Vector2f) super.add(other);
    }

    @Override
    public void addV(AbstractVector other) {
        super.addV(other);
    }

    @Override
    public Vector2f multiplyV(float scalar) {
        return (Vector2f) super.multiplyV(scalar);
    }

    @Override
    public Vector2f sub(AbstractVector other) {
        return (Vector2f) super.sub(other);
    }

    @Override
    public void subV(AbstractVector other) {
        super.subV(other);
    }

    // Этот метод не переопределяет метод из AbstractVector, поэтому убираем @Override
    public void sub(AbstractVector first, AbstractVector second) {
        // Реализация метода sub с двумя параметрами
        if (first == null || second == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }
        if (first.getSize() != SIZE || second.getSize() != SIZE) {
            throw new IllegalArgumentException("Vectors must have size " + SIZE);
        }

        for (int i = 0; i < SIZE; i++) {
            this.components[i] = first.components[i] - second.components[i];
        }
        calcLength();
    }

    @Override
    public float dot(AbstractVector other) {
        return super.dot(other);
    }

    @Override
    public Vector2f normalizeV() {
        return (Vector2f) super.normalizeV();
    }

    @Override
    public Vector2f clone() {
        return new Vector2f(this.components.clone());
    }
}