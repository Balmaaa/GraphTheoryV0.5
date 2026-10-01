/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Vector;

/**
 *
 * @author mk
 */
public class GraphProperties {

    public int[][] adjacencyMatrix;
    public int[][] distanceMatrix;
    public Vector<VertexPair> vpList;

    public int[][] generateAdjacencyMatrix(Vector<Vertex> vList, Vector<Edge> eList) {
        adjacencyMatrix = new int[vList.size()][vList.size()];

        for (int a = 0; a < vList.size(); a++)//initialize
        {
            for (int b = 0; b < vList.size(); b++) {
                adjacencyMatrix[a][b] = 0;
            }
        }

        for (int i = 0; i < eList.size(); i++) {
            adjacencyMatrix[vList.indexOf(eList.get(i).vertex1)][vList.indexOf(eList.get(i).vertex2)] = 1;
            adjacencyMatrix[vList.indexOf(eList.get(i).vertex2)][vList.indexOf(eList.get(i).vertex1)] = 1;
        }
        return adjacencyMatrix;
    }

    public int[][] generateDistanceMatrix(Vector<Vertex> vList) {
        distanceMatrix = new int[vList.size()][vList.size()];

        for (int a = 0; a < vList.size(); a++)//initialize
        {
            for (int b = 0; b < vList.size(); b++) {
                distanceMatrix[a][b] = 0;
            }
        }

        VertexPair vp;
        int shortestDistance;
        for (int i = 0; i < vList.size(); i++) {
            for (int j = i + 1; j < vList.size(); j++) {
                vp = new VertexPair(vList.get(i), vList.get(j));
                shortestDistance = vp.getShortestDistance();
                distanceMatrix[vList.indexOf(vp.vertex1)][vList.indexOf(vp.vertex2)] = shortestDistance;
                distanceMatrix[vList.indexOf(vp.vertex2)][vList.indexOf(vp.vertex1)] = shortestDistance;
            }
        }
        return distanceMatrix;
    }

    public void displayContainers(Vector<Vertex> vList) {
        vpList = new Vector<VertexPair>();
        int[] kWideGraph = new int[10];
        for (int i = 0; i < kWideGraph.length; i++) {
            kWideGraph[i] = -1;
        }



        VertexPair vp;

        for (int a = 0; a < vList.size(); a++) {    // assign vertex pairs
            for (int b = a + 1; b < vList.size(); b++) {
                vp = new VertexPair(vList.get(a), vList.get(b));
                vpList.add(vp);
                int longestWidth = 0;
                System.out.println(">Vertex Pair " + vList.get(a).name + "-" + vList.get(b).name + "\n All Paths:");
                vp.generateVertexDisjointPaths();
                for (int i = 0; i < vp.VertexDisjointContainer.size(); i++) {//for every container of the vertex pair
                    int width = vp.VertexDisjointContainer.get(i).size();
                    Collections.sort(vp.VertexDisjointContainer.get(i), new descendingWidthComparator());
                    int longestLength = vp.VertexDisjointContainer.get(i).firstElement().size();
                    longestWidth = Math.max(longestWidth, width);
                    System.out.println("\tContainer " + i + " - " + "Width=" + width + " - Length=" + longestLength);

                    for (int j = 0; j < vp.VertexDisjointContainer.get(i).size(); j++) //for every path in the container
                    {
                        System.out.print("\t\tPath " + j + "\n\t\t\t");
                        for (int k = 0; k < vp.VertexDisjointContainer.get(i).get(j).size(); k++) {
                            System.out.print("-" + vp.VertexDisjointContainer.get(i).get(j).get(k).name);
                        }
                        System.out.println();
                    }

                }
                //d-wide for vertexPair
                for (int k = 1; k <= longestWidth; k++) { // 1-wide, 2-wide, 3-wide...
                    int minLength = 999;
                    for (int m = 0; m < vp.VertexDisjointContainer.size(); m++) // for each container with k-wide select shortest length
                    {
                        minLength = Math.min(minLength, vp.VertexDisjointContainer.get(m).size());
                    }
                    if (minLength != 999) {
                        System.out.println(k + "-wide for vertexpair(" + vp.vertex1.name + "-" + vp.vertex2.name + ")=" + minLength);
                        kWideGraph[k] = Math.max(kWideGraph[k], minLength);
                    }
                }
            }
        }

        for (int i = 0; i < kWideGraph.length; i++) {
            if (kWideGraph[i] != -1) {
                System.out.println("D" + i + "(G)=" + kWideGraph[i]);
            }
        }


    }

