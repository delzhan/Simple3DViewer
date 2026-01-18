package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class ObjWriter {

    public static void write(Model model, String filePath) throws ObjWriterException {
        try {
            String content = modelToString(model);
            Files.writeString(Path.of(filePath), content);
        } catch (IOException e) {
            throw new ObjWriterException("Failed to write OBJ file to: " + filePath, -1, e);
        }
    }

    public static String modelToString(Model model) {
        return modelToString(model, "Exported by Lapin Nikita ObjWriter");
    }

    public static String modelToString(Model model, String comment) {
        if (model == null) {
            throw new ObjWriterException("Model cannot be null", 1);
        }

        StringBuilder sb = new StringBuilder();
        int currentLine = 1; // Счетчик строк

        if (comment != null && !comment.isEmpty()) {
            sb.append("# ").append(comment).append("\n");
            currentLine++;
        }

        try {
            List<Vector3f> vertices = model.getVertices();
            for (int i = 0; i < vertices.size(); i++) {
                Vector3f vertex = vertices.get(i);
                validateVertex(vertex, i, currentLine);
                sb.append("v ")
                        .append(formatFloatCompact(vertex.getX(), currentLine))
                        .append(" ")
                        .append(formatFloatCompact(vertex.getY(), currentLine))
                        .append(" ")
                        .append(formatFloatCompact(vertex.getZ(), currentLine))
                        .append("\n");
                currentLine++;
            }

            if (!vertices.isEmpty() &&
                    ((model.getTextureVertices() != null && !model.getTextureVertices().isEmpty()) ||
                            (model.getNormals() != null && !model.getNormals().isEmpty()))) {
                sb.append("\n");
                currentLine++;
            }

            List<Vector2f> textureVertices = model.getTextureVertices();
            if (textureVertices != null) {
                for (int i = 0; i < textureVertices.size(); i++) {
                    Vector2f textureVertex = textureVertices.get(i);
                    validateTextureVertex(textureVertex, i, currentLine);
                    sb.append("vt ")
                            .append(formatFloatCompact(textureVertex.getX(), currentLine))
                            .append(" ")
                            .append(formatFloatCompact(textureVertex.getY(), currentLine))
                            .append("\n");
                    currentLine++;
                }
            }

            if (textureVertices != null && !textureVertices.isEmpty() &&
                    model.getNormals() != null && !model.getNormals().isEmpty()) {
                sb.append("\n");
                currentLine++;
            }

            List<Vector3f> normals = model.getNormals();
            if (normals != null) {
                for (int i = 0; i < normals.size(); i++) {
                    Vector3f normal = normals.get(i);
                    validateNormal(normal, i, currentLine);
                    sb.append("vn ")
                            .append(formatFloatCompact(normal.getX(), currentLine))
                            .append(" ")
                            .append(formatFloatCompact(normal.getY(), currentLine))
                            .append(" ")
                            .append(formatFloatCompact(normal.getZ(), currentLine))
                            .append("\n");
                    currentLine++;
                }
            }

            if ((!vertices.isEmpty() ||
                    (textureVertices != null && !textureVertices.isEmpty()) ||
                    (normals != null && !normals.isEmpty())) &&
                    !model.getPolygons().isEmpty()) {
                sb.append("\n");
                currentLine++;
            }

            List<Polygon> polygons = model.getPolygons();
            for (int i = 0; i < polygons.size(); i++) {
                Polygon polygon = polygons.get(i);
                validatePolygon(polygon, i, currentLine,
                        vertices.size(),
                        textureVertices != null ? textureVertices.size() : 0,
                        normals != null ? normals.size() : 0);

                sb.append("f");
                List<Integer> vertexIndices = polygon.getVertexIndices();
                List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
                List<Integer> normalIndices = polygon.getNormalIndices();

                boolean hasTextures = textureVertexIndices != null && !textureVertexIndices.isEmpty();
                boolean hasNormals = normalIndices != null && !normalIndices.isEmpty();

                for (int j = 0; j < vertexIndices.size(); j++) {
                    sb.append(" ");
                    sb.append(vertexIndices.get(j) + 1);

                    if (hasTextures || hasNormals) {
                        sb.append("/");

                        if (hasTextures) {
                            sb.append(textureVertexIndices.get(j) + 1);
                        }

                        if (hasNormals) {
                            sb.append("/").append(normalIndices.get(j) + 1);
                        }
                    }
                }
                sb.append("\n");
                currentLine++;
            }

        } catch (IndexOutOfBoundsException e) {
            throw new ObjWriterException("Invalid model data structure", currentLine, e);
        } catch (NullPointerException e) {
            throw new ObjWriterException("Model contains null elements", currentLine, e);
        }

        return sb.toString();
    }

    protected static String formatFloatCompact(float value, int lineIndex) {
        if (Float.isNaN(value)) {
            throw new ObjWriterException("Cannot format NaN value", lineIndex);
        }
        if (Float.isInfinite(value)) {
            throw new ObjWriterException("Cannot format infinite value", lineIndex);
        }

        String result = String.format(Locale.ROOT, "%.6f", value);

        if (result.contains(".")) {
            result = result.replaceAll("0*$", "");
            if (result.endsWith(".")) {
                result = result.substring(0, result.length() - 1);
            }
        }

        return result;
    }

    protected static void validateVertex(Vector3f vertex, int index, int lineIndex) {
        if (vertex == null) {
            throw new ObjWriterException("Vertex at index " + index + " is null", lineIndex);
        }
        if (Float.isNaN(vertex.getX()) || Float.isNaN(vertex.getY()) || Float.isNaN(vertex.getZ())) {
            throw new ObjWriterException("Vertex at index " + index + " contains NaN values", lineIndex);
        }
        if (Float.isInfinite(vertex.getX()) || Float.isInfinite(vertex.getY()) || Float.isInfinite(vertex.getZ())) {
            throw new ObjWriterException("Vertex at index " + index + " contains infinite values", lineIndex);
        }
    }

    protected static void validateTextureVertex(Vector2f textureVertex, int index, int lineIndex) {
        if (textureVertex == null) {
            throw new ObjWriterException("Texture vertex at index " + index + " is null", lineIndex);
        }
        if (Float.isNaN(textureVertex.getX()) || Float.isNaN(textureVertex.getY())) {
            throw new ObjWriterException("Texture vertex at index " + index + " contains NaN values", lineIndex);
        }
        if (Float.isInfinite(textureVertex.getX()) || Float.isInfinite(textureVertex.getY())) {
            throw new ObjWriterException("Texture vertex at index " + index + " contains infinite values", lineIndex);
        }
    }

    protected static void validateNormal(Vector3f normal, int index, int lineIndex) {
        if (normal == null) {
            throw new ObjWriterException("Normal at index " + index + " is null", lineIndex);
        }
        if (Float.isNaN(normal.getX()) || Float.isNaN(normal.getY()) || Float.isNaN(normal.getZ())) {
            throw new ObjWriterException("Normal at index " + index + " contains NaN values", lineIndex);
        }
        if (Float.isInfinite(normal.getX()) || Float.isInfinite(normal.getY()) || Float.isInfinite(normal.getZ())) {
            throw new ObjWriterException("Normal at index " + index + " contains infinite values", lineIndex);
        }
    }

    protected static void validatePolygon(Polygon polygon, int polyIndex, int lineIndex, int vertexCount,
                                          int textureVertexCount, int normalCount) {
        if (polygon == null) {
            throw new ObjWriterException("Polygon at index " + polyIndex + " is null", lineIndex);
        }

        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();

        if (vertexIndices == null) {
            throw new ObjWriterException("Polygon at index " + polyIndex + " has null vertex indices", lineIndex);
        }

        if (vertexIndices.isEmpty()) {
            throw new ObjWriterException("Polygon at index " + polyIndex + " has no vertices", lineIndex);
        }

        if (vertexIndices.size() < 3) {
            throw new ObjWriterException("Polygon at index " + polyIndex + " has less than 3 vertices", lineIndex);
        }

        for (int vertexIndex : vertexIndices) {
            if (vertexIndex < 0 || vertexIndex >= vertexCount) {
                throw new ObjWriterException(
                        "Polygon at index " + polyIndex + " references invalid vertex index " +
                                vertexIndex + " (available vertices: 0-" + (vertexCount - 1) + ")",
                        lineIndex
                );
            }
        }

        if (textureVertexIndices != null && !textureVertexIndices.isEmpty()) {
            if (textureVertexIndices.size() != vertexIndices.size()) {
                throw new ObjWriterException(
                        "Polygon at index " + polyIndex + " has mismatched vertex and texture vertex counts",
                        lineIndex
                );
            }

            for (int texIndex : textureVertexIndices) {
                if (texIndex < 0 || texIndex >= textureVertexCount) {
                    throw new ObjWriterException(
                            "Polygon at index " + polyIndex + " references invalid texture vertex index " +
                                    texIndex + " (available texture vertices: 0-" + (textureVertexCount - 1) + ")",
                            lineIndex
                    );
                }
            }
        }

        if (normalIndices != null && !normalIndices.isEmpty()) {
            if (normalIndices.size() != vertexIndices.size()) {
                throw new ObjWriterException(
                        "Polygon at index " + polyIndex + " has mismatched vertex and normal counts",
                        lineIndex
                );
            }

            for (int normalIndex : normalIndices) {
                if (normalIndex < 0 || normalIndex >= normalCount) {
                    throw new ObjWriterException(
                            "Polygon at index " + polyIndex + " references invalid normal index " +
                                    normalIndex + " (available normals: 0-" + (normalCount - 1) + ")",
                            lineIndex
                    );
                }
            }
        }
    }
}