package graphtheory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

public class GraphProperties {

    public double[][] adjacencyMatrix;
    public double[][] distanceMatrix;
    public Vector<VertexPair> vpList;

    // --- Graph Completeness & Missing Edges ---

    public boolean isComplete(Vector<Vertex> vList, Vector<Edge> eList) {
        if (vList.size() <= 1) return true;
        return getMissingEdges(vList, eList).isEmpty();
    }

    public Vector<VertexPair> getMissingEdges(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<VertexPair> missing = new Vector<VertexPair>();
        int n = vList.size();
        if (n < 2) return missing;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Vertex u = vList.get(i);
                Vertex v = vList.get(j);

                boolean edgeExists = false;
                for (Edge e : eList) {
                    if ((e.vertex1 == u && e.vertex2 == v) || (e.vertex1 == v && e.vertex2 == u)) {
                        edgeExists = true;
                        break;
                    }
                }

                if (!edgeExists) {
                    missing.add(new VertexPair(u, v));
                }
            }
        }
        return missing;
    }

    public void drawMissingEdges(Graphics g, Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<VertexPair> missing = getMissingEdges(vList, eList);
        if (missing.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(46, 204, 113));

        Stroke originalStroke = g2.getStroke();
        float[] dashPattern = {8.0f, 6.0f};
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));

        for (VertexPair pair : missing) {
            g2.drawLine(pair.vertex1.location.x, pair.vertex1.location.y,
                        pair.vertex2.location.x, pair.vertex2.location.y);
        }

        g2.setStroke(originalStroke);
    }

    // --- Cyclic vs Acyclic Detection ---

    public String checkCyclic(Vector<Vertex> vList, Vector<Edge> eList) {
        if (vList.isEmpty()) return "Empty Graph";

        boolean isDirected = false;
        for (Edge e : eList) {
            if (e.directed) {
                isDirected = true;
                break;
            }
        }

        if (isDirected) {
            return isDirectedCyclic(vList, eList) ? "Cyclic (Directed Graph with Cycles)" : "Acyclic (DAG)";
        } else {
            return isUndirectedCyclic(vList, eList) ? "Cyclic" : "Acyclic (Forest/Tree)";
        }
    }

    private boolean isUndirectedCyclic(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<Vertex> visited = new Vector<Vertex>();
        for (Vertex v : vList) {
            if (!visited.contains(v)) {
                if (dfsUndirectedCycle(v, null, visited, vList, eList)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsUndirectedCycle(Vertex current, Vertex parent, Vector<Vertex> visited, Vector<Vertex> vList, Vector<Edge> eList) {
        visited.add(current);
        for (Vertex neighbor : getNeighbors(current, eList, false)) {
            if (!visited.contains(neighbor)) {
                if (dfsUndirectedCycle(neighbor, current, visited, vList, eList)) {
                    return true;
                }
            } else if (neighbor != parent) {
                return true;
            }
        }
        return false;
    }

    private boolean isDirectedCyclic(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<Vertex> visited = new Vector<Vertex>();
        Vector<Vertex> recStack = new Vector<Vertex>();

        for (Vertex v : vList) {
            if (!visited.contains(v)) {
                if (dfsDirectedCycle(v, visited, recStack, eList)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsDirectedCycle(Vertex current, Vector<Vertex> visited, Vector<Vertex> recStack, Vector<Edge> eList) {
        visited.add(current);
        recStack.add(current);

        for (Vertex neighbor : getNeighbors(current, eList, true)) {
            if (!visited.contains(neighbor)) {
                if (dfsDirectedCycle(neighbor, visited, recStack, eList)) {
                    return true;
                }
            } else if (recStack.contains(neighbor)) {
                return true;
            }
        }

        recStack.remove(current);
        return false;
    }

    private Vector<Vertex> getNeighbors(Vertex v, Vector<Edge> eList, boolean directedOnly) {
        Vector<Vertex> neighbors = new Vector<Vertex>();
        for (Edge e : eList) {
            if (e.vertex1 == v) {
                neighbors.add(e.vertex2);
            } else if (!directedOnly && e.vertex2 == v) {
                neighbors.add(e.vertex1);
            }
        }
        return neighbors;
    }

    // --- Weighted Adjacency & Shortest Path (Floyd-Warshall) ---

    public double[][] generateAdjacencyMatrix(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        adjacencyMatrix = new double[n][n];
        for (int i = 0; i < n; i++) Arrays.fill(adjacencyMatrix[i], 0.0);

        for (Edge e : eList) {
            int idx1 = vList.indexOf(e.vertex1);
            int idx2 = vList.indexOf(e.vertex2);
            if (idx1 != -1 && idx2 != -1) {
                adjacencyMatrix[idx1][idx2] = e.weight;
                if (!e.directed) {
                    adjacencyMatrix[idx2][idx1] = e.weight;
                }
            }
        }
        return adjacencyMatrix;
    }

    public double[][] generateDistanceMatrix(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        distanceMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) distanceMatrix[i][j] = 0;
                else distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        for (Edge e : eList) {
            int u = vList.indexOf(e.vertex1);
            int v = vList.indexOf(e.vertex2);
            if (u != -1 && v != -1) {
                distanceMatrix[u][v] = Math.min(distanceMatrix[u][v], e.weight);
                if (!e.directed) {
                    distanceMatrix[v][u] = Math.min(distanceMatrix[v][u], e.weight);
                }
            }
        }

        // Floyd-Warshall Algorithm for Shortest Paths
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][k] + distanceMatrix[k][j] < distanceMatrix[i][j]) {
                        distanceMatrix[i][j] = distanceMatrix[i][k] + distanceMatrix[k][j];
                    }
                }
            }
        }
        return distanceMatrix;
    }

    // --- Eulerian & Hamiltonian Checks ---

    public String checkEulerian(Vector<Vertex> vList, Vector<Edge> eList) {
        if (vList.isEmpty()) return "Empty Graph";
        if (!isConnected(vList)) {
            int nonIsolated = 0;
            for (Vertex v : vList) {
                if (v.getDegree() > 0) nonIsolated++;
            }
            if (nonIsolated > 1) return "Non-Eulerian (Disconnected)";
        }

        boolean isDirected = false;
        for (Edge e : eList) {
            if (e.directed) {
                isDirected = true;
                break;
            }
        }

        if (isDirected) {
            int startNodes = 0, endNodes = 0;
            for (Vertex v : vList) {
                int in = getInDegree(v, eList);
                int out = getOutDegree(v, eList);
                if (out - in == 1) startNodes++;
                else if (in - out == 1) endNodes++;
                else if (in != out) return "Non-Eulerian";
            }
            if (startNodes == 0 && endNodes == 0) return "Eulerian Circuit";
            if (startNodes == 1 && endNodes == 1) return "Semi-Eulerian (Eulerian Path)";
            return "Non-Eulerian";
        } else {
            int oddDegreeCount = 0;
            for (Vertex v : vList) {
                if (v.getDegree() % 2 != 0) oddDegreeCount++;
            }
            if (oddDegreeCount == 0) return "Eulerian Circuit";
            if (oddDegreeCount == 2) return "Semi-Eulerian (Eulerian Path)";
            return "Non-Eulerian (" + oddDegreeCount + " odd degree vertices)";
        }
    }

    public String checkHamiltonian(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        if (n == 0) return "Empty Graph";
        if (n == 1) return "Hamiltonian Cycle & Path";
        if (n == 2) {
            return (eList.size() >= 1) ? "Semi-Hamiltonian (Hamiltonian Path)" : "Non-Hamiltonian";
        }

        double[][] adj = generateAdjacencyMatrix(vList, eList);

        for (int i = 0; i < n; i++) {
            boolean[] visited = new boolean[n];
            visited[i] = true;
            if (hamiltonianCycleUtil(adj, visited, i, i, 1, n)) {
                return "Hamiltonian Cycle";
            }
        }

        for (int i = 0; i < n; i++) {
            boolean[] visited = new boolean[n];
            visited[i] = true;
            if (hamiltonianPathUtil(adj, visited, i, 1, n)) {
                return "Semi-Hamiltonian (Hamiltonian Path)";
            }
        }

        return "Non-Hamiltonian";
    }

    private boolean hamiltonianCycleUtil(double[][] adj, boolean[] visited, int current, int start, int count, int n) {
        if (count == n) {
            return adj[current][start] > 0;
        }
        for (int v = 0; v < n; v++) {
            if (adj[current][v] > 0 && !visited[v]) {
                visited[v] = true;
                if (hamiltonianCycleUtil(adj, visited, v, start, count + 1, n)) return true;
                visited[v] = false;
            }
        }
        return false;
    }

    private boolean hamiltonianPathUtil(double[][] adj, boolean[] visited, int current, int count, int n) {
        if (count == n) return true;
        for (int v = 0; v < n; v++) {
            if (adj[current][v] > 0 && !visited[v]) {
                visited[v] = true;
                if (hamiltonianPathUtil(adj, visited, v, count + 1, n)) return true;
                visited[v] = false;
            }
        }
        return false;
    }

    public Map<Integer, Integer> getDegreeDistribution(Vector<Vertex> vList) {
        Map<Integer, Integer> dist = new HashMap<Integer, Integer>();
        for (Vertex v : vList) {
            int deg = v.getDegree();
            dist.put(deg, dist.getOrDefault(deg, 0) + 1);
        }
        return dist;
    }

    public double getAverageDegree(Vector<Vertex> vList) {
        if (vList.isEmpty()) return 0.0;
        double totalDegree = 0;
        for (Vertex v : vList) {
            totalDegree += v.getDegree();
        }
        return totalDegree / vList.size();
    }

    public void drawAdjacencyMatrix(Graphics g, Vector<Vertex> vList, int x, int y, int cardWidth, int cardHeight) {
        drawMatrixCard(g, vList, x, y, cardWidth, cardHeight, "Adjacency Matrix", adjacencyMatrix);
    }

    public void drawDistanceMatrix(Graphics g, Vector<Vertex> vList, int x, int y, int cardWidth, int cardHeight) {
        drawMatrixCard(g, vList, x, y, cardWidth, cardHeight, "Shortest Path Matrix", distanceMatrix);
    }

    private void drawMatrixCard(Graphics g, Vector<Vertex> vList, int x, int y, int width, int height, String title, double[][] matrix) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Canvas.CARD_BG);
        g2.fillRoundRect(x, y, width, height, 16, 16);
        g2.setColor(Canvas.CARD_BORDER);
        g2.drawRoundRect(x, y, width, height, 16, 16);

        g2.setColor(Canvas.ACCENT_CYAN);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.drawString(title, x + 20, y + 30);

        if (vList.isEmpty() || matrix == null) {
            g2.setColor(Canvas.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.drawString("No graph data available", x + 20, y + 65);
            return;
        }

        int n = vList.size();
        int availableW = width - 60;
        int availableH = height - 70;
        int cellSize = Math.min(32, Math.min(availableW / (n + 1), availableH / (n + 1)));
        cellSize = Math.max(22, cellSize);

        int startX = x + 25;
        int startY = y + 55;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(Canvas.ACCENT_PINK);
        FontMetrics fm = g2.getFontMetrics();

        for (int j = 0; j < n; j++) {
            String colLabel = vList.get(j).name;
            int cx = startX + (j + 1) * cellSize + (cellSize - fm.stringWidth(colLabel)) / 2;
            int cy = startY + (cellSize + fm.getAscent()) / 2 - 2;
            g2.drawString(colLabel, cx, cy);
        }

        for (int i = 0; i < n; i++) {
            int rowY = startY + (i + 1) * cellSize;

            g2.setColor(Canvas.ACCENT_PINK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String rowLabel = vList.get(i).name;
            int rx = startX + (cellSize - fm.stringWidth(rowLabel)) / 2;
            int ry = rowY + (cellSize + fm.getAscent()) / 2 - 2;
            g2.drawString(rowLabel, rx, ry);

            g2.setFont(new Font("Consolas", Font.PLAIN, 12));
            fm = g2.getFontMetrics();

            for (int j = 0; j < n; j++) {
                if (i < matrix.length && j < matrix[i].length) {
                    double val = matrix[i][j];
                    String valStr;
                    if (Double.isInfinite(val)) valStr = "∞";
                    else if (val == (long) val) valStr = String.format("%d", (long) val);
                    else valStr = String.format("%.1f", val);

                    if (val > 0 && !Double.isInfinite(val)) g2.setColor(Canvas.TEXT_PRIMARY);
                    else g2.setColor(Canvas.TEXT_MUTED);

                    int vx = startX + (j + 1) * cellSize + (cellSize - fm.stringWidth(valStr)) / 2;
                    int vy = rowY + (cellSize + fm.getAscent()) / 2 - 2;
                    g2.drawString(valStr, vx, vy);
                }
            }
        }
    }

    public Vector<Vertex> vertexConnectivity(Vector<Vertex> vList) {
        Vector<Vertex> minCut = new Vector<Vertex>();
        int n = vList.size();
        if (n <= 1 || countComponents(vList) > 1) return minCut;

        boolean complete = true;
        for (Vertex v : vList) {
            if (v.getDegree() < n - 1) {
                complete = false;
                break;
            }
        }
        if (complete) {
            for (int i = 0; i < n - 1; i++) minCut.add(vList.get(i));
            return minCut;
        }

        int best = Integer.MAX_VALUE;
        for (int s = 0; s < n; s++) {
            for (int t = s + 1; t < n; t++) {
                if (vList.get(s).connectedToVertex(vList.get(t))) continue;
                int[][] cap = new int[2 * n][2 * n];
                for (int v = 0; v < n; v++) {
                    cap[2 * v][2 * v + 1] = (v == s || v == t) ? n : 1;
                    for (Vertex w : vList.get(v).connectedVertices) {
                        cap[2 * v + 1][2 * vList.indexOf(w)] = n;
                    }
                }
                int flow = maxFlow(cap, 2 * s + 1, 2 * t);
                if (flow < best) {
                    best = flow;
                    boolean[] reachable = residualReachable(cap, 2 * s + 1);
                    minCut = new Vector<Vertex>();
                    for (int v = 0; v < n; v++) {
                        if (reachable[2 * v] && !reachable[2 * v + 1]) {
                            minCut.add(vList.get(v));
                        }
                    }
                }
            }
        }
        return minCut;
    }

    public int getVertexConnectivity(Vector<Vertex> vList) {
        return vertexConnectivity(vList).size();
    }

    private int maxFlow(int[][] cap, int source, int sink) {
        int flow = 0;
        int size = cap.length;
        while (true) {
            int[] parent = new int[size];
            for (int i = 0; i < size; i++) parent[i] = -1;
            parent[source] = source;
            LinkedList<Integer> queue = new LinkedList<Integer>();
            queue.add(source);
            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.removeFirst();
                for (int w = 0; w < size; w++) {
                    if (parent[w] == -1 && cap[u][w] > 0) {
                        parent[w] = u;
                        queue.add(w);
                    }
                }
            }
            if (parent[sink] == -1) return flow;
            int bottleneck = Integer.MAX_VALUE;
            for (int w = sink; w != source; w = parent[w]) {
                bottleneck = Math.min(bottleneck, cap[parent[w]][w]);
            }
            for (int w = sink; w != source; w = parent[w]) {
                cap[parent[w]][w] -= bottleneck;
                cap[w][parent[w]] += bottleneck;
            }
            flow += bottleneck;
        }
    }

    private boolean[] residualReachable(int[][] cap, int source) {
        boolean[] reachable = new boolean[cap.length];
        LinkedList<Integer> queue = new LinkedList<Integer>();
        reachable[source] = true;
        queue.add(source);
        while (!queue.isEmpty()) {
            int u = queue.removeFirst();
            for (int w = 0; w < cap.length; w++) {
                if (!reachable[w] && cap[u][w] > 0) {
                    reachable[w] = true;
                    queue.add(w);
                }
            }
        }
        return reachable;
    }

    public Vector<Vertex> findIsolatedNodes(Vector<Vertex> vList) {
        Vector<Vertex> isolatedNodes = new Vector<Vertex>();
        for (Vertex v : vList) {
            if (v.getDegree() == 0) isolatedNodes.add(v);
        }
        return isolatedNodes;
    }

    public int getInDegree(Vertex v, Vector<Edge> eList) {
        int inDegree = 0;
        for (Edge e : eList) {
            if (e.vertex2 == v || (!e.directed && e.vertex1 == v)) inDegree++;
        }
        return inDegree;
    }

    public int getOutDegree(Vertex v, Vector<Edge> eList) {
        int outDegree = 0;
        for (Edge e : eList) {
            if (e.vertex1 == v || (!e.directed && e.vertex2 == v)) outDegree++;
        }
        return outDegree;
    }

    public int countComponents(Vector<Vertex> vList) {
        return countComponents(vList, null, null);
    }

    private int countComponents(Vector<Vertex> vList, Vertex removedVertex, Edge removedEdge) {
        Vector<Vertex> visited = new Vector<Vertex>();
        if (removedVertex != null) visited.add(removedVertex);
        int componentCount = 0;
        for (Vertex v : vList) {
            if (!visited.contains(v)) {
                componentCount++;
                dfsComponent(v, visited, removedEdge);
            }
        }
        return componentCount;
    }

    private void dfsComponent(Vertex v, Vector<Vertex> visited, Edge removedEdge) {
        visited.add(v);
        for (Vertex neighbor : v.connectedVertices) {
            if (removedEdge != null && ((removedEdge.vertex1 == v && removedEdge.vertex2 == neighbor) || (removedEdge.vertex2 == v && removedEdge.vertex1 == neighbor))) {
                continue;
            }
            if (!visited.contains(neighbor)) {
                dfsComponent(neighbor, visited, removedEdge);
            }
        }
    }

    public boolean isConnected(Vector<Vertex> vList) {
        return countComponents(vList) == 1;
    }

    public Vector<Vertex> findCutpoints(Vector<Vertex> vList) {
        Vector<Vertex> cutpoints = new Vector<Vertex>();
        int baseComponents = countComponents(vList);
        for (Vertex v : vList) {
            if (countComponents(vList, v, null) > baseComponents) cutpoints.add(v);
        }
        return cutpoints;
    }

    public int calculateEdgeConnectivity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        if (n <= 1 || countComponents(vList) > 1) return 0;
        int minEdgeCut = Integer.MAX_VALUE;
        for (int t = 1; t < n; t++) {
            int[][] cap = new int[n][n];
            for (Edge e : eList) {
                int a = vList.indexOf(e.vertex1);
                int b = vList.indexOf(e.vertex2);
                if (a != -1 && b != -1) {
                    cap[a][b]++;
                    cap[b][a]++;
                }
            }
            minEdgeCut = Math.min(minEdgeCut, maxFlow(cap, 0, t));
        }
        return minEdgeCut;
    }

    public double calculateDensity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        if (n <= 1) return 0.0;
        return (double) eList.size() / maxPossibleEdges(vList, eList);
    }

    private int maxPossibleEdges(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        boolean allDirected = !eList.isEmpty();
        for (Edge e : eList) {
            if (!e.directed) {
                allDirected = false;
                break;
            }
        }
        return allDirected ? n * (n - 1) : n * (n - 1) / 2;
    }

    public String classifyDensity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        int edges = eList.size();
        if (edges == 0) return "Sparse";
        int maxEdges = maxPossibleEdges(vList, eList);
        if (maxEdges <= n) return calculateDensity(vList, eList) > 0.5 ? "Dense" : "Sparse";
        return maxEdges - edges <= edges - n ? "Dense" : "Sparse";
    }

    public Vector<Edge> findBridges(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<Edge> bridges = new Vector<Edge>();
        int baseComponents = countComponents(vList);
        for (Edge e : eList) {
            if (countComponents(vList, null, e) > baseComponents) bridges.add(e);
        }
        return bridges;
    }

    public void generateWalks(Vector<Vertex> vList, int maxLength) {
        if (vList.isEmpty()) return;
        int[] walkCount = {0};
        for (Vertex start : vList) {
            Vector<Vertex> currentWalk = new Vector<Vertex>();
            currentWalk.add(start);
            generateWalksRecursive(currentWalk, vList, maxLength, walkCount);
        }
    }

    private void generateWalksRecursive(Vector<Vertex> currentWalk, Vector<Vertex> vList, int maxLength, int[] walkCount) {
        if (currentWalk.size() > maxLength) return;
        if (currentWalk.size() > 1) {
            System.out.print("Walk " + (++walkCount[0]) + ": ");
            for (Vertex v : currentWalk) System.out.print(v.name + " ");
            System.out.println();
        }
        Vertex last = currentWalk.lastElement();
        for (Vertex neighbor : last.connectedVertices) {
            currentWalk.add(neighbor);
            generateWalksRecursive(currentWalk, vList, maxLength, walkCount);
            currentWalk.removeElementAt(currentWalk.size() - 1);
        }
    }

    public void generateTrails(Vector<Vertex> vList, int maxLength) {
        if (vList.isEmpty()) return;
        int[] trailCount = {0};
        for (Vertex start : vList) {
            Vector<Vertex> currentTrail = new Vector<Vertex>();
            Vector<Edge> usedEdges = new Vector<Edge>();
            currentTrail.add(start);
            generateTrailsRecursive(currentTrail, usedEdges, vList, maxLength, trailCount);
        }
    }

    private void generateTrailsRecursive(Vector<Vertex> currentTrail, Vector<Edge> usedEdges, Vector<Vertex> vList, int maxLength, int[] trailCount) {
        if (currentTrail.size() > maxLength) return;
        if (currentTrail.size() > 1) {
            System.out.print("Trail " + (++trailCount[0]) + ": ");
            for (Vertex v : currentTrail) System.out.print(v.name + " ");
            System.out.println();
        }
        Vertex last = currentTrail.lastElement();
        for (Vertex neighbor : last.connectedVertices) {
            Edge newEdge = new Edge(last, neighbor);
            if (!edgeExistsInList(newEdge, usedEdges)) {
                currentTrail.add(neighbor);
                usedEdges.add(newEdge);
                generateTrailsRecursive(currentTrail, usedEdges, vList, maxLength, trailCount);
                currentTrail.removeElementAt(currentTrail.size() - 1);
                usedEdges.removeElementAt(usedEdges.size() - 1);
            }
        }
    }

    private boolean edgeExistsInList(Edge edge, Vector<Edge> edgeList) {
        for (Edge e : edgeList) {
            if ((e.vertex1 == edge.vertex1 && e.vertex2 == edge.vertex2) ||
                (e.vertex1 == edge.vertex2 && e.vertex2 == edge.vertex1)) {
                return true;
            }
        }
        return false;
    }
}