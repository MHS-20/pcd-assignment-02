package reactive;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DependencyGraphPanel extends JPanel {

    private final Map<String, Point> packageCenters = new HashMap<>();
    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Random rand = new Random();

    int padding = 200;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


    public void reset() {
        nodePositions.clear();
        edges.clear();
        repaint();
    }

    public void addFileWithDependencies(String fileName, Set<String> deps) {
        String pkg = extractPackage(fileName);
        ensureNode(fileName, pkg);
        for (String dep : deps) {
            String depPkg = extractPackage(dep);
            ensureNode(dep, depPkg);
            edges.computeIfAbsent(fileName, k -> new HashSet<>()).add(dep);
        }
        repaint();
    }

    private String extractPackage(String fileName) {
        return fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : "default";
    }


    private void ensureNode(String name, String packageName) {
        if (!nodePositions.containsKey(name)) {
            Point center = getOrCreatePackageCenter(packageName);
            int jitter = 50;
            int x = center.x + rand.nextInt(jitter * 2) - jitter;
            int y = center.y + rand.nextInt(jitter * 2) - jitter;
            nodePositions.put(name, new Point(x, y));
        }
    }


    private Point getOrCreatePackageCenter(String packageName) {
        return packageCenters.computeIfAbsent(packageName, pkg -> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = padding + rand.nextInt(screenSize.width - padding * 2);
            int y = padding + rand.nextInt(screenSize.height - padding * 2);
            return new Point(x, y);
        });
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
            g2d.fillOval(p.x - 20, p.y - 20, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString(entry.getKey(), p.x - 20, p.y - 25);
        }
    }
}

