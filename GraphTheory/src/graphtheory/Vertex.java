package graphtheory;

import java.awt.*;
import java.util.Vector;

public class Vertex implements Comparable {

    public String name;
    public Point location;
    public boolean wasFocused;
    public boolean wasClicked;
    private int size = 38;
    public Vector<Vertex> connectedVertices;

    public Vertex(String name, int x, int y) {
        this.name = name;
        this.location = new Point(x, y);
        this.connectedVertices = new Vector<Vertex>();
    }

    public void addVertex(Vertex v) {
        connectedVertices.add(v);
    }

    public boolean hasIntersection(int x, int y) {
        return Math.hypot(x - location.x, y - location.y) <= size / 2.0;
    }

    public boolean connectedToVertex(Vertex v) {
        return connectedVertices.contains(v);
    }

    public int getDegree() {
        return connectedVertices.size();
    }

    @Override
    public int compareTo(Object v) {
        return Integer.compare(((Vertex) v).getDegree(), getDegree());
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Soft Outer Glow / Ring on Select or Focus
        if (wasClicked) {
            g2.setColor(new Color(255, 51, 102, 80));
            g2.fillOval(location.x - 26, location.y - 26, 52, 52);
            g2.setColor(Canvas.ACCENT_PINK);
        } else if (wasFocused) {
            g2.setColor(new Color(0, 210, 255, 60));
            g2.fillOval(location.x - 24, location.y - 24, 48, 48);
            g2.setColor(Canvas.ACCENT_CYAN);
        } else {
            g2.setColor(Canvas.CARD_BORDER);
        }

        // Main Node Circle
        g2.fillOval(location.x - size / 2, location.y - size / 2, size, size);

        // Core Fill
        g2.setColor(Canvas.CARD_BG);
        g2.fillOval(location.x - (size - 6) / 2, location.y - (size - 6) / 2, size - 6, size - 6);

        // Node Label
        g2.setColor(Canvas.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int textX = location.x - fm.stringWidth(name) / 2;
        int textY = location.y + fm.getAscent() / 2 - 2;
        g2.drawString(name, textX, textY);
    }
}