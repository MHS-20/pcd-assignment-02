package reactive;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DependencyGraphPanel extends JPanel {

    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Random rand = new Random();

    int padding = 150;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


    public void reset() {
        nodePositions.clear();
        edges.clear();
        repaint();
    }

    public void addFileWithDependencies(String file, Set<String> deps) {
        ensureNode(file);
        for (String dep : deps) {
            ensureNode(dep);
            edges.computeIfAbsent(file, k -> new HashSet<>()).add(dep);
        }
        repaint();
    }

    private void ensureNode(String name) {
        if (!nodePositions.containsKey(name)) {
            nodePositions.put(name, new Point(padding + rand.nextInt(screenSize.width - padding * 2),
                    padding + rand.nextInt(screenSize.height - padding * 2)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.LIGHT_GRAY);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.GRAY);
        for (var from : edges.keySet()) {
            Point fromP = nodePositions.get(from);
            for (var to : edges.get(from)) {
                Point toP = nodePositions.get(to);
                if (fromP != null && toP != null) {
                    g2d.drawLine(fromP.x, fromP.y, toP.x, toP.y);
                }
            }
        }

        for (var entry : nodePositions.entrySet()) {
            Point p = entry.getValue();
            g2d.setColor(Color.BLUE);
            g2d.fillOval(p.x - 20, p.y - 20, 40, 40);
            g2d.setColor(Color.BLACK);
            g2d.drawString(entry.getKey(), p.x - 20, p.y - 25);
        }
    }
}

