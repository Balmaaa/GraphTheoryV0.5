package graphtheory;

import java.awt.*;

public class Edge {

    public Vertex vertex1;
    public Vertex vertex2;
    public boolean wasFocused;
    public boolean wasClicked;
    public boolean directed;

    public Edge(Vertex v1, Vertex v2) {
        this(v1, v2, false);
    }

    public Edge(Vertex v1, Vertex v2, boolean directed) {
        this.vertex1 = v1;
        this.vertex2 = v2;
        this.directed = directed;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (wasClicked) {
            g2.setColor(Canvas.ACCENT_PINK);
            g2.setStroke(new BasicStroke(3f));
        } else if (wasFocused) {
            g2.setColor(Canvas.ACCENT_CYAN);
            g2.setStroke(new BasicStroke(2.5f));
        } else {
            g2.setColor(new Color(80, 92, 120));
            g2.setStroke(new BasicStroke(2f));
        }

        g2.drawLine(vertex1.location.x, vertex1.location.y, vertex2.location.x, vertex2.location.y);

        if (directed) {
            drawArrowHead(g2);
        }
    }

    private void drawArrowHead(Graphics2D g2) {
        double dx = vertex2.location.x - vertex1.location.x;
        double dy = vertex2.location.y - vertex1.location.y;
        double angle = Math.atan2(dy, dx);
        int len = 12;

        int x1 = (int) (vertex2.location.x - 22 * Math.cos(angle));
        int y1 = (int) (vertex2.location.y - 22 * Math.sin(angle));

        int x2 = (int) (x1 - len * Math.cos(angle - Math.PI / 7));
        int y2 = (int) (y1 - len * Math.sin(angle - Math.PI / 7));

        int x3 = (int) (x1 - len * Math.cos(angle + Math.PI / 7));
        int y3 = (int) (y1 - len * Math.sin(angle + Math.PI / 7));

        g2.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

    public boolean hasIntersection(int x, int y) {
        double x1 = vertex1.location.x, y1 = vertex1.location.y;
        double x2 = vertex2.location.x, y2 = vertex2.location.y;
        double l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0) return Math.hypot(x - x1, y - y1) <= 6;
        double t = Math.max(0, Math.min(1, ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / l2));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return Math.hypot(x - projX, y - projY) <= 8;
    }
}