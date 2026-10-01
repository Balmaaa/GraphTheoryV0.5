/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFileChooser;

/**
 *
 * @author mk
 */
public class FileManager {

    public JFileChooser jF;

    public FileManager() {
        jF = new JFileChooser();


    }

    public void saveFile(Vector<Vertex> vList, Vector<Edge> eList, File fName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fName));

            out.write(""+vList.size());
            out.newLine();
            for (Vertex v : vList) {
                out.write(v.name);
                out.newLine();
            }
            // row i, column j is 1 when an edge goes from i to j (both ways for undirected edges)
            int[][] matrix = new int[vList.size()][vList.size()];
            for (Edge e : eList) {
                int a = vList.indexOf(e.vertex1);
                int b = vList.indexOf(e.vertex2);
                matrix[a][b] = 1;
                if (!e.directed) {
                    matrix[b][a] = 1;
                }
            }
            for (int i = 0; i < vList.size(); i++) {
                for (int j = 0; j < vList.size(); j++) {
                    out.write("" + matrix[i][j]);
                }
                out.newLine();
            }
            for (int k = 0; k < vList.size(); k++) {
                out.write(vList.get(k).location.x + "," + vList.get(k).location.y);
                out.newLine();
            }
            out.close();

        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public Vector<Vector> loadFile(File fName) {
        Vector<Vertex> vertexList = new Vector<Vertex>();
        Vector<Edge> edgeList = new Vector<Edge>();
        Vector<Vector> file = new Vector<Vector>();
        try {
            FileReader f = new FileReader(fName.toString());
            Scanner data = new Scanner(f);
            if (data.hasNext()) {
                int size = Integer.parseInt(data.nextLine());
                for (int i = 0; i < size; i++) {//vertex only
                    Vertex v = new Vertex(data.nextLine(), 0, 0);
                    vertexList.add(v);
                }

                String[] adjacencyLines = new String[vertexList.size()];
                for (int j = 0; j < vertexList.size(); j++) {
                    adjacencyLines[j] = data.nextLine();
                    System.out.println(adjacencyLines[j]);
                }

                for (int j = 0; j < vertexList.size(); j++) { // adjacency list
                    for (int k = 0; k < vertexList.size(); k++) {
                        if (adjacencyLines[j].charAt(k) == '1' || adjacencyLines[k].charAt(j) == '1') {
                            vertexList.get(j).addVertex(vertexList.get(k));
                        }
                    }

                    for (int l = j + 1; l < vertexList.size(); l++) { //edges
                        boolean forward = adjacencyLines[j].charAt(l) == '1';
                        boolean backward = adjacencyLines[l].charAt(j) == '1';
                        if (forward && backward) {
                            edgeList.add(new Edge(vertexList.get(j), vertexList.get(l)));
                        } else if (forward) {
                            edgeList.add(new Edge(vertexList.get(j), vertexList.get(l), true));
                        } else if (backward) {
                            edgeList.add(new Edge(vertexList.get(l), vertexList.get(j), true));
                        }
                    }
                }

                if (data.hasNextLine()) {
                    for (Vertex v : vertexList) {
                        String pos = data.nextLine();
                        v.location = new Point(Integer.parseInt(pos.split(",")[0]), Integer.parseInt(pos.split(",")[1]));
                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
        file.add(vertexList);
        file.add(edgeList);
        return file;
    }
}
