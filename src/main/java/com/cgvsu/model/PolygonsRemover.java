package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.*;

public class PolygonsRemover {
    /**
     * Удаляет полигоны и (опционально) свободные вершины
     */
    public static void removePolygons(Model model, List<Integer> polygonIndicesToDelete,
                                      boolean removeOrphanedVertices) {
        if (model == null || polygonIndicesToDelete == null || polygonIndicesToDelete.isEmpty()) {
            return;
        }

        // Удаляем дубликаты индексов
        List<Integer> uniqueIndices = new ArrayList<>(new LinkedHashSet<>(polygonIndicesToDelete));

        // Сортируем по убыванию для безопасного удаления
        uniqueIndices.sort(Collections.reverseOrder());

        // Удаляем выбранные полигоны
        for (int polyIndex : uniqueIndices) {
            int internalIndex = polyIndex - 1; // OBJ -> внутренний индекс
            if (internalIndex >= 0 && internalIndex < model.getPolygons().size()) {
                model.getPolygons().remove(internalIndex);
            }
        }

        // Если нужно - находим и удаляем свободные вершины
        if (removeOrphanedVertices) {
            removeOrphanedVertices(model);
        }

        recalculateNormals(model);
    }

    /**
     * Находит и удаляет вершины, не принадлежащие ни одному полигону
     */
    private static void removeOrphanedVertices(Model model) {
        if (model.getPolygons().isEmpty()) {
            // Если нет полигонов, все вершины становятся свободными
            model.getVertices().clear();
            model.getNormals().clear();
            return;
        }

        // Собираем все индексы вершин, используемые в полигонах
        Set<Integer> usedVertexIndices = new HashSet<>();
        for (Polygon polygon : model.getPolygons()) {
            usedVertexIndices.addAll(polygon.getVertexIndices());
        }

        // Находим индексы вершин, которые не используются
        List<Integer> orphanedIndices = new ArrayList<>();
        for (int i = 1; i <= model.getVertices().size(); i++) {
            if (!usedVertexIndices.contains(i)) {
                orphanedIndices.add(i);
            }
        }

        // Удаляем свободные вершины (с конца, чтобы не сбивались индексы)
        orphanedIndices.sort(Collections.reverseOrder());
        for (int vertexIndex : orphanedIndices) {
            int internalIndex = vertexIndex - 1;
            if (internalIndex >= 0 && internalIndex < model.getVertices().size()) {
                model.getVertices().remove(internalIndex);
                // Удаляем соответствующую нормаль, если есть
                if (internalIndex < model.getNormals().size()) {
                    model.getNormals().remove(internalIndex);
                }
            }
        }

        if (!orphanedIndices.isEmpty()) {
            reindexPolygonsAfterVertexRemoval(model, orphanedIndices);
        }
    }

    /**
     * Переиндексация полигонов после удаления вершин
     */
    private static void reindexPolygonsAfterVertexRemoval(Model model, List<Integer> deletedVertexIndices) {
        for (Polygon polygon : model.getPolygons()) {
            // Переиндексируем вершины
            List<Integer> newVertexIndices = new ArrayList<>();
            for (int oldIndex : polygon.getVertexIndices()) {
                int shift = 0;
                for (int deletedIndex : deletedVertexIndices) {
                    if (oldIndex > deletedIndex) {
                        shift++;
                    }
                }
                newVertexIndices.add(oldIndex - shift);
            }
            polygon.setVertexIndices(new ArrayList<>(newVertexIndices));

            // Переиндексируем нормали, если есть
            if (polygon.getNormalIndices() != null && !polygon.getNormalIndices().isEmpty()) {
                List<Integer> newNormalIndices = new ArrayList<>();
                for (int oldIndex : polygon.getNormalIndices()) {
                    int shift = 0;
                    for (int deletedIndex : deletedVertexIndices) {
                        if (oldIndex > deletedIndex) {
                            shift++;
                        }
                    }
                    newNormalIndices.add(oldIndex - shift);
                }
                polygon.setNormalIndices(new ArrayList<>(newNormalIndices));
            }
        }
    }

    /**
     * Пересчёт нормалей
     */
    public static void recalculateNormals(Model model) {
        model.getNormals().clear();

        if (model.getVertices().isEmpty() || model.getPolygons().isEmpty()) {
            return;
        }

        // Временный список для сумм нормалей
        List<Vector3f> vertexNormalSums = new ArrayList<>();
        for (int i = 0; i < model.getVertices().size(); i++) {
            vertexNormalSums.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем нормали для каждого полигона
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            // Берём первые три вершины полигона
            Vector3f v0 = model.getVertices().get(vertexIndices.get(0) - 1);
            Vector3f v1 = model.getVertices().get(vertexIndices.get(1) - 1);
            Vector3f v2 = model.getVertices().get(vertexIndices.get(2) - 1);

            // Вычисляем нормаль полигона через векторное произведение
            Vector3f edge1 = v1.sub(v0);
            Vector3f edge2 = v2.sub(v0);
            Vector3f faceNormal = edge1.cross(edge2).normalizeV();

            // Добавляем к каждой вершине полигона
            for (int vertexIndex : vertexIndices) {
                int internalIndex = vertexIndex - 1;
                Vector3f currentSum = vertexNormalSums.get(internalIndex);
                vertexNormalSums.set(internalIndex, currentSum.add(faceNormal));
            }
        }

        // Нормализуем и сохраняем
        for (Vector3f sum : vertexNormalSums) {
            model.getNormals().add(sum.normalizeV());
        }

        // Обновляем индексы нормалей в полигонах
        for (Polygon polygon : model.getPolygons()) {
            polygon.setNormalIndices(new ArrayList<>(polygon.getVertexIndices()));
        }
    }

    /**
     * Вспомогательный метод: находит все свободные вершины
     */
    public static List<Integer> findOrphanedVertices(Model model) {
        List<Integer> orphanedIndices = new ArrayList<>();

        if (model.getPolygons().isEmpty()) {
            // Все вершины свободные
            for (int i = 1; i <= model.getVertices().size(); i++) {
                orphanedIndices.add(i);
            }
            return orphanedIndices;
        }

        Set<Integer> usedVertexIndices = new HashSet<>();
        for (Polygon polygon : model.getPolygons()) {
            usedVertexIndices.addAll(polygon.getVertexIndices());
        }

        for (int i = 1; i <= model.getVertices().size(); i++) {
            if (!usedVertexIndices.contains(i)) {
                orphanedIndices.add(i);
            }
        }

        return orphanedIndices;
    }
}