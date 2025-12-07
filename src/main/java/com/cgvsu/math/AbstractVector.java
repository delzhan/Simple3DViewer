package com.cgvsu.math;

public abstract class AbstractVector {
    protected float[] components;
    protected float length;

    /**
     * Конструктор для создания вектора с заданными компонентами. <p>
     * <p>
     * т.е. можно создавать как AbstractVector(1,2,3,4 и т.п.), так и AbstractVector(int[]{1,2,3,4 и т.п.})
     * </p>
     */

    public AbstractVector(float... components) {
        if (components.length != getSize()) {
            throw new IndexOutOfBoundsException("Неверная длина");
        }
        this.components = components;
        calcLength();
    }

    /**
     * Конструктор для создания нулевого вектора.
     */
    public AbstractVector() {
        components = new float[getSize()];
        for (int i = 0; i < getSize(); i++) {
            this.components[i] = 0;
        }
        calcLength();
    }


    /**
     * Вычисляет длину вектора и сохраняет её.
     */
    protected void calcLength() {
        float res = 0;
        for (int i = 0; i < getSize(); i++) {
            res += (components[i] * components[i]);
        }
        length = (float) Math.sqrt(res);
    }

    protected abstract int getSize();

    protected abstract AbstractVector instantiateVector(float[] elements);
    public float length() {
        calcLength();
        return length;
    }


    /**
     * Складывает текущий вектор с другим вектором.
     */
    public AbstractVector add(AbstractVector other) {
        return addVector(other);
    }
    /**
     * Общий метод складывания вектора с другим вектором.
     */
    private AbstractVector addVector(AbstractVector other) {
        equalsLength(other);
        float[] res = new float[getSize()];
        for (int i = 0; i < getSize(); i++) {
            res[i] = this.components[i] + other.components[i];
        }
        return instantiateVector(res);
    }


    /**
     * Складывает текущий вектор с другим вектором.
     */
    public void addV(AbstractVector other) {
        this.components = addVector(other).components;
    }

    /**
     * Общий метод вычисления разницы. Возвращает новый вектор
     */
    private AbstractVector subVector(AbstractVector other) {
        equalsLength(other);
        float[] res = new float[getSize()];
        for (int i = 0; i < getSize(); i++) {
            res[i] = this.components[i] - other.components[i];
        }
        return instantiateVector(res);
    }

    /**
     * Вычитает из текущего вектора другой вектор.
     */
    public AbstractVector sub(AbstractVector other) {
        return subVector(other);
    }

    /**
     * вычитание с изменением состояния текущего вектора

     */
    public void subV(AbstractVector other) {
        this.components = subVector(other).components;
    }


    /**
     * Умножает текущий вектор на скаляр. Изменяет состояние текущего вектора
     */
    public void multiply(float scalar) {
        for (int i = 0; i < components.length; i++) {
            this.components[i] *= scalar;
        }
        calcLength();
    }
    /**
     * Умножает текущий вектор на скаляр. Создает новый вектор
     */

    public AbstractVector multiplyV(float scalar) {
        AbstractVector res = instantiateVector(this.components);
        res.multiply(scalar);
        return res;
    }


    /**
     * Делит текущий вектор на скаляр.
     */
    public void divide(float scalar) {
        if (scalar == 0) {
            throw new ArithmeticException("Деление на ноль");
        }
        for (int i = 0; i < components.length; i++) {
            this.components[i] /= scalar;
        }
        calcLength();
    }

    /**
     * Делит текущий вектор на скаляр. Создает новый вектор.
     */
    public AbstractVector divideV(float scalar) {
        AbstractVector res = instantiateVector(this.components);
        res.divide(scalar);
        return res;
    }


    /**
     * Вычисляет скалярное произведение текущего вектора с другим вектором.
     */
    public float dot(AbstractVector other) {
        equalsLength(other);
        float res = 0;
        for (int i = 0; i < getSize(); i++) {
            res += (this.components[i] * other.components[i]);
        }
        return res;
    }

    /**
     * <p>Нормализует вектор, НО. При этом изменяет состояние текущего вектора
     * Полетит не все, но все еще полетит, лучше погядите на следщй метод, он то же самое, только в профиль
     * ну и без полетов(предварительно)
     * Если длина вектора равна нулю, метод ничего не делает
     */
    public void normalize() {
        calcNormalize();
    }
    /**
     * <p>Нормализует вектор.</p>
     * Новый вектор. Благодаря этому можно играться с векторами: vector = vector1.add(vector2).normalizeV()
     * предвариллоьно!!! должно быть живо. надеюсь
     * Если длина вектора равна нулю, метод ничего не делает.
     */
    public AbstractVector normalizeV() {
        AbstractVector res = instantiateVector(this.components);
        res.normalize();
        return res;
    }

    public boolean positiveVector(){
        for (int i = 0; i < components.length; i++) {
            if (components[i]<=0){
                return false;
            }
        }
        return true;
    }

    /**
     * Возвращает компонент вектора по индексу.
     */

    public float getNum(int a) {
        if (a < 0 || a >= components.length) {
            throw new IndexOutOfBoundsException("Invalid index: " + a);
        }
        return components[a];
    }

    /**
     * Заменяет значения компоненты вектора по индексу.
     */

    public void setNum(int a, float num) {
        if (a < 0 || a >= components.length) {
        }
        components[a] = num;
        calcLength();
    }

    public void setElements(float... component) {
        if (component.length != components.length){
            throw new IndexOutOfBoundsException("Invalid length: ");
        }
        this.components = component;
    }

    /**
     * Проверка, что нам скармливают одинаковые вектора. Пусть будет, на всякий, в случае чего - не используйте
     */

    private void equalsLength(AbstractVector other) {
        if (this.components.length != other.components.length) {
            throw new IndexOutOfBoundsException("Разная длина");
        }
    }

    /**
     * Логика нормализации)
     */
    private void calcNormalize() {
        calcLength();
        if (length == 0) {
            return;
        }
        divide(length);
    }

}