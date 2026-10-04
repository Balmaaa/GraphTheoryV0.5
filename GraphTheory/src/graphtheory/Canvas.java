package graphtheory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

public class Canvas {

    public JFrame frame;
    private CanvasPane canvas;
    private Graphics2D graphic;
    private Image canvasImage, canvasImage2;
    private int selectedTool = 1; // 1: Node, 2: Edge, 3: Move, 4: Delete
    private int selectedWindow = 0; // 0: Studio, 1: Matrices, 2: Analytics
    private int clickedVertexIndex = -1;
    private FileManager fileManager = new FileManager();

    private Vector<Vertex> vertexList = new Vector<Vertex>();
    private Vector<Edge> edgeList = new Vector<Edge>();
    private GraphProperties gP = new GraphProperties();
    private boolean directedMode = false;
    private Vector<String> propertyLines = new Vector<String>();

    // Modern Palette
    public static final Color BG_DARK = new Color(15, 17, 23);
    public static final Color SIDEBAR_BG = new Color(22, 26, 36);
    public static final Color CARD_BG = new Color(30, 35, 48);
    public static final Color CARD_BORDER = new Color(45, 52, 70);
    public static final Color ACCENT_CYAN = new Color(0, 210, 255);
    public static final Color ACCENT_PINK = new Color(255, 51, 102);
    public static final Color TEXT_PRIMARY = new Color(245, 247, 250);
    public static final Color TEXT_MUTED = new Color(140, 148, 168);

