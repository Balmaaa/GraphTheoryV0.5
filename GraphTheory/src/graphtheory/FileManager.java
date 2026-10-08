package graphtheory;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFileChooser;

public class FileManager {

    public JFileChooser jF;

    public FileManager() {
        jF = new JFileChooser();
    }

    public void saveFile(Vector<Vertex> vList, Vector<Edge> eList, File fName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fName));

            out.write("" + vList.size());
            out.newLine();
            for (Vertex v : vList) {
                out.write(v.name);
                out.newLine();
            }

            // Write Matrix with Weights
            double[][] matrix = new double[vList.size()][vList.size()];
            for (Edge e : eList) {
                int a = vList.indexOf(e.vertex1);
                int b = vList.indexOf(e.vertex2);
                if (a != -1 && b != -1) {
                    matrix[a][b] = e.weight;
                    if (!e.directed) {
                        matrix[b][a] = e.weight;
                    }
                }
            }

            for (int i = 0; i < vList.size(); i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < vList.size(); j++) {
                    if (j > 0) line.append(",");
                    line.append(matrix[i][j]);
                }
                out.write(line.toString());
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
                for (int i = 0; i < size; i++) {
                    Vertex v = new Vertex(data.nextLine(), 0, 0);
                    vertexList.add(v);
                }

                String[] adjacencyLines = new String[vertexList.size()];
                for (int j = 0; j < vertexList.size(); j++) {
                    adjacencyLines[j] = data.nextLine();
                }

                for (int j = 0; j < vertexList.size(); j++) {
                    String[] tokens = adjacencyLines[j].split(",");
                    for (int k = j + 1; k < vertexList.size(); k++) {
                        double weightForward = 0, weightBackward = 0;
                        if (tokens.length > k) {
                            weightForward = Double.parseDouble(tokens[k]);
                        }
                        String[] otherTokens = adjacencyLines[k].split(",");
                        if (otherTokens.length > j) {
                            weightBackward = Double.parseDouble(otherTokens[j]);
                        }

                        if (weightForward > 0 || weightBackward > 0) {
                            vertexList.get(j).addVertex(vertexList.get(k));
                            vertexList.get(k).addVertex(vertexList.get(j));

                            if (weightForward > 0 && weightBackward > 0 && weightForward == weightBackward) {
                                edgeList.add(new Edge(vertexList.get(j), vertexList.get(k), false, weightForward));
                            } else if (weightForward > 0) {
                                edgeList.add(new Edge(vertexList.get(j), vertexList.get(k), true, weightForward));
                            } else if (weightBackward > 0) {
                                edgeList.add(new Edge(vertexList.get(k), vertexList.get(j), true, weightBackward));
                            }
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