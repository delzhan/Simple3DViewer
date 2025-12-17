package com.cgvsu.math;

import java.util.Objects;

public class Vector3f extends AbstractVector {
    private static final float EPSILON = 1e-7f;
    private static final int SIZE = 3;

    // Конструкторы
    public Vector3f(float x, float y, float z) {
        this.components = new float[SIZE];
        this.components[0] = x;
        this.components[1] = y;
        this.components[2] = z;
        calcLength();
    }

    public Vector3f(float... components) {
        super(components);
        if (components.length != SIZE) {
            throw new IllegalArgumentException("Vector3f must have exactly 3 components");
        }
    }

    public Vector3f() {
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

    public float getZ() {
        return components[2];
    }

    public void setZ(float z) {
        components[2] = z;
        calcLength();
    }

    // Методы сравнения с epsilon
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3f other = (Vector3f) obj;
        return Math.abs(components[0] - other.components[0]) < EPSILON &&
                Math.abs(components[1] - other.components[1]) < EPSILON &&
                Math.abs(components[2] - other.components[2]) < EPSILON;
    }

    public boolean equals(Vector3f other) {
        if (other == null) return false;
        return Math.abs(components[0] - other.components[0]) < EPSILON &&
                Math.abs(components[1] - other.components[1]) < EPSILON &&
                Math.abs(components[2] - other.components[2]) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Float.hashCode(components[0]),
                Float.hashCode(components[1]),
                Float.hashCode(components[2])
        );
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.6f, %.6f, %.6f)",
                components[0], components[1], components[2]);
    }

    // Реализация абстрактных методов
    @Override
    protected int getSize() {
        return SIZE;
    }

    @Override
    protected Vector3f instantiateVector(float[] elements) {
        return new Vector3f(elements);
    }

    // Арифметические операции с правильными типами возврата
    @Override
    public Vector3f add(AbstractVector other) {
        return (Vector3f) super.add(other);
    }

    @Override
    public void addV(AbstractVector other) {
        super.addV(other);
    }

    @Override
    public Vector3f multiplyV(float scalar) {
        return (Vector3f) super.multiplyV(scalar);
    }

    @Override
    public Vector3f sub(AbstractVector other) {
        return (Vector3f) super.sub(other);
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

    // Метод векторного произведения
    public Vector3f cross(Vector3f other) {
        float x = this.components[1] * other.components[2] - this.components[2] * other.components[1];
        float y = this.components[2] * other.components[0] - this.components[0] * other.components[2];
        float z = this.components[0] * other.components[1] - this.components[1] * other.components[0];
        return new Vector3f(x, y, z);
    }

    @Override
    public Vector3f normalizeV() {
        return (Vector3f) super.normalizeV();
    }

    @Override
    public Vector3f clone() {
        return new Vector3f(this.components.clone());
    }
}