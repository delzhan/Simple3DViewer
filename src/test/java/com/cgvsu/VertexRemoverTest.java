package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.removers.VertexRemover;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class VertexRemoverTest {

    private Model model;

    @BeforeEach
    void setUp() { // Модель для тестов
        // Создаем простую модель - треугольник для каждого теста
        model = new Model();

        // Вершины: треугольник
        model.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0),   // индекс 1 в OBJ
                new Vector3f(1, 0, 0),   // индекс 2
                new Vector3f(0, 1, 0),   // индекс 3
                new Vector3f(2, 2, 2)    // индекс 4 - изолированная вершина
        ));

        // Полигон (треугольник из первых трех вершин)
        Polygon triangle = new Polygon();
        triangle.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        model.getPolygons().add(triangle);

        // Нормали (будут пересчитаны автоматически)
    }

    @Test
    void testRemoveSingleVertexRemovesPolygon() {  // Тест удаления вершины, которая используется в полигоне
        // Удаляем вершину 1, которая входит в треугольник
        List<Integer> verticesToDelete = Arrays.asList(1);

        // Запоминаем начальное состояние
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Выполняем удаление
        VertexRemover.removeVertices(model, verticesToDelete);

        // Проверяем результаты
        assertEquals(initialVertexCount - 1, model.getVertices().size(),
                "Должна удалиться одна вершина");
        assertEquals(0, model.getPolygons().size(),
                "Все полигоны, содержащие удаленную вершину, должны быть удалены");
        assertEquals(3, model.getNormals().size(),
                "Нормали должны быть пересчитаны для оставшихся вершин");
    }

    @Test
    void testRemoveIsolatedVertexKeepsPolygons() { // Тест удаления сиротской вершины (не используемой в полигонах)
        // Удаляем вершину 4, которая не входит ни в один полигон
        List<Integer> verticesToDelete = Arrays.asList(4);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        VertexRemover.removeVertices(model, verticesToDelete);

        // Проверяем
        assertEquals(initialVertexCount - 1, model.getVertices().size(),
                "Изолированная вершина должна быть удалена");
        assertEquals(initialPolygonCount, model.getPolygons().size(),
                "Полигоны не должны быть затронуты");
        // Треугольник из вершин 1,2,3 должен остаться
        Polygon remainingPolygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getVertexIndices(),
                "Индексы полигона не должны измениться при удалении изолированной вершины");
    }

    @Test
    void testRemoveMultipleVertices() { // Тест удаления нескольких вершин одновременно
        // Удаляем вершины 1 и 4
        List<Integer> verticesToDelete = Arrays.asList(1, 4);

        VertexRemover.removeVertices(model, verticesToDelete);

        // Ожидаем: вершина 1 удалена (и полигон тоже), вершина 4 удалена
        assertEquals(2, model.getVertices().size(), "Осталось 2 вершины");
        assertEquals(0, model.getPolygons().size(), "Нет полигонов");
    }

    @Test
    void testReindexingAfterVertexRemoval() { // Тест корректности переиндексации после удаления вершины
        // Создаем более сложную модель с двумя полигонами
        Model complexModel = new Model();

        // 4 вершины квадрата
        complexModel.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0), // 1
                new Vector3f(1, 0, 0), // 2
                new Vector3f(1, 1, 0), // 3
                new Vector3f(0, 1, 0)  // 4
        ));

        // Два треугольника, составляющие квадрат
        Polygon tri1 = new Polygon();
        tri1.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        Polygon tri2 = new Polygon();
        tri2.setVertexIndices(new ArrayList<>(Arrays.asList(1, 3, 4)));

        complexModel.getPolygons().add(tri1);
        complexModel.getPolygons().add(tri2);

        // Удаляем вершину 2 (она только в первом треугольнике)
        VertexRemover.removeVertices(complexModel, Arrays.asList(2));

        // Проверяем переиндексацию
        assertEquals(3, complexModel.getVertices().size(), "Осталось 3 вершины");

        // Второй треугольник (1,3,4) должен стать (1,2,3) после переиндексации
        Polygon remainingPolygon = complexModel.getPolygons().get(0);
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getVertexIndices(),
                "Индексы должны быть пересчитаны после удаления вершины");
    }

    @Test
    void testEmptyVerticesListDoesNothing() { // Тест на пустой вход (граничный случай)
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Пытаемся удалить пустой список вершин
        VertexRemover.removeVertices(model, new ArrayList<>());

        // Ничего не должно измениться
        assertEquals(initialVertexCount, model.getVertices().size());
        assertEquals(initialPolygonCount, model.getPolygons().size());
    }

    @Test
    void testRemoveAllVertices() { // Тест полной очистки модели
        // Удаляем все вершины модели
        List<Integer> allVertices = Arrays.asList(1, 2, 3, 4);

        VertexRemover.removeVertices(model, allVertices);

        // Все вершины и полигоны должны быть удалены
        assertEquals(0, model.getVertices().size(), "Не должно остаться вершин");
        assertEquals(0, model.getPolygons().size(), "Не должно остаться полигонов");
        assertEquals(0, model.getNormals().size(), "Не должно остаться нормалей");
    }

    @Test
    void testInvalidVertexIndexIsIgnored() { // Тест на устойчивость к невалидным данным
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Пытаемся удалить несуществующую вершину
        List<Integer> invalidVertices = Arrays.asList(999, 1); // 999 - не существует

        VertexRemover.removeVertices(model, invalidVertices);

        // Вершина 1 должна быть удалена, 999 проигнорирована
        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(0, model.getPolygons().size()); // Полигон с вершиной 1 удален
    }

    @Test
    void testTextureAndNormalIndicesReindexing() { // Тест переиндексации связанных данных (нормалей)
        // Создаем модель с текстурными координатами и нормалями
        Model texturedModel = new Model();

        texturedModel.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0), // 1
                new Vector3f(1, 0, 0), // 2
                new Vector3f(0, 1, 0), // 3
                new Vector3f(1, 1, 0)  // 4 (добавляем 4ю вершину для сохранения полигона)
        ));

        texturedModel.getNormals().addAll(Arrays.asList(
                new Vector3f(0, 0, 1), // нормаль 1
                new Vector3f(0, 0, 1), // нормаль 2
                new Vector3f(0, 0, 1), // нормаль 3
                new Vector3f(0, 0, 1)  // нормаль 4
        ));

        // Создаем 2 полигона, чтобы при удалении одной вершины остался хотя бы один полигон
        Polygon poly1 = new Polygon();
        poly1.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        poly1.setNormalIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        Polygon poly2 = new Polygon();
        poly2.setVertexIndices(new ArrayList<>(Arrays.asList(2, 3, 4)));
        poly2.setNormalIndices(new ArrayList<>(Arrays.asList(2, 3, 4)));

        texturedModel.getPolygons().add(poly1);
        texturedModel.getPolygons().add(poly2);

        // Удаляем вершину 1 (она только в первом полигоне)
        VertexRemover.removeVertices(texturedModel, Arrays.asList(1));

        // Проверяем, что остался один полигон
        assertEquals(1, texturedModel.getPolygons().size());

        // Проверяем переиндексацию в оставшемся полигоне
        Polygon remainingPolygon = texturedModel.getPolygons().get(0);

        // После удаления вершины 1, индексы должны сместиться
        // Полигон был (2,3,4) должен стать (1,2,3)
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getVertexIndices());
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getNormalIndices());
    }
}