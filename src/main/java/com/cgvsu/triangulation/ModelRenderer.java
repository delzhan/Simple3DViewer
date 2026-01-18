//package com.cgvsu.triangulation;
//
//import com.cgvsu.math.Vector3f;
//import com.cgvsu.math.Vector4f;
//import com.cgvsu.model.Polygon;
//
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.util.List;
//
//public class ModelRenderer {
//    private TriangulatedModel model;
//    private Rasterizer rasterizer;
//    private TextureShader shader;
//    private RenderSettings settings;
//
//    public static class RenderSettings {
//        public boolean drawWireframe = false;
//        public boolean useTexture = false;
//        public boolean useLighting = true;
//        public Color staticColor = Color.WHITE;
//        public Vector3f cameraPosition = new Vector3f(0, 0, 5);
//        public Vector3f cameraTarget = new Vector3f(0, 0, 0);
//        public float fov = 60.0f;
//        public float nearPlane = 0.1f;
//        public float farPlane = 100.0f;
//    }
//
//    public ModelRenderer(int width, int height) {
//        this.rasterizer = new Rasterizer(width, height);
//        this.shader = new TextureShader();
//        this.settings = new RenderSettings();
//    }
//
//    public void setModel(TriangulatedModel model) {
//        this.model = model;
//    }
//
//    public void setSettings(RenderSettings settings) {
//        this.settings = settings;
//        Vector3f lightDir = settings.cameraTarget.sub(settings.cameraPosition);
//        lightDir.normalize();
//        shader.setLightDirection(lightDir);
//    }
//
//    public BufferedImage render() {
//        if (model == null) {
//            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
//        }
//
//        rasterizer.clearBuffers();
//        List<Polygon> triangles = model.getPolygons();
//        List<Float> vertices = model.getVertices();
//        List<Float> normals = model.getNormals();
//        List<Float> texCoords = model.getTextureVertices();
//
//        for (Polygon triangle : triangles) {
//            List<Integer> vertexIndices = triangle.getVertexIndices();
//            List<Integer> normalIndices = triangle.getNormalIndices();
//            List<Integer> texIndices = triangle.getTextureVertexIndices();
//
//            float[][] screenVerts = new float[3][3];
//            float[] depths = new float[3];
//            Color[] vertexColors = new Color[3];
//            float[][] vertexNormals = new float[3][3];
//            float[][] vertexTexCoords = new float[3][2];
//
//            for (int i = 0; i < 3; i++) {
//                int vIdx = vertexIndices.get(i) * 3;
//                screenVerts[i] = projectToScreen(
//                        vertices.get(vIdx), vertices.get(vIdx + 1), vertices.get(vIdx + 2)
//                );
//                depths[i] = vertices.get(vIdx + 2);
//
//                if (settings.useLighting && normalIndices != null && !normalIndices.isEmpty() &&
//                        normals != null && !normals.isEmpty()) {
//                    int nIdx = normalIndices.get(i) * 3;
//                    if (nIdx + 2 < normals.size()) {
//                        vertexNormals[i][0] = normals.get(nIdx);
//                        vertexNormals[i][1] = normals.get(nIdx + 1);
//                        vertexNormals[i][2] = normals.get(nIdx + 2);
//                    }
//                } else {
//                    vertexNormals[i][0] = 0;
//                    vertexNormals[i][1] = 0;
//                    vertexNormals[i][2] = 1;
//                }
//
//                if (settings.useTexture && texIndices != null && !texIndices.isEmpty() &&
//                        texCoords != null && !texCoords.isEmpty()) {
//                    int tIdx = texIndices.get(i) * 2;
//                    if (tIdx + 1 < texCoords.size()) {
//                        vertexTexCoords[i][0] = texCoords.get(tIdx);
//                        vertexTexCoords[i][1] = texCoords.get(tIdx + 1);
//                    }
//                }
//
//                vertexColors[i] = calculateVertexColor(vertexNormals[i], vertexTexCoords[i]);
//            }
//
//            if (!settings.drawWireframe) {
//                rasterizer.rasterizeTriangle(
//                        screenVerts[0], screenVerts[1], screenVerts[2],
//                        vertexColors, depths
//                );
//            }
//
//            if (settings.drawWireframe) {
//                rasterizer.drawWireframe(
//                        screenVerts[0], screenVerts[1], screenVerts[2],
//                        Color.RED
//                );
//            }
//        }
//
//        return rasterizer.getImage();
//    }
//
//    private Color calculateVertexColor(float[] normal, float[] texCoord) {
//        Color color = settings.staticColor;
//
//        if (settings.useLighting) {
//            Vector3f normalVec = new Vector3f(normal[0], normal[1], normal[2]);
//            color = shader.shadePixel(texCoord[0], texCoord[1], normalVec, color);
//        }
//
//        return color;
//    }
//
//    private float[] projectToScreen(float x, float y, float z) {
//        // Преобразование в координаты экрана
//        float aspect = (float) rasterizer.getWidth() / rasterizer.getHeight();
//        float fovRad = (float) Math.toRadians(settings.fov);
//        float f = (float) (1.0 / Math.tan(fovRad / 2.0));
//
//        Vector4f point = new Vector4f(x, y, z, 1.0f);
//
//        // Простая перспективная проекция
//        float px = point.getX() * f / aspect;
//        float py = point.getY() * f;
//        float pz = point.getZ();
//
//        // Преобразование в координаты экрана
//        float screenX = (px + 1) * rasterizer.getWidth() / 2;
//        float screenY = (1 - py) * rasterizer.getHeight() / 2;
//
//        return new float[]{screenX, screenY, pz};
//    }
//
//    public void setTexture(BufferedImage texture) {
//        shader.setTexture(texture);
//    }
//}