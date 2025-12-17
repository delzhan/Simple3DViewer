package com.cgvsu.math;

import java.util.Objects;

public class Vector4f extends AbstractVector {
    private static final int SIZE = 4;

    // Конструкторы
    public Vector4f(float x, float y, float z, float w) {
        this.components = new float[SIZE];
        this.components[0] = x;
        this.components[1] = y;
        this.components[2] = z;
        this.components[3] = w;
        calcLength();
    }

    public Vector4f(float... components) {
        super(components);
        if (components.length != SIZE) {
            throw new IllegalArgumentException("Vector4f must have exactly 4 components");
        }
    }

    public Vector4f() {
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

    public float getW() {
        return components[3];
    }

    public void setW(float w) {
        components[3] = w;
        calcLength();
    }

    // Реализация абстрактных методов
    @Override
    protected int getSize() {
        return SIZE;
    }

    @Override
    protected Vector4f instantiateVector(float[] elements) {
        return new Vector4f(elements);
    }

    // Арифметические операции с правильными типами возврата
    @Override
    public Vector4f add(AbstractVector other) {
        return (Vector4f) super.add(other);
    }

    @Override
    public void addV(AbstractVector other) {
        super.addV(other);
    }

    @Override
    public Vector4f sub(AbstractVector other) {
        return (Vector4f) super.sub(other);
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
    public Vector4f normalizeV() {
        return (Vector4f) super.normalizeV();
    }

    @Override
    public Vector4f multiplyV(float scalar) {
        return (Vector4f) super.multiplyV(scalar);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector4f)) return false;
        Vector4f other = (Vector4f) obj;
        return Float.compare(components[0], other.components[0]) == 0 &&
                Float.compare(components[1], other.components[1]) == 0 &&
                Float.compare(components[2], other.components[2]) == 0 &&
                Float.compare(components[3], other.components[3]) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(components[0], components[1], components[2], components[3]);
    }

    @Override
    public String toString() {
        return "Vector4f{" +
                "x=" + components[0] +
                ", y=" + components[1] +
                ", z=" + components[2] +
                ", w=" + components[3] +
                '}';
    }
}