    public void drawAdjacencyMatrix(Graphics g, Vector<Vertex> vList, int x, int y) {
        int cSize = 20;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y-30, vList.size() * cSize+cSize, vList.size() * cSize+cSize);
        g.setColor(Color.black);
        g.drawString("AdjacencyMatrix", x, y - cSize);
        for (int i = 0; i < vList.size(); i++) {
            g.setColor(Color.RED);
            g.drawString(vList.get(i).name, x + cSize + i * cSize, y);
            g.drawString(vList.get(i).name, x, cSize + i * cSize + y);
            g.setColor(Color.black);
            for (int j = 0; j < vList.size(); j++) {
                g.drawString("" + adjacencyMatrix[i][j], x + cSize * (j + 1), y + cSize * (i + 1));
            }
        }
    }

    public void drawDistanceMatrix(Graphics g, Vector<Vertex> vList, int x, int y) {
        int cSize = 20;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y-30, vList.size() * cSize+cSize, vList.size() * cSize+cSize);
        g.setColor(Color.black);
        g.drawString("ShortestPathMatrix", x, y - cSize);
        for (int i = 0; i < vList.size(); i++) {
            g.setColor(Color.RED);
            g.drawString(vList.get(i).name, x + cSize + i * cSize, y);
            g.drawString(vList.get(i).name, x, cSize + i * cSize + y);
            g.setColor(Color.black);
            for (int j = 0; j < vList.size(); j++) {
                g.drawString("" + distanceMatrix[i][j], x + cSize * (j + 1), y + cSize * (i + 1));
            }
        }
    }

    public Vector<Vertex> vertexConnectivity(Vector<Vertex> vList) {
        Vector<Vertex> minCut = new Vector<Vertex>();
        int n = vList.size();
        if (n <= 1 || countComponents(vList) > 1) {
            return minCut;
        }

        boolean complete = true;
        for (Vertex v : vList) {
            if (v.getDegree() < n - 1) {
                complete = false;
                break;
            }
        }
        if (complete) {
            for (int i = 0; i < n - 1; i++) {
                minCut.add(vList.get(i));
            }
            return minCut;
        }

        int best = Integer.MAX_VALUE;
        for (int s = 0; s < n; s++) {
            for (int t = s + 1; t < n; t++) {
                if (vList.get(s).connectedToVertex(vList.get(t))) {
                    continue;
                }
                // split every vertex v into v_in (2v) and v_out (2v+1) joined by a unit-capacity arc
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

    // Edmonds-Karp; leaves the residual capacities in cap
    private int maxFlow(int[][] cap, int source, int sink) {
        int flow = 0;
        int size = cap.length;
        while (true) {
            int[] parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = -1;
            }
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
            if (parent[sink] == -1) {
                return flow;
            }
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
    private class descendingWidthComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vector<Vertex>) v1).size() > (((Vector<Vertex>) v2).size())) {
                return -1;
            } else if (((Vector<Vertex>) v1).size() < (((Vector<Vertex>) v2).size())) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    // New feature implementations
    
    public Vector<Vertex> findIsolatedNodes(Vector<Vertex> vList) {
        Vector<Vertex> isolatedNodes = new Vector<Vertex>();
        for (Vertex v : vList) {
            if (v.getDegree() == 0) {
                isolatedNodes.add(v);
            }
        }
        return isolatedNodes;
    }

    public int getInDegree(Vertex v, Vector<Edge> eList) {
        int inDegree = 0;
        for (Edge e : eList) {
            if (e.vertex2 == v || (!e.directed && e.vertex1 == v)) {
                inDegree++;
            }
        }
        return inDegree;
    }

    public int getOutDegree(Vertex v, Vector<Edge> eList) {
        int outDegree = 0;
        for (Edge e : eList) {
            if (e.vertex1 == v || (!e.directed && e.vertex2 == v)) {
                outDegree++;
            }
        }
        return outDegree;
    }

    public int countComponents(Vector<Vertex> vList) {
        return countComponents(vList, null, null);
    }

    // counts components while ignoring removedVertex and removedEdge (either may be null)
    private int countComponents(Vector<Vertex> vList, Vertex removedVertex, Edge removedEdge) {
        Vector<Vertex> visited = new Vector<Vertex>();
        if (removedVertex != null) {
            visited.add(removedVertex);
        }
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
            if (removedEdge != null
                    && ((removedEdge.vertex1 == v && removedEdge.vertex2 == neighbor)
                    || (removedEdge.vertex2 == v && removedEdge.vertex1 == neighbor))) {
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
            if (countComponents(vList, v, null) > baseComponents) {
                cutpoints.add(v);
            }
        }
        return cutpoints;
    }

    public int calculateEdgeConnectivity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        if (n <= 1 || countComponents(vList) > 1) {
            return 0;
        }

        // lambda(G) = min over t of the max number of edge-disjoint paths between vertex 0 and t
        int minEdgeCut = Integer.MAX_VALUE;
        for (int t = 1; t < n; t++) {
            int[][] cap = new int[n][n];
            for (Edge e : eList) {
                int a = vList.indexOf(e.vertex1);
                int b = vList.indexOf(e.vertex2);
                cap[a][b]++;
                cap[b][a]++;
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

    // sparse when |E| is nearer |V|, dense when |E| is nearer the maximum edge count (~|V|^2)
    public String classifyDensity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        int edges = eList.size();
        if (edges == 0) {
            return "Sparse";
        }
        int maxEdges = maxPossibleEdges(vList, eList);
        if (maxEdges <= n) {
            return calculateDensity(vList, eList) > 0.5 ? "Dense" : "Sparse";
        }
        return maxEdges - edges <= edges - n ? "Dense" : "Sparse";
    }

    public Vector<Edge> findBridges(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<Edge> bridges = new Vector<Edge>();
        int baseComponents = countComponents(vList);
        
        for (Edge e : eList) {
            if (countComponents(vList, null, e) > baseComponents) {
                bridges.add(e);
            }
        }
        
        return bridges;
    }

    public void printGeodesics(Vector<Vertex> vList) {
        System.out.println("=== CONNECTEDNESS & GEODESICS ===");
        for (int i = 0; i < vList.size(); i++) {
            for (int j = i + 1; j < vList.size(); j++) {
                VertexPair vp = new VertexPair(vList.get(i), vList.get(j));
                Vector<Vertex> geodesic = vp.getShortestPath();
                System.out.print(vList.get(i).name + "-" + vList.get(j).name + ": ");
                if (geodesic == null) {
                    System.out.println("not connected (unreachable)");
                } else {
                    System.out.print("connected, geodesic ");
                    for (int k = 0; k < geodesic.size(); k++) {
                        System.out.print((k > 0 ? "-" : "") + geodesic.get(k).name);
                    }
                    System.out.println(", distance " + (geodesic.size() - 1));
                }
            }
        }
    }
    public void generateWalks(Vector<Vertex> vList, int maxLength) {
        System.out.println("=== WALKS ===");
        if (vList.isEmpty()) return;
        
        int[] walkCount = {0}; // Use array to pass by reference
        for (Vertex start : vList) {
            Vector<Vertex> currentWalk = new Vector<Vertex>();
            currentWalk.add(start);
            generateWalksRecursive(currentWalk, vList, maxLength, walkCount);
        }
    }
    
    private void generateWalksRecursive(Vector<Vertex> currentWalk, Vector<Vertex> vList, int maxLength, int[] walkCount) {
        if (currentWalk.size() > maxLength) return;
        
        // Print current walk
        if (currentWalk.size() > 1) {
            System.out.print("Walk " + (++walkCount[0]) + ": ");
            for (Vertex v : currentWalk) {
                System.out.print(v.name + " ");
            }
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
        System.out.println("=== TRAILS ===");
        if (vList.isEmpty()) return;
        
        int[] trailCount = {0}; // Use array to pass by reference
        for (Vertex start : vList) {
            Vector<Vertex> currentTrail = new Vector<Vertex>();
            Vector<Edge> usedEdges = new Vector<Edge>();
            currentTrail.add(start);
            generateTrailsRecursive(currentTrail, usedEdges, vList, maxLength, trailCount);
        }
    }
    
    private void generateTrailsRecursive(Vector<Vertex> currentTrail, Vector<Edge> usedEdges, 
                                        Vector<Vertex> vList, int maxLength, int[] trailCount) {
        if (currentTrail.size() > maxLength) return;
        
        // Print current trail
        if (currentTrail.size() > 1) {
            System.out.print("Trail " + (++trailCount[0]) + ": ");
            for (Vertex v : currentTrail) {
                System.out.print(v.name + " ");
            }
            System.out.println();
        }
        
        Vertex last = currentTrail.lastElement();
        for (Vertex neighbor : last.connectedVertices) {
            // Check if edge exists and hasn't been used
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
