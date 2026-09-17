/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Comparator;
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
        Vector<Vertex> origList = new Vector<Vertex>();
        Vector<Vertex> tempList = new Vector<Vertex>();
        Vector<Vertex> toBeRemoved = new Vector<Vertex>();
        Vertex victim;


        origList.setSize(vList.size());
        Collections.copy(origList, vList);

        int maxPossibleRemove = 0;
        while (graphConnectivity(origList)) {
            Collections.sort(origList, new ascendingDegreeComparator());
            maxPossibleRemove = origList.firstElement().getDegree();

            for (Vertex v : origList) {
                if (v.getDegree() == maxPossibleRemove) {
                    for (Vertex z : v.connectedVertices) {
                        if (!tempList.contains(z)) {
                            tempList.add(z);
                        }
                    }
                }
            }

            while (graphConnectivity(origList) && tempList.size() > 0) {
                Collections.sort(tempList, new descendingDegreeComparator());
                victim = tempList.firstElement();
                tempList.removeElementAt(0);
                origList.remove(victim);
                for (Vertex x : origList) {
                    x.connectedVertices.remove(victim);
                }
                toBeRemoved.add(victim);
            }
            tempList.removeAllElements();
        }

        return toBeRemoved;
    }

    private boolean graphConnectivity(Vector<Vertex> vList) {

        Vector<Vertex> visitedList = new Vector<Vertex>();

        recurseGraphConnectivity(vList.firstElement().connectedVertices, visitedList); //recursive function
        if (visitedList.size() != vList.size()) {
            return false;
        } else {
            return true;
        }
    }

    private void recurseGraphConnectivity(Vector<Vertex> vList, Vector<Vertex> visitedList) {
        for (Vertex v : vList) {
            {
                if (!visitedList.contains(v)) {
                    visitedList.add(v);
                    recurseGraphConnectivity(v.connectedVertices, visitedList);
                }
            }
        }
    }

    private class ascendingDegreeComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return 1;
            } else if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class descendingDegreeComparator implements Comparator {

        public int compare(Object v1, Object v2) {

            if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return -1;
            } else if (((Vertex) v1).getDegree() > ((Vertex) v2).getDegree()) {
                return 1;
            } else {
                return 0;
            }
        }
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

    public int countComponents(Vector<Vertex> vList) {
        if (vList.isEmpty()) return 0;
        
        Vector<Vertex> visited = new Vector<Vertex>();
        int componentCount = 0;
        
        for (Vertex v : vList) {
            if (!visited.contains(v)) {
                componentCount++;
                // DFS to mark all vertices in this component
                dfsComponent(v, visited);
            }
        }
        
        return componentCount;
    }
    
    private void dfsComponent(Vertex v, Vector<Vertex> visited) {
        visited.add(v);
        for (Vertex neighbor : v.connectedVertices) {
            if (!visited.contains(neighbor)) {
                dfsComponent(neighbor, visited);
            }
        }
    }

    public int calculateEdgeConnectivity(Vector<Vertex> vList, Vector<Edge> eList) {
        if (vList.isEmpty() || countComponents(vList) > 1) {
            return 0; // Graph is already disconnected
        }
        
        int minEdgeCuts = Integer.MAX_VALUE;
        
        // Try removing each edge and check connectivity
        for (Edge e : eList) {
            // Temporarily remove edge
            e.vertex1.connectedVertices.remove(e.vertex2);
            e.vertex2.connectedVertices.remove(e.vertex1);
            
            // Check if graph becomes disconnected
            if (countComponents(vList) > 1) {
                minEdgeCuts = 1; // This edge is a bridge
                // Restore edge
                e.vertex1.addVertex(e.vertex2);
                e.vertex2.addVertex(e.vertex1);
                return 1; // Can't get lower than 1
            }
            
            // Restore edge
            e.vertex1.addVertex(e.vertex2);
            e.vertex2.addVertex(e.vertex1);
        }
        
        // If no single edge removal disconnects, try pairs
        if (minEdgeCuts == Integer.MAX_VALUE && eList.size() >= 2) {
            for (int i = 0; i < eList.size(); i++) {
                for (int j = i + 1; j < eList.size(); j++) {
                    Edge e1 = eList.get(i);
                    Edge e2 = eList.get(j);
                    
                    // Temporarily remove both edges
                    e1.vertex1.connectedVertices.remove(e1.vertex2);
                    e1.vertex2.connectedVertices.remove(e1.vertex1);
                    e2.vertex1.connectedVertices.remove(e2.vertex2);
                    e2.vertex2.connectedVertices.remove(e2.vertex1);
                    
                    if (countComponents(vList) > 1) {
                        minEdgeCuts = 2;
                        
                        // Restore edges
                        e1.vertex1.addVertex(e1.vertex2);
                        e1.vertex2.addVertex(e1.vertex1);
                        e2.vertex1.addVertex(e2.vertex2);
                        e2.vertex2.addVertex(e2.vertex1);
                        return 2;
                    }
                    
                    // Restore edges
                    e1.vertex1.addVertex(e1.vertex2);
                    e1.vertex2.addVertex(e1.vertex1);
                    e2.vertex1.addVertex(e2.vertex2);
                    e2.vertex2.addVertex(e2.vertex1);
                }
            }
        }
        
        return (minEdgeCuts == Integer.MAX_VALUE) ? eList.size() : minEdgeCuts;
    }

    public double calculateDensity(Vector<Vertex> vList, Vector<Edge> eList) {
        int n = vList.size();
        if (n <= 1) return 0.0;
        
        int maxPossibleEdges = n * (n - 1) / 2; // For undirected graph
        return (double) eList.size() / maxPossibleEdges;
    }

    public Vector<Edge> findBridges(Vector<Vertex> vList, Vector<Edge> eList) {
        Vector<Edge> bridges = new Vector<Edge>();
        
        for (Edge e : eList) {
            // Temporarily remove edge
            e.vertex1.connectedVertices.remove(e.vertex2);
            e.vertex2.connectedVertices.remove(e.vertex1);
            
            // Check if graph becomes disconnected
            if (countComponents(vList) > 1) {
                bridges.add(e);
            }
            
            // Restore edge
            e.vertex1.addVertex(e.vertex2);
            e.vertex2.addVertex(e.vertex1);
        }
        
        return bridges;
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
