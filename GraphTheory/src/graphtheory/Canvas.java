package graphtheory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

public class Canvas {

    public JFrame frame;
    private CanvasPane canvas;
    private int selectedTool = 1; // 1: Add Node, 2: Add Edge, 3: Move, 4: Delete
    private int activeActionBtn = -1; // 1: Arrange, 2: Isolated, 3: Cutpoints, 4: Bridges, 5: Walks, 6: Trails, 7: Complete
    private int selectedWindow = 0; // 0: Studio, 1: Matrices, 2: Analytics
    private int clickedVertexIndex = -1;
    private Point dragCurrentPoint = null;
    private FileManager fileManager = new FileManager();

    private Vector<Vertex> vertexList = new Vector<Vertex>();
    private Vector<Edge> edgeList = new Vector<Edge>();
    private GraphProperties gP = new GraphProperties();
    private boolean directedMode = false;
    private Vector<String> propertyLines = new Vector<String>();

    // Interactive Analytics State
    private int selectedDegreeFilter = -1; // -1 means no bar selected / show all
    private Vector<DegreeBarRegion> barRegions = new Vector<DegreeBarRegion>();

    // Modern Palette
    public static final Color BG_DARK = new Color(15, 17, 23);
    public static final Color SIDEBAR_BG = new Color(22, 26, 36);
    public static final Color CARD_BG = new Color(30, 35, 48);
    public static final Color CARD_BORDER = new Color(45, 52, 70);
    public static final Color ACCENT_CYAN = new Color(0, 210, 255);
    public static final Color ACCENT_PINK = new Color(255, 51, 102); 
    public static final Color TEXT_PRIMARY = new Color(245, 247, 250);
    public static final Color TEXT_MUTED = new Color(140, 148, 168);

    // Dynamic Toolbar Button Click Regions
    private Vector<Rectangle> toolBounds = new Vector<Rectangle>();
    private Vector<Rectangle> actionBounds = new Vector<Rectangle>();
    private Rectangle directedBounds = new Rectangle();
    private Rectangle clearBounds = new Rectangle();

    private static class DegreeBarRegion {
        int degree;
        Rectangle bounds;
        DegreeBarRegion(int degree, Rectangle bounds) {
            this.degree = degree;
            this.bounds = bounds;
        }
    }

