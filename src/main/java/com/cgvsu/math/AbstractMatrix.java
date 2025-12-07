package com.cgvsu.math;

public abstract class AbstractMatrix {
    protected float[][] elements;

    //Конструктооры
    public AbstractMatrix(float... array) {
        int size = getSize();
        if (array.length != size * size) {
            throw new IllegalArgumentException("Массив должен содержать ровно " + (size * size) + " элементов.");
        }
        this.elements = new float[size][size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.elements[i][j] = array[index++];
            }
        }
    }

    public AbstractMatrix(float[][] array) {
        int size = getSize();
        if (array.length != size || array[0].length != size) {
            throw new IllegalArgumentException("Массив должен быть размера " + size + "x" + size + ".");
        }
        this.elements = array;
    }

    public AbstractMatrix(AbstractMatrix other) {
        int size = getSize();
        this.elements = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.elements[i][j] = other.getElement(i, j);
            }
        }
    }

    public AbstractMatrix(int one) {
        // Единичная матрица
        int size = getSize();
        this.elements = new float[size][size];
        for (int i = 0; i < size; i++) {
            this.elements[i][i] = 1;
        }
    }

    public AbstractMatrix() {
        // Нулевая матрица
        this.elements = new float[getSize()][getSize()];
    }

    // Абстрактные методы, я скоро уже с ума сойду

    protected abstract int getSize();
    protected abstract AbstractMatrix createInstance(float[] elements);
    protected abstract AbstractMatrix createInstance(float[][] elements);
    protected abstract AbstractMatrix createInstance();
    protected abstract AbstractVector instantiateVector();
    public abstract float getElement(int row, int col);
    public abstract void setElement(int row, int col, float value);

    // основные операции

    // сложение
    public AbstractMatrix add(AbstractMatrix other) {
        checkSameSize(other);
        float[] result = new float[getSize() * getSize()];
        int index = 0;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                result[index++] = this.elements[i][j] + other.getElement(i, j);
            }
        }
        return createInstance(result);
    }

    public void addV(AbstractMatrix other) {
        this.elements = add(other).elements;
    }

    //вычитание
    public AbstractMatrix sub(AbstractMatrix other) {
        checkSameSize(other);
        float[] result = new float[getSize() * getSize()];
        int index = 0;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                result[index++] = this.elements[i][j] - other.getElement(i, j);
            }
        }
        return createInstance(result);
    }

    public void subV(AbstractMatrix other) {
        this.elements = sub(other).elements;
    }

    //умножение на матрицу
    public AbstractMatrix multiplyNew(AbstractMatrix other) {
        checkSameSize(other);
        AbstractMatrix result = createInstance();
        for (int i = 0; i < getSize(); i++) {
            for (int k = 0; k < getSize(); k++) {
                float sum = 0;
                for (int j = 0; j < getSize(); j++) {
                    sum += this.elements[i][j] * other.elements[j][k];
                }
                result.elements[i][k] = sum;
            }
        }
        return result;
    }

    public void multiply(AbstractMatrix other) {
        this.elements = multiplyNew(other).elements;
    }

    // умножение на вектор
    public AbstractVector multiply(AbstractVector vector) {
        if (vector.components.length != getSize()) {
            throw new IllegalArgumentException(
                    "Размер вектора (" + vector.components.length +
                            ") должен совпадать с размером матрицы (" + getSize() + ")"
            );
        }

        AbstractVector result = instantiateVector();
        for (int i = 0; i < getSize(); i++) {
            float sum = 0;
            for (int j = 0; j < getSize(); j++) {
                sum += elements[i][j] * vector.getNum(j);
            }
            result.setNum(i, sum);
        }
        return result;
    }

    // транспонирование
    public AbstractMatrix transpositionNew() {
        float[][] result = new float[getSize()][getSize()];
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                result[j][i] = elements[i][j];
            }
        }
        return createInstance(result);
    }

    public void transposition() {
        this.elements = transpositionNew().elements;
    }

    // обратная матрица
    public AbstractMatrix inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-10) {
            throw new ArithmeticException("Матрица вырождена, обратной не существует.");
        }

        float[][] adjugate = computeAdjugateMatrix();
        float[] inverseElements = new float[getSize() * getSize()];
        int index = 0;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                inverseElements[index++] = adjugate[i][j] / det;
            }
        }
        return createInstance(inverseElements);
    }

    public void inverseV() {
        this.elements = inverse().elements;
    }

    // det
    public float determinant() {
        return computeDeterminant(elements, getSize());
    }

    private float computeDeterminant(float[][] matrix, int size) {
        if (size == 1) return matrix[0][0];
        if (size == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        float det = 0;
        for (int col = 0; col < size; col++) {
            float sign = (col % 2 == 0) ? 1 : -1;
            float[][] minor = getMinor(matrix, 0, col, size);
            det += sign * matrix[0][col] * computeDeterminant(minor, size - 1);
        }
        return det;
    }

    // вспомогат

    private void checkSameSize(AbstractMatrix other) {
        if (other == null || other.getSize() != getSize()) {
            throw new IllegalArgumentException("Матрицы должны иметь одинаковый размер.");
        }
    }

    private float[][] getMinor(float[][] matrix, int excludeRow, int excludeCol, int size) {
        float[][] minor = new float[size - 1][size - 1];
        int minorRow = 0;
        for (int i = 0; i < size; i++) {
            if (i == excludeRow) continue;
            int minorCol = 0;
            for (int j = 0; j < size; j++) {
                if (j == excludeCol) continue;
                minor[minorRow][minorCol++] = matrix[i][j];
            }
            minorRow++;
        }
        return minor;
    }

    private float[][] computeAdjugateMatrix() {
        int size = getSize();
        float[][] cofactors = new float[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                float sign = ((i + j) % 2 == 0) ? 1 : -1;
                float[][] minor = getMinor(elements, i, j, size);
                cofactors[i][j] = sign * computeDeterminant(minor, size - 1);
            }
        }

        // трансп матрицу алгебраических дополнений
        float[][] adjugate = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                adjugate[j][i] = cofactors[i][j];
            }
        }
        return adjugate;
    }

    //еще переопред методы

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                result = 31 * result + Float.hashCode(elements[i][j]);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getSize(); i++) {
            for (int j = 0; j < getSize(); j++) {
                sb.append(String.format("%8.3f", elements[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}