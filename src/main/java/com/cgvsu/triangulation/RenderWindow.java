package com.cgvsu.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class RenderWindow extends JFrame {
    private TriangulatedModel model;
    private Rasterizer rasterizer;
    private TextureShader texture;
    private Timer repaintTimer;
    private boolean drawWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;
    private Color staticColor = Color.WHITE;

    private JPanel renderPanel;
    private JCheckBox wireframeCheckBox;
    private JCheckBox textureCheckBox;
    private JCheckBox lightingCheckBox;
    private JButton colorButton;

    public RenderWindow(TriangulatedModel model) {
        this.model = model;
        rasterizer = new Rasterizer(800, 600);
        texture = new TextureShader();

        repaintTimer = new Timer(100, e -> renderPanel.repaint());
        repaintTimer.start();


        setTitle("3D Model Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                render();
                BufferedImage image = rasterizer.getImage();
                g.drawImage(image, 0, 0, null);
            }
        };
        renderPanel.setPreferredSize(new Dimension(800, 600));

        JPanel controlPanel = new JPanel();
        wireframeCheckBox = new JCheckBox("Рисовать полигональную сетку");
        textureCheckBox = new JCheckBox("Использовать текстуру");
        lightingCheckBox = new JCheckBox("Использовать освещение");
        colorButton = new JButton("Выбрать цвет");

        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(RenderWindow.this, "Выберите цвет", staticColor);
                if (newColor != null) {
                    staticColor = newColor;
                    renderPanel.repaint();
                }
            }
        });

        wireframeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawWireframe = wireframeCheckBox.isSelected();
                renderPanel.repaint();
            }
        });

        textureCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useTexture = textureCheckBox.isSelected();
                renderPanel.repaint();
            }
        });

        lightingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useLighting = lightingCheckBox.isSelected();
                renderPanel.repaint();
            }
        });

        controlPanel.add(wireframeCheckBox);
        controlPanel.add(textureCheckBox);
        controlPanel.add(lightingCheckBox);
        controlPanel.add(colorButton);

        JButton loadTextureButton = new JButton("Загрузить текстуру");
        loadTextureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(RenderWindow.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        texture = new TextureShader(fileChooser.getSelectedFile().getPath());
                        useTexture = true;
                        textureCheckBox.setSelected(true);
                        renderPanel.repaint();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(RenderWindow.this, "Ошибка загрузки текстуры");
                    }
                }
            }
        });
        controlPanel.add(loadTextureButton);

        add(renderPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void render() {
        rasterizer.clear();

        if (model == null || model.getPolygons() == null) return;

        for (Polygon polygon : model.getPolygons()) {
            if (polygon.getVertexIndices().size() < 3) continue;

            Vertex v0 = transformVertex(polygon, 0);
            Vertex v1 = transformVertex(polygon, 1);
            Vertex v2 = transformVertex(polygon, 2);

            rasterizer.rasterizeTriangle(v0, v1, v2);

            if (drawWireframe) {
                drawWireframeTriangle(v0, v1, v2);
            }
        }
    }

    private Vertex transformVertex(Polygon polygon, int vertexIndex) {
        if (vertexIndex >= polygon.getVertexIndices().size()) {
            return new Vertex(0, 0, 0, Color.BLACK.getRGB());
        }

        int vertexIdx = polygon.getVertexIndices().get(vertexIndex);

        float x = 0, y = 0, z = 0;
        if (model.getVertices() != null && vertexIdx * 3 + 2 < model.getVertices().size()) {
            x = model.getVertices().get(vertexIdx * 3);
            y = model.getVertices().get(vertexIdx * 3 + 1);
            z = model.getVertices().get(vertexIdx * 3 + 2);
        }

        // Исправляем преобразование координат для лучшего отображения
        int screenX = (int) ((x + 2.0f) * 200);  // Смещение и масштабирование
        int screenY = (int) ((y + 2.0f) * 200);
        float screenZ = z;

        // Для отладки: используем фиксированный цвет
        int color = Color.RED.getRGB();

        // Простое затенение для теста
        if (useLighting) {
            Vector3f lightDir = new Vector3f(0, 0, -1);
            float nx = 0, ny = 0, nz = 1;  // Простая нормаль
            Vector3f normal = new Vector3f(nx, ny, nz);
            normal.normalizeV();
            float dot = Math.max(normal.dot(lightDir), 0.2f);  // Минимальная яркость 0.2

            int r = (int) (Color.RED.getRed() * dot);
            int g = (int) (Color.RED.getGreen() * dot);
            int b = (int) (Color.RED.getBlue() * dot);
            color = new Color(r, g, b).getRGB();
        }

        return new Vertex(screenX, screenY, screenZ, color);
    }

    private void drawWireframeTriangle(Vertex v0, Vertex v1, Vertex v2) {
        rasterizer.drawLineBresenham(
                new float[]{v0.x, v0.y},
                new float[]{v1.x, v1.y},
                Color.BLACK
        );
        rasterizer.drawLineBresenham(
                new float[]{v1.x, v1.y},
                new float[]{v2.x, v2.y},
                Color.BLACK
        );
        rasterizer.drawLineBresenham(
                new float[]{v2.x, v2.y},
                new float[]{v0.x, v0.y},
                Color.BLACK
        );
    }

    @Override
    public void dispose() {
        if (repaintTimer != null) {
            repaintTimer.stop();
        }
        super.dispose();
    }
}