    public Canvas(String title, int width, int height, Color bgColour) {
        frame = new JFrame("GraphStudio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        canvas = new CanvasPane();
        InputListener listener = new InputListener();
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);

        canvas.setPreferredSize(new Dimension(width, height));
        frame.setContentPane(canvas);
        frame.pack();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width - width) / 2, (screen.height - height) / 2);
        setVisible(true);
    }

    class InputListener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY();

            // 1. Sidebar Navigation
            if (x < 220) {
                if (y >= 90 && y <= 125) selectedWindow = 0;
                else if (y >= 135 && y <= 170) { selectedWindow = 1; buildProperties(); }
                else if (y >= 180 && y <= 215) selectedWindow = 2;
                
                // File Operations (Sidebar Action Buttons)
                else if (y >= 250 && y <= 280) handleOpenFile();
                else if (y >= 290 && y <= 320) handleSaveFile();
                
                refresh();
                return;
            }

            // 2. Canvas Floating Tools & Extras
            if (selectedWindow == 0 && y >= 18 && y <= 58) {
                // Main Tools
                if (x >= 240 && x < 320) selectedTool = 1;
                else if (x >= 320 && x < 400) selectedTool = 2;
                else if (x >= 400 && x < 480) selectedTool = 3;
                else if (x >= 480 && x < 560) selectedTool = 4;
                else if (x >= 560 && x < 650) directedMode = !directedMode;

                // Extras Menu Actions
                else if (x >= 670 && x < 760) autoArrangeVertices();
                else if (x >= 760 && x < 850) highlightIsolatedNodes();
                else if (x >= 850 && x < 930) highlightCutpoints();
                else if (x >= 930 && x < 1010) highlightBridges();
                else if (x >= 1010 && x < 1090) clearAll();

                erase();
                refresh();
                return;
            }

            // 3. Studio Canvas Interaction
            if (selectedWindow == 0 && x > 230 && y > 70) {
                if (selectedTool == 1) {
                    Vertex v = new Vertex(nextVertexName(), x, y);
                    vertexList.add(v);
                }
            }
            erase();
            refresh();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (selectedWindow == 0 && e.getX() > 230 && e.getY() > 70) {
                if (selectedTool == 4) { // Delete Tool
                    Vertex target = null;
                    for (Vertex v : vertexList) if (v.hasIntersection(e.getX(), e.getY())) target = v;
                    if (target != null) removeVertex(target);
                    else {
                        for (Edge d : edgeList) {
                            if (d.hasIntersection(e.getX(), e.getY())) { removeEdge(d); break; }
                        }
                    }
                } else if (selectedTool == 2 || selectedTool == 3) {
                    for (Vertex v : vertexList) {
                        if (v.hasIntersection(e.getX(), e.getY())) {
                            v.wasClicked = true;
                            clickedVertexIndex = vertexList.indexOf(v);
                        } else v.wasClicked = false;
                    }
                }
            }
            erase();
            refresh();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedWindow == 0 && selectedTool == 2 && clickedVertexIndex >= 0 && clickedVertexIndex < vertexList.size()) {
                Vertex parentV = vertexList.get(clickedVertexIndex);
                for (Vertex v : vertexList) {
                    if (v.hasIntersection(e.getX(), e.getY()) && v != parentV && !v.connectedToVertex(parentV)) {
                        Edge edge = new Edge(parentV, v, directedMode);
                        v.addVertex(parentV);
                        parentV.addVertex(v);
                        edgeList.add(edge);
                    }
                    v.wasClicked = false;
                }
            }
            if (clickedVertexIndex >= 0 && clickedVertexIndex < vertexList.size()) {
                vertexList.get(clickedVertexIndex).wasClicked = false;
            }
            clickedVertexIndex = -1;
            erase();
            refresh();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedWindow == 0 && clickedVertexIndex >= 0 && clickedVertexIndex < vertexList.size()) {
                erase();
                if (selectedTool == 2) {
                    graphic.setColor(ACCENT_CYAN);
                    graphic.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6}, 0));
                    graphic.drawLine(vertexList.get(clickedVertexIndex).location.x, vertexList.get(clickedVertexIndex).location.y, e.getX(), e.getY());
                } else if (selectedTool == 3) {
                    vertexList.get(clickedVertexIndex).location.x = e.getX();
                    vertexList.get(clickedVertexIndex).location.y = e.getY();
                }
                refresh();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (selectedWindow == 0) {
                for (Edge d : edgeList) d.wasFocused = d.hasIntersection(e.getX(), e.getY());
                for (Vertex v : vertexList) v.wasFocused = v.hasIntersection(e.getX(), e.getY());
                refresh();
            }
        }

        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }

    // --- Restored Original Features & Algorithms ---

    private void handleOpenFile() {
        if (fileManager.jF.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Vector<Vector> data = fileManager.loadFile(fileManager.jF.getSelectedFile());
            if (data != null && data.size() >= 2) {
                vertexList = data.firstElement();
                edgeList = data.lastElement();
                erase();
                refresh();
            }
        }
    }

    private void handleSaveFile() {
        if (fileManager.jF.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            fileManager.saveFile(vertexList, edgeList, fileManager.jF.getSelectedFile());
        }
    }

    private void autoArrangeVertices() {
        if (vertexList.isEmpty()) return;
        double deg2rad = Math.PI / 180;
        double radius = canvas.getHeight() / 3.2;
        double centerX = (canvas.getWidth() - 220) / 2 + 220;
        double centerY = canvas.getHeight() / 2 + 20;
        int interval = 360 / vertexList.size();

        for (int i = 0; i < vertexList.size(); i++) {
            double degInRad = i * deg2rad * interval;
            vertexList.get(i).location.x = (int) (centerX + (Math.cos(degInRad) * radius));
            vertexList.get(i).location.y = (int) (centerY + (Math.sin(degInRad) * radius));
        }
    }

    private void highlightIsolatedNodes() {
        clearHighlights();
        Vector<Vertex> isolated = gP.findIsolatedNodes(vertexList);
        for (Vertex v : isolated) v.wasClicked = true;
    }

    private void highlightCutpoints() {
        clearHighlights();
        Vector<Vertex> cutpoints = gP.findCutpoints(vertexList);
        for (Vertex v : cutpoints) v.wasClicked = true;
    }

    private void highlightBridges() {
        clearHighlights();
        Vector<Edge> bridges = gP.findBridges(vertexList, edgeList);
        for (Edge e : bridges) e.wasClicked = true;
    }

    private void clearAll() {
        vertexList.clear();
        edgeList.clear();
        clickedVertexIndex = -1;
        erase();
        refresh();
    }

    private void clearHighlights() {
        for (Vertex v : vertexList) v.wasClicked = false;
        for (Edge d : edgeList) d.wasClicked = false;
    }

    private void buildProperties() {
        propertyLines.clear();
        if (vertexList.isEmpty()) return;
        gP.generateAdjacencyMatrix(vertexList, edgeList);
        Vector<Vertex> tempList = gP.vertexConnectivity(vertexList);
        gP.generateDistanceMatrix(vertexList);
        propertyLines.add("• Total Vertices: " + vertexList.size() + " | Edges: " + edgeList.size());
        propertyLines.add("• Connected Components: " + gP.countComponents(vertexList));
        propertyLines.add("• Graph Is Connected: " + (gP.isConnected(vertexList) ? "Yes" : "No"));
        propertyLines.add("• Vertex Connectivity: " + tempList.size() + " | Edge Connectivity: " + gP.calculateEdgeConnectivity(vertexList, edgeList));
        propertyLines.add("• Graph Density: " + String.format("%.4f", gP.calculateDensity(vertexList, edgeList)) + " (" + gP.classifyDensity(vertexList, edgeList) + ")");
    }

    private String nextVertexName() {
        int n = vertexList.size();
        while (true) {
            boolean taken = false;
            for (Vertex v : vertexList) {
                if (v.name.equals(String.valueOf(n))) { taken = true; n++; break; }
            }
            if (!taken) return String.valueOf(n);
        }
    }

    private void removeVertex(Vertex v) {
        for (Edge d : new Vector<Edge>(edgeList)) {
            if (d.vertex1 == v || d.vertex2 == v) removeEdge(d);
        }
        vertexList.remove(v);
    }

    private void removeEdge(Edge d) {
        edgeList.remove(d);
        d.vertex1.connectedVertices.remove(d.vertex2);
        d.vertex2.connectedVertices.remove(d.vertex1);
    }

    public void refresh() {
        for (Edge e : edgeList) e.draw(graphic);
        for (Vertex v : vertexList) v.draw(graphic);
        canvas.repaint();
    }

    public void setVisible(boolean visible) {
        if (graphic == null) {
            Dimension size = canvas.getSize();
            canvasImage = canvas.createImage(size.width, size.height);
            canvasImage2 = canvas.createImage(size.width, size.height);
            graphic = (Graphics2D) canvasImage.getGraphics();
            erase();
        }
        frame.setVisible(visible);
    }

    public void erase() {
        graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphic.setColor(BG_DARK);
        graphic.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Grid
        graphic.setColor(new Color(25, 30, 42));
        for (int i = 220; i < canvas.getWidth(); i += 30) graphic.drawLine(i, 0, i, canvas.getHeight());
        for (int j = 0; j < canvas.getHeight(); j += 30) graphic.drawLine(220, j, canvas.getWidth(), j);
    }

    private class CanvasPane extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (selectedWindow == 0) {
                g2.drawImage(canvasImage, 0, 0, null);
                drawFloatingToolbar(g2);
            } else if (selectedWindow == 1) {
                drawMatricesView(g2);
            } else if (selectedWindow == 2) {
                drawAnalyticsView(g2);
            }

            drawSidebar(g2);
        }

        private void drawSidebar(Graphics2D g2) {
            g2.setColor(SIDEBAR_BG);
            g2.fillRect(0, 0, 220, getHeight());

            g2.setColor(CARD_BORDER);
            g2.drawLine(220, 0, 220, getHeight());

            // Brand
            g2.setColor(ACCENT_CYAN);
            g2.fillRoundRect(20, 25, 10, 24, 6, 6);
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.drawString("GraphStudio", 38, 44);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(TEXT_MUTED);
            g2.drawString("NAVIGATION", 20, 80);

            drawNavPill(g2, "Studio Canvas", 0, 90);
            drawNavPill(g2, "Matrices & Proofs", 1, 135);
            drawNavPill(g2, "Degree Analytics", 2, 180);

            // File IO Buttons
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(TEXT_MUTED);
            g2.drawString("FILE MANAGEMENT", 20, 240);

            drawSidebarButton(g2, "📂 Open File", 250);
            drawSidebarButton(g2, "💾 Save File", 290);

            // Quick Stats Card
            g2.setColor(CARD_BG);
            g2.fillRoundRect(15, getHeight() - 110, 190, 90, 14, 14);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(15, getHeight() - 110, 190, 90, 14, 14);

            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString("ACTIVE GRAPH", 28, getHeight() - 88);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Nodes: " + vertexList.size(), 28, getHeight() - 62);
            g2.drawString("Edges: " + edgeList.size(), 28, getHeight() - 40);
        }

        private void drawSidebarButton(Graphics2D g2, String text, int y) {
            g2.setColor(CARD_BG);
            g2.fillRoundRect(15, y, 190, 30, 8, 8);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(15, y, 190, 30, 8, 8);
            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(text, 28, y + 20);
        }

        private void drawNavPill(Graphics2D g2, String title, int windowIdx, int y) {
            boolean active = selectedWindow == windowIdx;
            if (active) {
                g2.setColor(new Color(0, 210, 255, 30));
                g2.fillRoundRect(15, y, 190, 36, 10, 10);
                g2.setColor(ACCENT_CYAN);
                g2.drawRoundRect(15, y, 190, 36, 10, 10);
                g2.setColor(ACCENT_CYAN);
            } else {
                g2.setColor(TEXT_MUTED);
            }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(title, 32, y + 23);
        }

        private void drawFloatingToolbar(Graphics2D g2) {
            // Tools Panel
            g2.setColor(CARD_BG);
            g2.fillRoundRect(240, 18, 410, 42, 14, 14);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(240, 18, 410, 42, 14, 14);

            drawToolBtn(g2, "+ Node", 1, 245, 23, 70);
            drawToolBtn(g2, "+ Edge", 2, 325, 23, 70);
            drawToolBtn(g2, "Move", 3, 405, 23, 70);
            drawToolBtn(g2, "Delete", 4, 485, 23, 70);
            
            // Directed Mode Toggle
            if (directedMode) {
                g2.setColor(ACCENT_PINK);
                g2.fillRoundRect(565, 23, 80, 32, 10, 10);
                g2.setColor(BG_DARK);
            } else {
                g2.setColor(TEXT_MUTED);
            }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString("Directed", 580, 43);

            // Extras Toolbar
            g2.setColor(CARD_BG);
            g2.fillRoundRect(665, 18, 430, 42, 14, 14);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(665, 18, 430, 42, 14, 14);

            drawActionBtn(g2, "Arrange", 672, 23);
            drawActionBtn(g2, "Isolated", 762, 23);
            drawActionBtn(g2, "Cutpoints", 852, 23);
            drawActionBtn(g2, "Bridges", 932, 23);
            
            g2.setColor(ACCENT_PINK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString("Clear", 1025, 43);
        }

        private void drawToolBtn(Graphics2D g2, String label, int toolIdx, int x, int y, int w) {
            boolean active = selectedTool == toolIdx;
            if (active) {
                g2.setColor(ACCENT_CYAN);
                g2.fillRoundRect(x, y, w, 32, 10, 10);
                g2.setColor(BG_DARK);
            } else {
                g2.setColor(TEXT_MUTED);
            }
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(label, x + 12, y + 20);
        }

        private void drawActionBtn(Graphics2D g2, String label, int x, int y) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(label, x + 10, y + 20);
        }

        private void drawMatricesView(Graphics2D g2) {
            g2.setColor(BG_DARK);
            g2.fillRect(220, 0, getWidth() - 220, getHeight());

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("Graph Structural Analysis", 250, 45);

            canvasImage2.getGraphics().setColor(BG_DARK);
            canvasImage2.getGraphics().fillRect(0, 0, getWidth(), getHeight());

            gP.drawAdjacencyMatrix(canvasImage2.getGraphics(), vertexList, 250, 80);
            gP.drawDistanceMatrix(canvasImage2.getGraphics(), vertexList, 620, 80);
            g2.drawImage(canvasImage2, 0, 0, null);

            g2.setColor(CARD_BG);
            g2.fillRoundRect(250, 360, 750, 280, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(250, 360, 750, 280, 16, 16);

            g2.setColor(ACCENT_CYAN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Calculated Topological Properties", 275, 395);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            int y = 430;
            for (String line : propertyLines) {
                g2.drawString(line, 275, y);
                y += 30;
            }
        }

        private void drawAnalyticsView(Graphics2D g2) {
            g2.setColor(BG_DARK);
            g2.fillRect(220, 0, getWidth() - 220, getHeight());

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("Degree Distribution", 250, 45);

            Map<Integer, Integer> dist = gP.getDegreeDistribution(vertexList);
            double avgDeg = gP.getAverageDegree(vertexList);

            g2.setColor(CARD_BG);
            g2.fillRoundRect(250, 65, 260, 40, 12, 12);
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Average Node Degree: ", 265, 90);
            g2.setColor(ACCENT_CYAN);
            g2.drawString(String.format("%.2f", avgDeg), 425, 90);

            g2.setColor(CARD_BG);
            g2.fillRoundRect(250, 125, 430, 520, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(250, 125, 430, 520, 16, 16);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Degree Frequency Histogram", 275, 160);

            if (!dist.isEmpty()) {
                int maxFreq = Collections.max(dist.values());
                int startX = 295;
                int baseY = 580;

                for (Map.Entry<Integer, Integer> entry : dist.entrySet()) {
                    int degree = entry.getKey();
                    int count = entry.getValue();
                    int barHeight = (int) (((double) count / maxFreq) * 320);

                    g2.setColor(ACCENT_PINK);
                    g2.fillRoundRect(startX, baseY - barHeight, 42, barHeight, 8, 8);

                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString(String.valueOf(count), startX + 16, baseY - barHeight - 8);
                    g2.setColor(TEXT_MUTED);
                    g2.drawString("k=" + degree, startX + 10, baseY + 24);

                    startX += 70;
                }
            }

            g2.setColor(CARD_BG);
            g2.fillRoundRect(700, 125, 330, 520, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(700, 125, 330, 520, 16, 16);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Node Degrees", 725, 160);

            int nodeY = 200;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            for (Vertex v : vertexList) {
                if (nodeY > 600) break;
                int inDeg = gP.getInDegree(v, edgeList);
                int outDeg = gP.getOutDegree(v, edgeList);

                g2.setColor(ACCENT_CYAN);
                g2.fillOval(725, nodeY - 10, 8, 8);

                g2.setColor(TEXT_PRIMARY);
                g2.drawString("Node " + v.name, 742, nodeY);
                g2.setColor(TEXT_MUTED);
                g2.drawString("Deg: " + v.getDegree() + " (In: " + inDeg + ", Out: " + outDeg + ")", 820, nodeY);

                nodeY += 28;
            }
        }
    }
}