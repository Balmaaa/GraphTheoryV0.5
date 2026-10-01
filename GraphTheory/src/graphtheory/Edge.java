/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtheory;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author mk
 */
public class Edge {

    public Vertex vertex1;
    public Vertex vertex2;
    public boolean wasFocused;
    public boolean wasClicked;
    public boolean directed;    // when directed, the edge goes from vertex1 to vertex2

    public Edge(Vertex v1, Vertex v2) {
        this(v1, v2, false);
    }

    public Edge(Vertex v1, Vertex v2, boolean directed) {
        vertex1 = v1;
        vertex2 = v2;
        this.directed = directed;
    }

    public void draw(Graphics g) {
        if (wasClicked) {
            g.setColor(Color.red);
        } else if (wasFocused) {
            g.setColor(Color.blue);
        } else {
            g.setColor(Color.black);
        }
        g.drawLine(vertex1.location.x, vertex1.location.y, vertex2.location.x, vertex2.location.y);
        if (directed) {
            drawArrowHead(g);
        }
    }

    private void drawArrowHead(Graphics g) {
        double dx = vertex2.location.x - vertex1.location.x;
        double dy = vertex2.location.y - vertex1.location.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            return;
        }
        double ux = dx / length;
        double uy = dy / length;
        double tipX = vertex2.location.x - ux * 20;     // stop at the vertex outline
        double tipY = vertex2.location.y - uy * 20;
        double headLength = 12;
        double headWidth = 6;
        int[] xs = {(int) tipX,
            (int) (tipX - ux * headLength - uy * headWidth),
            (int) (tipX - ux * headLength + uy * headWidth)};
        int[] ys = {(int) tipY,
            (int) (tipY - uy * headLength + ux * headWidth),
            (int) (tipY - uy * headLength - ux * headWidth)};
        g.fillPolygon(xs, ys, 3);
    }

    public boolean hasIntersection(int x, int y) {
        double x1 = vertex1.location.x;
        double y1 = vertex1.location.y;
        double dx = vertex2.location.x - x1;
        double dy = vertex2.location.y - y1;
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((x - x1) * dx + (y - y1) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        double distX = x - (x1 + t * dx);
        double distY = y - (y1 + t * dy);
        return distX * distX + distY * distY <= 8 * 8;
    }
}