    public Canvas(String title, int width, int height, Color bgColour) {
        frame = new JFrame("GraphStudio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(950, 600));

        canvas = new CanvasPane();
        InputListener listener = new InputListener();
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);

        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });

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

            // 1. Sidebar Tab Navigation
            if (x < 220) {
                if (y >= 90 && y <= 125) selectedWindow = 0;
                else if (y >= 135 && y <= 170) { selectedWindow = 1; buildProperties(); }
                else if (y >= 180 && y <= 215) selectedWindow = 2;
                else if (y >= 250 && y <= 280) handleOpenFile();
                else if (y >= 290 && y <= 320) handleSaveFile();
                refresh();
                return;
            }

            // 2. Toolbar & Top Actions (Studio Canvas Window)
            if (selectedWindow == 0 && y >= 12 && y <= 54) {
                for (int i = 0; i < toolBounds.size(); i++) {
                    if (toolBounds.get(i).contains(x, y)) {
                        selectedTool = i + 1;
                        activeActionBtn = -1;
                        clearHighlights();
                        refresh();
                        return;
                    }
                }

                if (directedBounds.contains(x, y)) {
                    directedMode = !directedMode;
                    refresh();
                    return;
                }

                for (int i = 0; i < actionBounds.size(); i++) {
                    if (actionBounds.get(i).contains(x, y)) {
                        int btnIdx = i + 1;
                        if (btnIdx == 1) { activeActionBtn = 1; autoArrangeVertices(); }
                        else if (btnIdx == 2) { activeActionBtn = 2; highlightIsolatedNodes(); }
                        else if (btnIdx == 3) { activeActionBtn = 3; highlightCutpoints(); }
                        else if (btnIdx == 4) { activeActionBtn = 4; highlightBridges(); }
                        else if (btnIdx == 5) { activeActionBtn = 5; generateWalks(); }
                        else if (btnIdx == 6) { activeActionBtn = 6; generateTrails(); }
                        else if (btnIdx == 7) {
                            if (activeActionBtn == 7) activeActionBtn = -1;
                            else { clearHighlights(); activeActionBtn = 7; }
                        }
                        refresh();
                        return;
                    }
                }

                if (clearBounds.contains(x, y)) {
                    activeActionBtn = -1;
                    clearAll();
                    refresh();
                    return;
                }
            }

            // 3. Analytics Histogram Bar Clicks
            if (selectedWindow == 2) {
                for (DegreeBarRegion bar : barRegions) {
                    if (bar.bounds.contains(x, y)) {
                        if (selectedDegreeFilter == bar.degree) {
                            selectedDegreeFilter = -1; // Toggle off if clicked again
                        } else {
                            selectedDegreeFilter = bar.degree;
                        }
                        refresh();
                        return;
                    }
                }
            }

            // 4. Canvas Interaction
            if (selectedWindow == 0 && x > 230 && y > 65) {
                if (selectedTool == 1) {
                    Vertex v = new Vertex(nextVertexName(), x, y);
                    vertexList.add(v);
                }
            }
            refresh();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (selectedWindow == 0 && e.getX() > 230 && e.getY() > 65) {
                if (selectedTool == 4) { // Delete
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
            dragCurrentPoint = null;
            refresh();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedWindow == 0 && clickedVertexIndex >= 0 && clickedVertexIndex < vertexList.size()) {
                if (selectedTool == 2) {
                    dragCurrentPoint = e.getPoint();
                } else if (selectedTool == 3) {
                    int clampedX = Math.max(240, Math.min(canvas.getWidth() - 25, e.getX()));
                    int clampedY = Math.max(85, Math.min(canvas.getHeight() - 25, e.getY()));
                    vertexList.get(clickedVertexIndex).location.x = clampedX;
                    vertexList.get(clickedVertexIndex).location.y = clampedY;
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

    // --- Graph Operations ---

    private void handleOpenFile() {
        if (fileManager.jF.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Vector<Vector> data = fileManager.loadFile(fileManager.jF.getSelectedFile());
            if (data != null && data.size() >= 2) {
                vertexList = data.firstElement();
                edgeList = data.lastElement();
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
        double radius = Math.min(canvas.getWidth() - 260, canvas.getHeight() - 100) / 2.6;
        double centerX = (canvas.getWidth() - 220) / 2.0 + 220;
        double centerY = canvas.getHeight() / 2.0 + 30;
        int interval = 360 / vertexList.size();

        for (int i = 0; i < vertexList.size(); i++) {
            double degInRad = i * deg2rad * interval;
            vertexList.get(i).location.x = (int) (centerX + (Math.cos(degInRad) * radius));
            vertexList.get(i).location.y = (int) (centerY + (Math.sin(degInRad) * radius));
        }
        refresh();
    }

    private void highlightIsolatedNodes() {
        clearHighlights();
        Vector<Vertex> isolated = gP.findIsolatedNodes(vertexList);
        for (Vertex v : isolated) v.wasClicked = true;
        refresh();
    }

    private void highlightCutpoints() {
        clearHighlights();
        Vector<Vertex> cutpoints = gP.findCutpoints(vertexList);
        for (Vertex v : cutpoints) v.wasClicked = true;
        refresh();
    }

    private void highlightBridges() {
        clearHighlights();
        Vector<Edge> bridges = gP.findBridges(vertexList, edgeList);
        for (Edge e : bridges) e.wasClicked = true;
        refresh();
    }

    private void generateWalks() {
        if (vertexList.isEmpty()) return;
        
        PrintStream origOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        gP.generateWalks(vertexList, 4);

        System.setOut(origOut);
        showPopupModal("Generated Graph Walks (Length ≤ 4)", baos.toString());
    }

    private void generateTrails() {
        if (vertexList.isEmpty()) return;

        PrintStream origOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        gP.generateTrails(vertexList, 4);

        System.setOut(origOut);
        showPopupModal("Generated Graph Trails (Length ≤ 4)", baos.toString());
    }

    private void showPopupModal(String title, String content) {
        JDialog modal = new JDialog(frame, title, true);
        modal.setSize(520, 420);
        modal.setLocationRelativeTo(frame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(ACCENT_CYAN);
        mainPanel.add(header, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(content.isEmpty() ? "No sequences found for this graph." : content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setBackground(CARD_BG);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(CARD_BG);
        closeBtn.setForeground(TEXT_PRIMARY);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeBtn.addActionListener(e -> modal.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        modal.setContentPane(mainPanel);
        modal.setVisible(true);
    }

    private void clearAll() {
        vertexList.clear();
        edgeList.clear();
        clickedVertexIndex = -1;
        activeActionBtn = -1;
        selectedDegreeFilter = -1;
        refresh();
    }

    private void clearHighlights() {
        for (Vertex v : vertexList) v.wasClicked = false;
        for (Edge d : edgeList) d.wasClicked = false;
    }

    private void buildProperties() {
        propertyLines.clear();
        if (vertexList.isEmpty()) return;

        int[][] matrix = gP.generateAdjacencyMatrix(vertexList, edgeList);
        Vector<Vertex> tempList = gP.vertexConnectivity(vertexList);
        for (Vertex v : tempList) {
            vertexList.get(vertexList.indexOf(v)).wasClicked = true;
        }

        gP.generateDistanceMatrix(vertexList);

        propertyLines.add("• Order |V| = " + vertexList.size() + "    Size |E| = " + edgeList.size());
        propertyLines.add("• Components k(G) = " + gP.countComponents(vertexList) + "    Connected: " + (gP.isConnected(vertexList) ? "Yes" : "No"));
        propertyLines.add("• Connectivity K(G) = " + tempList.size() + "    Edge Connectivity = " + gP.calculateEdgeConnectivity(vertexList, edgeList));
        propertyLines.add("• Density = " + String.format("%.4f", gP.calculateDensity(vertexList, edgeList)) + " (" + gP.classifyDensity(vertexList, edgeList) + ")");
        propertyLines.add("• Graph Completeness: " + (gP.isComplete(vertexList, edgeList) ? "Complete (K_" + vertexList.size() + ")" : "Incomplete (" + gP.getMissingEdges(vertexList, edgeList).size() + " edges missing)"));

        StringBuilder names = new StringBuilder();
        Vector<Vertex> isolatedNodes = gP.findIsolatedNodes(vertexList);
        for (Vertex v : isolatedNodes) names.append(names.length() > 0 ? ", " : "").append(v.name);
        addWrapped("• Isolated Nodes (" + isolatedNodes.size() + "): " + (isolatedNodes.isEmpty() ? "None" : names));

        names = new StringBuilder();
        Vector<Vertex> cutpoints = gP.findCutpoints(vertexList);
        for (Vertex v : cutpoints) names.append(names.length() > 0 ? ", " : "").append(v.name);
        addWrapped("• Cutpoints (" + cutpoints.size() + "): " + (cutpoints.isEmpty() ? "None" : names));

        names = new StringBuilder();
        Vector<Edge> bridges = gP.findBridges(vertexList, edgeList);
        for (Edge d : bridges) names.append(names.length() > 0 ? ", " : "").append(d.vertex1.name).append("-").append(d.vertex2.name);
        addWrapped("• Bridges (" + bridges.size() + "): " + (bridges.isEmpty() ? "None" : names));

        names = new StringBuilder();
        for (Vertex v : vertexList) {
            names.append(names.length() > 0 ? ", " : "").append(v.name).append(":").append(v.getDegree())
                    .append("(").append(gP.getInDegree(v, edgeList)).append("/").append(gP.getOutDegree(v, edgeList)).append(")");
        }
        addWrapped("• Degrees (Deg(In/Out)): " + names);
    }

    private void addWrapped(String text) {
        int maxChars = 78;
        while (text.length() > maxChars) {
            int cut = text.lastIndexOf(", ", maxChars);
            if (cut <= 0) cut = maxChars;
            else cut += 1;
            propertyLines.add(text.substring(0, cut));
            text = "    " + text.substring(cut).trim();
        }
        propertyLines.add(text);
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
        canvas.repaint();
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    private class CanvasPane extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (selectedWindow == 0) {
                drawCanvasGrid(g2);
                if (activeActionBtn == 7) {
                    gP.drawMissingEdges(g2, vertexList, edgeList);
                }
                for (Edge e : edgeList) e.draw(g2);
                for (Vertex v : vertexList) v.draw(g2);

                if (selectedTool == 2 && clickedVertexIndex >= 0 && clickedVertexIndex < vertexList.size() && dragCurrentPoint != null) {
                    g2.setColor(ACCENT_CYAN);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6}, 0));
                    g2.drawLine(vertexList.get(clickedVertexIndex).location.x, vertexList.get(clickedVertexIndex).location.y, dragCurrentPoint.x, dragCurrentPoint.y);
                }

                drawFloatingToolbar(g2);
            } else if (selectedWindow == 1) {
                drawMatricesView(g2);
            } else if (selectedWindow == 2) {
                drawAnalyticsView(g2);
            }

            drawSidebar(g2);
        }

        private void drawCanvasGrid(Graphics2D g2) {
            g2.setColor(BG_DARK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(25, 30, 42));
            for (int i = 220; i < getWidth(); i += 30) g2.drawLine(i, 0, i, getHeight());
            for (int j = 0; j < getHeight(); j += 30) g2.drawLine(220, j, getWidth(), j);
        }

        private void drawSidebar(Graphics2D g2) {
            g2.setColor(SIDEBAR_BG);
            g2.fillRect(0, 0, 220, getHeight());

            g2.setColor(CARD_BORDER);
            g2.drawLine(220, 0, 220, getHeight());

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

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(TEXT_MUTED);
            g2.drawString("FILE MANAGEMENT", 20, 240);

            drawSidebarButton(g2, "📂 Open File", 250);
            drawSidebarButton(g2, "💾 Save File", 290);

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
            int startX = 235;
            int totalWidth = getWidth() - startX - 15;
            int toolbarY = 12;
            int toolbarHeight = 42;

            g2.setColor(CARD_BG);
            g2.fillRoundRect(startX - 5, toolbarY, totalWidth + 10, toolbarHeight, 14, 14);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(startX - 5, toolbarY, totalWidth + 10, toolbarHeight, 14, 14);

            int totalButtons = 13;
            int gap = 6;
            int btnWidth = (totalWidth - (gap * (totalButtons + 1))) / totalButtons;
            btnWidth = Math.max(50, btnWidth);

            toolBounds.clear();
            actionBounds.clear();

            int currentX = startX + gap;
            int btnY = toolbarY + 5;
            int btnH = 32;

            // 1. Tool Buttons
            String[] toolLabels = {"+ Node", "+ Edge", "Move", "Delete"};
            for (int i = 0; i < toolLabels.length; i++) {
                Rectangle r = new Rectangle(currentX, btnY, btnWidth, btnH);
                toolBounds.add(r);
                drawToolBtn(g2, toolLabels[i], i + 1, r);
                currentX += btnWidth + gap;
            }

            // 2. Directed Mode Button
            directedBounds = new Rectangle(currentX, btnY, btnWidth, btnH);
            if (directedMode) {
                g2.setColor(ACCENT_PINK);
                g2.fillRoundRect(directedBounds.x, directedBounds.y, directedBounds.width, directedBounds.height, 10, 10);
                g2.setColor(TEXT_PRIMARY);
            } else {
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(directedBounds.x, directedBounds.y, directedBounds.width, directedBounds.height, 10, 10);
                g2.setColor(TEXT_MUTED);
            }
            drawCenteredString(g2, "Directed", directedBounds);
            currentX += btnWidth + gap;

            // 3. Action Buttons
            String[] actionLabels = {"Arrange", "Isolated", "Cutpoints", "Bridges", "Walks", "Trails", "Complete"};
            for (int i = 0; i < actionLabels.length; i++) {
                Rectangle r = new Rectangle(currentX, btnY, btnWidth, btnH);
                actionBounds.add(r);
                drawRedActionBtn(g2, actionLabels[i], i + 1, r);
                currentX += btnWidth + gap;
            }

            // 4. Clear Button
            clearBounds = new Rectangle(currentX, btnY, btnWidth, btnH);
            g2.setColor(ACCENT_PINK);
            drawCenteredString(g2, "Clear", clearBounds);
        }

        private void drawToolBtn(Graphics2D g2, String label, int toolIdx, Rectangle r) {
            boolean active = selectedTool == toolIdx;
            if (active) {
                g2.setColor(ACCENT_CYAN);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                g2.setColor(BG_DARK);
            } else {
                g2.setColor(TEXT_MUTED);
            }
            drawCenteredString(g2, label, r);
        }

        private void drawRedActionBtn(Graphics2D g2, String label, int btnIdx, Rectangle r) {
            boolean active = activeActionBtn == btnIdx;
            if (active) {
                g2.setColor(btnIdx == 7 ? new Color(46, 204, 113) : ACCENT_PINK);
                g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                g2.setColor(TEXT_PRIMARY);
            } else {
                g2.setColor(TEXT_MUTED);
            }
            drawCenteredString(g2, label, r);
        }

        private void drawCenteredString(Graphics2D g2, String text, Rectangle rect) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics metrics = g2.getFontMetrics();
            int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
            int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
            g2.drawString(text, x, y);
        }

        private void drawMatricesView(Graphics2D g2) {
            g2.setColor(BG_DARK);
            g2.fillRect(220, 0, getWidth() - 220, getHeight());

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("Graph Structural Analysis", 250, 45);

            int totalAvailableWidth = getWidth() - 280;
            int gap = 20;

            int cardWidth = Math.max(220, (totalAvailableWidth - (gap * 2)) / 3);
            int topCardHeight = 230;

            int x1 = 250;
            int x2 = x1 + cardWidth + gap;
            int x3 = x2 + cardWidth + gap;
            int topY = 70;

            gP.drawAdjacencyMatrix(g2, vertexList, x1, topY, cardWidth, topCardHeight);
            gP.drawDistanceMatrix(g2, vertexList, x2, topY, cardWidth, topCardHeight);

            drawGraphPreviewCard(g2, x3, topY, cardWidth, topCardHeight);

            int bottomCardWidth = totalAvailableWidth;
            int bottomCardHeight = Math.max(250, getHeight() - 330);
            int bottomY = topY + topCardHeight + gap;

            g2.setColor(CARD_BG);
            g2.fillRoundRect(250, bottomY, bottomCardWidth, bottomCardHeight, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(250, bottomY, bottomCardWidth, bottomCardHeight, 16, 16);

            g2.setColor(ACCENT_CYAN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Calculated Topological Properties", 275, bottomY + 30);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            int y = bottomY + 60;
            for (String line : propertyLines) {
                if (y > getHeight() - 30) break;
                g2.drawString(line, 275, y);
                y += 24;
            }
        }

        private void drawGraphPreviewCard(Graphics2D g2, int x, int y, int width, int height) {
            g2.setColor(CARD_BG);
            g2.fillRoundRect(x, y, width, height, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(x, y, width, height, 16, 16);

            g2.setColor(ACCENT_CYAN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Graph Preview", x + 20, y + 30);

            g2.setColor(new Color(0, 210, 255, 30));
            g2.fillRoundRect(x + width - 90, y + 14, 70, 22, 6, 6);
            g2.setColor(ACCENT_CYAN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString("READ-ONLY", x + width - 83, y + 29);

            if (vertexList.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g2.drawString("No graph rendered", x + 20, y + 65);
                return;
            }

            Shape origClip = g2.getClip();
            int previewMargin = 15;
            int px = x + previewMargin;
            int py = y + 42;
            int pw = width - (previewMargin * 2);
            int ph = height - 52;
            g2.clipRect(px, py, pw, ph);

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

            for (Vertex v : vertexList) {
                minX = Math.min(minX, v.location.x);
                minY = Math.min(minY, v.location.y);
                maxX = Math.max(maxX, v.location.x);
                maxY = Math.max(maxY, v.location.y);
            }

            int graphW = Math.max(1, maxX - minX);
            int graphH = Math.max(1, maxY - minY);

            double scaleX = (double) (pw - 40) / Math.max(graphW, 100);
            double scaleY = (double) (ph - 40) / Math.max(graphH, 100);
            double scale = Math.min(1.0, Math.min(scaleX, scaleY));

            int offsetX = px + (pw - (int) (graphW * scale)) / 2;
            int offsetY = py + (ph - (int) (graphH * scale)) / 2;

            g2.setColor(new Color(100, 115, 145));
            g2.setStroke(new BasicStroke(1.5f));
            for (Edge e : edgeList) {
                int x1 = (int) (offsetX + (e.vertex1.location.x - minX) * scale);
                int y1 = (int) (offsetY + (e.vertex1.location.y - minY) * scale);
                int x2 = (int) (offsetX + (e.vertex2.location.x - minX) * scale);
                int y2 = (int) (offsetY + (e.vertex2.location.y - minY) * scale);
                g2.drawLine(x1, y1, x2, y2);
            }

            int nodeRadius = Math.max(8, (int) (14 * Math.max(0.6, scale)));
            Font nodeFont = new Font("Segoe UI", Font.BOLD, Math.max(9, (int) (11 * scale)));
            g2.setFont(nodeFont);
            FontMetrics fm = g2.getFontMetrics();

            for (Vertex v : vertexList) {
                int vx = (int) (offsetX + (v.location.x - minX) * scale);
                int vy = (int) (offsetY + (v.location.y - minY) * scale);

                g2.setColor(ACCENT_CYAN);
                g2.fillOval(vx - nodeRadius, vy - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                g2.setColor(BG_DARK);
                g2.fillOval(vx - nodeRadius + 2, vy - nodeRadius + 2, (nodeRadius - 2) * 2, (nodeRadius - 2) * 2);

                g2.setColor(TEXT_PRIMARY);
                int tx = vx - fm.stringWidth(v.name) / 2;
                int ty = vy + fm.getAscent() / 2 - 2;
                g2.drawString(v.name, tx, ty);
            }

            g2.setClip(origClip);
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

            int panelHeight = Math.max(520, getHeight() - 150);
            int totalWidth = getWidth() - 280;
            int gap = 20;

            int histogramWidth = (int) (totalWidth * 0.52);
            int previewWidth = totalWidth - histogramWidth - gap;

            // 1. Histogram Card
            g2.setColor(CARD_BG);
            g2.fillRoundRect(250, 125, histogramWidth, panelHeight, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(250, 125, histogramWidth, panelHeight, 16, 16);

            g2.setColor(TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Degree Frequency Histogram", 275, 160);

            g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g2.setColor(TEXT_MUTED);
            g2.drawString("Click a bar to filter & highlight matching nodes", 275, 180);

            barRegions.clear();

            if (!dist.isEmpty()) {
                int maxFreq = Collections.max(dist.values());
                int startX = 295;
                int baseY = 125 + panelHeight - 65;
                int maxBarH = panelHeight - 240;

                for (Map.Entry<Integer, Integer> entry : dist.entrySet()) {
                    int degree = entry.getKey();
                    int count = entry.getValue();
                    int barHeight = (int) (((double) count / maxFreq) * maxBarH);
                    barHeight = Math.max(8, barHeight);

                    Rectangle barRect = new Rectangle(startX, baseY - barHeight, 46, barHeight);
                    barRegions.add(new DegreeBarRegion(degree, barRect));

                    boolean isSelected = (selectedDegreeFilter == degree);

                    if (isSelected) {
                        g2.setColor(ACCENT_CYAN);
                        g2.fillRoundRect(startX - 3, baseY - barHeight - 3, 52, barHeight + 6, 10, 10);
                        g2.setColor(ACCENT_PINK);
                    } else {
                        g2.setColor(selectedDegreeFilter == -1 ? ACCENT_PINK : new Color(255, 51, 102, 100));
                    }
                    g2.fillRoundRect(startX, baseY - barHeight, 46, barHeight, 8, 8);

                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString(String.valueOf(count), startX + 18, baseY - barHeight - 8);

                    if (isSelected) g2.setColor(ACCENT_CYAN);
                    else g2.setColor(TEXT_MUTED);
                    g2.drawString("k=" + degree, startX + 12, baseY + 24);

                    startX += 72;
                }
            }

            // 2. Interactive Degree Preview Card (Right Side)
            int previewX = 250 + histogramWidth + gap;
            drawInteractiveDegreePreview(g2, previewX, 125, previewWidth, panelHeight);
        }

        private void drawInteractiveDegreePreview(Graphics2D g2, int x, int y, int width, int height) {
            g2.setColor(CARD_BG);
            g2.fillRoundRect(x, y, width, height, 16, 16);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(x, y, width, height, 16, 16);

            g2.setColor(ACCENT_CYAN);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.drawString("Degree Highlight Preview", x + 20, y + 30);

            // Filter Status Badge
            String badgeText = (selectedDegreeFilter == -1) ? "SHOWING ALL" : ("FILTER: k = " + selectedDegreeFilter);
            g2.setColor(selectedDegreeFilter == -1 ? new Color(0, 210, 255, 30) : new Color(255, 51, 102, 40));
            g2.fillRoundRect(x + width - 130, y + 14, 110, 22, 6, 6);
            g2.setColor(selectedDegreeFilter == -1 ? ACCENT_CYAN : ACCENT_PINK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(badgeText, x + width - 120, y + 29);

            if (vertexList.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g2.drawString("No graph available", x + 20, y + 65);
                return;
            }

            Shape origClip = g2.getClip();
            int margin = 15;
            int px = x + margin;
            int py = y + 45;
            int pw = width - (margin * 2);
            int ph = height - 55;
            g2.clipRect(px, py, pw, ph);

            // Compute Graph Bounds
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

            for (Vertex v : vertexList) {
                minX = Math.min(minX, v.location.x);
                minY = Math.min(minY, v.location.y);
                maxX = Math.max(maxX, v.location.x);
                maxY = Math.max(maxY, v.location.y);
            }

            int graphW = Math.max(1, maxX - minX);
            int graphH = Math.max(1, maxY - minY);

            double scaleX = (double) (pw - 50) / Math.max(graphW, 100);
            double scaleY = (double) (ph - 50) / Math.max(graphH, 100);
            double scale = Math.min(1.0, Math.min(scaleX, scaleY));

            int offsetX = px + (pw - (int) (graphW * scale)) / 2;
            int offsetY = py + (ph - (int) (graphH * scale)) / 2;

            // Draw Edges (Dimmed if filtering active)
            g2.setColor(selectedDegreeFilter == -1 ? new Color(90, 105, 135) : new Color(50, 60, 80));
            g2.setStroke(new BasicStroke(1.5f));
            for (Edge e : edgeList) {
                int x1 = (int) (offsetX + (e.vertex1.location.x - minX) * scale);
                int y1 = (int) (offsetY + (e.vertex1.location.y - minY) * scale);
                int x2 = (int) (offsetX + (e.vertex2.location.x - minX) * scale);
                int y2 = (int) (offsetY + (e.vertex2.location.y - minY) * scale);
                g2.drawLine(x1, y1, x2, y2);
            }

            // Draw Scaled Vertices with Highlight for Selected Degree
            int nodeRadius = Math.max(10, (int) (16 * Math.max(0.6, scale)));
            Font nodeFont = new Font("Segoe UI", Font.BOLD, Math.max(10, (int) (12 * scale)));
            g2.setFont(nodeFont);
            FontMetrics fm = g2.getFontMetrics();

            for (Vertex v : vertexList) {
                int vx = (int) (offsetX + (v.location.x - minX) * scale);
                int vy = (int) (offsetY + (v.location.y - minY) * scale);
                boolean matchesDegree = (selectedDegreeFilter == -1) || (v.getDegree() == selectedDegreeFilter);

                if (matchesDegree) {
                    // Outer Glowing Ring for Highlighted Nodes
                    if (selectedDegreeFilter != -1) {
                        g2.setColor(new Color(255, 51, 102, 100));
                        g2.fillOval(vx - nodeRadius - 6, vy - nodeRadius - 6, (nodeRadius + 6) * 2, (nodeRadius + 6) * 2);
                    }

                    g2.setColor(selectedDegreeFilter != -1 ? ACCENT_PINK : ACCENT_CYAN);
                    g2.fillOval(vx - nodeRadius, vy - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                    g2.setColor(BG_DARK);
                    g2.fillOval(vx - nodeRadius + 3, vy - nodeRadius + 3, (nodeRadius - 3) * 2, (nodeRadius - 3) * 2);

                    g2.setColor(TEXT_PRIMARY);
                } else {
                    // Muted/Dimmed Non-Matching Nodes
                    g2.setColor(new Color(50, 60, 80));
                    g2.fillOval(vx - nodeRadius, vy - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                    g2.setColor(BG_DARK);
                    g2.fillOval(vx - nodeRadius + 2, vy - nodeRadius + 2, (nodeRadius - 2) * 2, (nodeRadius - 2) * 2);

                    g2.setColor(TEXT_MUTED);
                }

                int tx = vx - fm.stringWidth(v.name) / 2;
                int ty = vy + fm.getAscent() / 2 - 2;
                g2.drawString(v.name, tx, ty);
            }

            g2.setClip(origClip);
        }
    }
}