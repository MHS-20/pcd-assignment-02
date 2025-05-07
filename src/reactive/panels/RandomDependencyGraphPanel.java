package reactive.panels;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.util.*;

public class RandomDependencyGraphPanel extends JPanel implements DependencyGraphPanel {

    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Color> fileColors = new HashMap<>();
    private final Random rand = new Random(42);

    int padding = 5;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public void reset() {
        nodePositions.clear();
        edges.clear();
        fileColors.clear();
        repaint();
    }

    public void addDependency(SingleDependencyResult dependency) {
        ensureNode(dependency.fileName, true);
        ensureNode(dependency.dependency, false);
        edges.computeIfAbsent(dependency.fileName, k -> new HashSet<>()).add(dependency.dependency);
        repaint();
    }

    public Map<String, Color> getFileColors() {
        return Collections.unmodifiableMap(fileColors);
    }

    private void ensureNode(String name, boolean isSrcFile) {
        if (!nodePositions.containsKey(name)) {
            nodePositions.put(name, new Point(
                    padding + rand.nextInt(screenSize.width - padding * 2),
                    padding + rand.nextInt(screenSize.height - padding * 2)
            ));
        }

        name = removeJavaExtension(name);
        if (isSrcFile) {
            fileColors.put(name, new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
        } else {
            int grayValue = 80 + rand.nextInt(120);
            fileColors.putIfAbsent(name, new Color(grayValue, grayValue, grayValue));
        }
    }

    public String removeJavaExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".java")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return fileName
                .replace("/", ".")
                .replace("\\", ".");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setPreferredSize(screenSize);
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
            String name = entry.getKey();
            Point p = entry.getValue();

            String pkg = removeJavaExtension(name);
            Color color = fileColors.getOrDefault(pkg, Color.BLUE);

            g2d.setColor(color);
            //g2d.setColor(Color.BLUE);
            g2d.fillOval(p.x - 20, p.y - 20, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, p.x - 20, p.y - 25);
        }
    }
}