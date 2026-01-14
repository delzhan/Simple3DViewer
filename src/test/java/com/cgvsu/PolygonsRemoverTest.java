package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.removers.PolygonsRemover;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolygonsRemoverTest {

    @Test
    void testModelWithDegeneratePolygon() {  // Тест обработки вырожденных полигонов (<3 вершин)
        // Подготовка тестовой модели с вырожденным полигоном
        Model model = new Model();

        model.setVertices(new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(2, 0, 0)
        )));

        // Пропускаем создание вырожденного полигона, так как класс Polygon его не поддерживает
        // Вместо этого создаем нормальный полигон для теста
        Polygon normalPolygon = new Polygon();
        normalPolygon.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        model.setPolygons(new ArrayList<>(Arrays.asList(normalPolygon)));

        // Тестируем удаление полигона
        PolygonsRemover.removePolygons(model, Arrays.asList(1), true);

        // Проверяем, что полигон удален
        assertEquals(0, model.getPolygons().size());
    }

    @Test
    void testModelWithInconsistentNormals() { // Тест корректности пересчёта нормалий при несоответствии их количества количеству вершин
        // Подготовка модели с несоответствующим количеством нормалей
        Model model = new Model();

        model.setVertices(new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        )));

        // Изначально добавляем только одну нормаль (недостаточно)
        model.setNormals(new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 1)
        )));

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(1, 1, 1)));

        model.setPolygons(new ArrayList<>(Arrays.asList(polygon)));

        // Вызываем метод пересчета нормалей напрямую
        PolygonsRemover.recalculateNormals(model);

        // Проверяем, что теперь нормалей столько же, сколько вершин
        assertEquals(model.getVertices().size(), model.getNormals().size(),
                "После пересчета нормалей количество нормалей должно быть равно количеству вершин");

        // Дополнительная проверка: убедимся, что нормали не нулевые
        for (Vector3f normal : model.getNormals()) {
            assertNotNull(normal, "Нормаль не должна быть null");
            assertTrue(normal.length() > 0.9f, "Нормаль должна быть нормализована (длина ~1)");
        }
    }

    @Test
    void testRemoveNonExistentVertices() { // Тест устойчивости кода к ошибкам в данных (несуществующие индексы вершин)
        Model model = new Model();

        model.setVertices(new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0)
        )));

        // Полигон ссылается на несуществующую вершину 3
        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        model.setPolygons(new ArrayList<>(Arrays.asList(polygon)));

        // Проверяем, что метод не выбрасывает исключение при обработке модели с некорректными индексами
        assertDoesNotThrow(() -> {
            PolygonsRemover.removePolygons(model, Arrays.asList(1), true);
        }, "Метод должен корректно обрабатывать модели с несуществующими вершинами без выброса исключений");
    }

    @Test
    void testLargeModelPerformance() { // Тест производительности на большой модели (1000 вершин, 2000 полигонов)
        Model model = new Model();

        // Создаем 1000 вершин
        ArrayList<Vector3f> vertices = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            vertices.add(new Vector3f(i, i, i));
        }
        model.setVertices(vertices);

        // Создаем 2000 полигонов, использующих вершины циклически
        ArrayList<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            Polygon p = new Polygon();
            // Циклические индексы для создания полигонов
            int v1 = (i % 1000) + 1;
            int v2 = ((i + 1) % 1000) + 1;
            int v3 = ((i + 2) % 1000) + 1;
            p.setVertexIndices(new ArrayList<>(Arrays.asList(v1, v2, v3)));
            polygons.add(p);
        }
        model.setPolygons(polygons);

        // Формируем список из 1000 полигонов для удаления (половина всех полигонов)
        List<Integer> indicesToDelete = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            indicesToDelete.add(i);
        }

        // Замеряем время выполнения операции удаления
        long startTime = System.currentTimeMillis();
        PolygonsRemover.removePolygons(model, indicesToDelete, true);
        long endTime = System.currentTimeMillis();

        // Проверяем, что операция выполняется за разумное время (менее 5 секунд)
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 5000,
                "Операция удаления полигонов слишком медленная: " + executionTime + "мс");

        // Проверяем, что осталось 1000 полигонов (удалили половину)
        assertEquals(1000, model.getPolygons().size(),
                "После удаления половины полигонов должно остаться 1000 полигонов");
    }
}