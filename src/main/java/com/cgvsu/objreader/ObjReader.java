package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	// Константы для ограничений
	private static final int MAX_VERTICES = 10000000;
	private static final int MAX_TEXTURE_COORDS = 10000000;
	private static final int MAX_NORMALS = 10000000;
	private static final int MAX_FACES = 10000000;
	private static final int MAX_FACE_VERTICES = 100;

	public static Model read(String fileContent) {
		if (fileContent == null) {
			throw new ObjReaderException("File content cannot be null", 0);
		}
		if (fileContent.trim().isEmpty()) {
			throw new ObjReaderException("File content cannot be empty", 0);
		}

		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);

		try {
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine().trim();
				lineInd++;

				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
				if (wordsInLine.isEmpty()) {
					continue;
				}

				final String token = wordsInLine.get(0);
				wordsInLine.remove(0);

				switch (token) {
					case OBJ_VERTEX_TOKEN -> {
						Vector3f vertex = parseVertex(wordsInLine, lineInd);
						result.getVertices().add(vertex);
						checkModelLimits(result, lineInd);
					}
					case OBJ_TEXTURE_TOKEN -> {
						Vector2f textureVertex = parseTextureVertex(wordsInLine, lineInd);
						result.getTextureVertices().add(textureVertex);
						checkModelLimits(result, lineInd);
					}
					case OBJ_NORMAL_TOKEN -> {
						Vector3f normal = parseNormal(wordsInLine, lineInd);
						result.getNormals().add(normal);
						checkModelLimits(result, lineInd);
					}
					case OBJ_FACE_TOKEN -> {
						Polygon polygon = parseFace(wordsInLine, lineInd, result);
						result.getPolygons().add(polygon);
						checkModelLimits(result, lineInd);
					}
					default -> {
						// Игнорируем неизвестные токены
						System.out.println("Ignoring unknown token on line " + lineInd + ": " + token);
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof ObjReaderException) {
				throw (ObjReaderException) e;
			} else {
				throw new ObjReaderException("Unexpected error: " + e.getMessage(), lineInd, e);
			}
		} finally {
			scanner.close();
		}

		// Проверяем, что у нас есть данные
		if (result.getVertices().isEmpty()) {
			throw new ObjReaderException("No vertices found in OBJ file", 0);
		}

		return result;
	}

	private static void checkModelLimits(Model model, int lineInd) {
		if (model.getVertices().size() > MAX_VERTICES) {
			throw new ObjReaderException(String.format("Too many vertices: %d (maximum: %d)",
					model.getVertices().size(), MAX_VERTICES), lineInd);
		}
		if (model.getTextureVertices().size() > MAX_TEXTURE_COORDS) {
			throw new ObjReaderException(String.format("Too many texture coordinates: %d (maximum: %d)",
					model.getTextureVertices().size(), MAX_TEXTURE_COORDS), lineInd);
		}
		if (model.getNormals().size() > MAX_NORMALS) {
			throw new ObjReaderException(String.format("Too many normals: %d (maximum: %d)",
					model.getNormals().size(), MAX_NORMALS), lineInd);
		}
		if (model.getPolygons().size() > MAX_FACES) {
			throw new ObjReaderException(String.format("Too many faces: %d (maximum: %d)",
					model.getPolygons().size(), MAX_FACES), lineInd);
		}
	}

	public static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() < 3) {
				throw new ObjReaderException("Expected 3 coordinates, got " +
						wordsInLineWithoutToken.size(), lineInd);
			}
			if (wordsInLineWithoutToken.size() > 4) {
				throw new ObjReaderException("Too many coordinates: expected 3-4, got " +
						wordsInLineWithoutToken.size(), lineInd);
			}

			for (int i = 0; i < 3; i++) {
				if (!isValidFloat(wordsInLineWithoutToken.get(i))) {
					throw new ObjReaderException(String.format("Invalid coordinate value '%s' at position %d",
							wordsInLineWithoutToken.get(i), i + 1), lineInd);
				}
			}

			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

			if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
				throw new ObjReaderException("Coordinates must be finite numbers", lineInd);
			}

			if (wordsInLineWithoutToken.size() == 4) {
				if (!isValidFloat(wordsInLineWithoutToken.get(3))) {
					throw new ObjReaderException(String.format("Invalid w coordinate '%s'",
							wordsInLineWithoutToken.get(3)), lineInd);
				}

				float w = Float.parseFloat(wordsInLineWithoutToken.get(3));
				if (Math.abs(w) < 1.0E-10F) {
					throw new ObjReaderException("W coordinate cannot be zero", lineInd);
				}

				x /= w;
				y /= w;
				z /= w;
			}

			return new Vector3f(x, y, z);

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value", lineInd, e);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() < 1) {
				throw new ObjReaderException("At least U coordinate is required", lineInd);
			}
			if (wordsInLineWithoutToken.size() > 3) {
				throw new ObjReaderException("Too many coordinates: expected 1-3, got " +
						wordsInLineWithoutToken.size(), lineInd);
			}

			if (!isValidFloat(wordsInLineWithoutToken.get(0))) {
				throw new ObjReaderException(String.format("Invalid U coordinate '%s'",
						wordsInLineWithoutToken.get(0)), lineInd);
			}

			float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float v = 0.0F;

			if (wordsInLineWithoutToken.size() >= 2) {
				if (!isValidFloat(wordsInLineWithoutToken.get(1))) {
					throw new ObjReaderException(String.format("Invalid V coordinate '%s'",
							wordsInLineWithoutToken.get(1)), lineInd);
				}
				v = Float.parseFloat(wordsInLineWithoutToken.get(1));
			}

			if (wordsInLineWithoutToken.size() == 3 && !isValidFloat(wordsInLineWithoutToken.get(2))) {
				throw new ObjReaderException(String.format("Invalid W coordinate '%s'",
						wordsInLineWithoutToken.get(2)), lineInd);
			}

			if (!Float.isFinite(u) || !Float.isFinite(v)) {
				throw new ObjReaderException("Texture coordinates must be finite numbers", lineInd);
			}

			return new Vector2f(u, v);

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value", lineInd, e);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() != 3) {
				throw new ObjReaderException("Expected exactly 3 components, got " +
						wordsInLineWithoutToken.size(), lineInd);
			}

			for (int i = 0; i < 3; i++) {
				if (!isValidFloat(wordsInLineWithoutToken.get(i))) {
					throw new ObjReaderException(String.format("Invalid normal component '%s' at position %d",
							wordsInLineWithoutToken.get(i), i + 1), lineInd);
				}
			}

			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

			if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
				throw new ObjReaderException("Normal components must be finite numbers", lineInd);
			}

			double lengthSquared = (double)(x * x + y * y + z * z);
			if (lengthSquared < 1.0E-12) {
				throw new ObjReaderException("Normal vector cannot be zero length", lineInd);
			}

			return new Vector3f(x, y, z);

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value", lineInd, e);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd, Model model) {
		if (wordsInLineWithoutToken.isEmpty()) {
			throw new ObjReaderException("Face must have at least one vertex", lineInd);
		}
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Face must have at least 3 vertices, got " +
					wordsInLineWithoutToken.size(), lineInd);
		}
		if (wordsInLineWithoutToken.size() > MAX_FACE_VERTICES) {
			throw new ObjReaderException(String.format("Face has too many vertices: %d (maximum: %d)",
					wordsInLineWithoutToken.size(), MAX_FACE_VERTICES), lineInd);
		}

		ArrayList<Integer> vertexIndices = new ArrayList<>();
		ArrayList<Integer> textureVertexIndices = new ArrayList<>();
		ArrayList<Integer> normalIndices = new ArrayList<>();

		Boolean hasTextures = null;
		Boolean hasNormals = null;

		for (int i = 0; i < wordsInLineWithoutToken.size(); i++) {
			String vertexData = wordsInLineWithoutToken.get(i);

			try {
				FaceVertex faceVertex = parseFaceVertex(vertexData, model, lineInd);
				vertexIndices.add(faceVertex.vertexIndex);

				if (hasTextures == null) {
					hasTextures = faceVertex.hasTexture;
				} else if (hasTextures != faceVertex.hasTexture) {
					throw new ObjReaderException(String.format("Inconsistent texture coordinate usage in face (vertex %d)",
							i + 1), lineInd);
				}

				if (hasNormals == null) {
					hasNormals = faceVertex.hasNormal;
				} else if (hasNormals != faceVertex.hasNormal) {
					throw new ObjReaderException(String.format("Inconsistent normal usage in face (vertex %d)",
							i + 1), lineInd);
				}

				if (faceVertex.hasTexture) {
					textureVertexIndices.add(faceVertex.textureIndex);
				}

				if (faceVertex.hasNormal) {
					normalIndices.add(faceVertex.normalIndex);
				}
			} catch (ObjReaderException e) {
				// Перебрасываем существующие ObjReaderException без изменений
				throw e;
			} catch (Exception e) {
				throw new ObjReaderException(String.format("Error parsing vertex %d: %s",
						i + 1, e.getMessage()), lineInd);
			}
		}

		Polygon polygon = new Polygon();
		polygon.setVertexIndices(vertexIndices);

		if (hasTextures != null && hasTextures) {
			polygon.setTextureVertexIndices(textureVertexIndices);
		}

		if (hasNormals != null && hasNormals) {
			polygon.setNormalIndices(normalIndices);
		}

		return polygon;
	}

	private static FaceVertex parseFaceVertex(String vertexData, Model model, int lineInd) {
		if (vertexData == null || vertexData.trim().isEmpty()) {
			throw new ObjReaderException("Vertex data cannot be empty", lineInd);
		}

		String[] components = vertexData.split("/", -1);
		if (components.length == 0 || components.length > 3) {
			throw new ObjReaderException(String.format("Invalid vertex format '%s'", vertexData), lineInd);
		}

		if (components[0].trim().isEmpty()) {
			throw new ObjReaderException("Vertex index cannot be empty", lineInd);
		}

		int vertexIndex;
		try {
			if (!isValidInteger(components[0])) {
				throw new ObjReaderException(String.format("Invalid vertex index '%s'", components[0]), lineInd);
			}

			int objVertexIndex = Integer.parseInt(components[0].trim());
			vertexIndex = convertObjIndex(objVertexIndex, model.getVertices().size(), "vertex", lineInd);
		} catch (NumberFormatException e) {
			throw new ObjReaderException(String.format("Cannot parse vertex index '%s'", components[0]), lineInd);
		}

		boolean hasTexture = false;
		int textureIndex = -1;
		if (components.length >= 2 && !components[1].trim().isEmpty()) {
			try {
				if (!isValidInteger(components[1])) {
					throw new ObjReaderException(String.format("Invalid texture index '%s'", components[1]), lineInd);
				}

				int objTextureIndex = Integer.parseInt(components[1].trim());
				textureIndex = convertObjIndex(objTextureIndex, model.getTextureVertices().size(), "texture", lineInd);
				hasTexture = true;
			} catch (NumberFormatException e) {
				throw new ObjReaderException(String.format("Cannot parse texture index '%s'", components[1]), lineInd);
			}
		}

		boolean hasNormal = false;
		int normalIndex = -1;
		if (components.length == 3 && !components[2].trim().isEmpty()) {
			try {
				if (!isValidInteger(components[2])) {
					throw new ObjReaderException(String.format("Invalid normal index '%s'", components[2]), lineInd);
				}

				int objNormalIndex = Integer.parseInt(components[2].trim());
				normalIndex = convertObjIndex(objNormalIndex, model.getNormals().size(), "normal", lineInd);
				hasNormal = true;
			} catch (NumberFormatException e) {
				throw new ObjReaderException(String.format("Cannot parse normal index '%s'", components[2]), lineInd);
			}
		}

		return new FaceVertex(vertexIndex, hasTexture, textureIndex, hasNormal, normalIndex);
	}

	private static int convertObjIndex(int objIndex, int size, String type, int lineInd) {
		if (objIndex == 0) {
			throw new ObjReaderException(type + " index cannot be zero", lineInd);
		}

		int index;
		if (objIndex > 0) {
			index = objIndex - 1;
		} else {
			index = size + objIndex;
		}

		if (index < 0 || index >= size) {
			throw new ObjReaderException(type + " index " + objIndex +
					" out of bounds. Must be between 1 and " + size +
					" or between -1 and -" + size, lineInd);
		}

		return index;
	}

	private static boolean isValidInteger(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		str = str.trim();
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isValidFloat(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		str = str.trim();
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static class FaceVertex {
		final int vertexIndex;
		final boolean hasTexture;
		final int textureIndex;
		final boolean hasNormal;
		final int normalIndex;

		FaceVertex(int vertexIndex, boolean hasTexture, int textureIndex, boolean hasNormal, int normalIndex) {
			this.vertexIndex = vertexIndex;
			this.hasTexture = hasTexture;
			this.textureIndex = textureIndex;
			this.hasNormal = hasNormal;
			this.normalIndex = normalIndex;
		}
	}
}