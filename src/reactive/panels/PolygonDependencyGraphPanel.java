package reactive.panels;

import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class PolygonDependencyGraphPanel extends JPanel implements DependencyGraphPanel {

    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Color> fileColors = new HashMap<>();
    private final Set<String> allNodes = new LinkedHashSet<>();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final Random rand = new Random(42);
    int padding = 1;

    public void reset() {
        nodePositions.clear();
        edges.clear();
        fileColors.clear();
        allNodes.clear();
        repaint();
    }

    public Map<String, Color> getFileColors() {
        return Collections.unmodifiableMap(fileColors);
    }

    public void addDependency(SingleDependencyResult dependency) {
        allNodes.add(dependency.fileName);
        allNodes.add(dependency.dependency);

        ensureColor(dependency.fileName, true);
        ensureColor(dependency.dependency, false);

        edges.computeIfAbsent(dependency.fileName, k -> new HashSet<>()).add(dependency.dependency);
        recalculateNodePositions();
        repaint();
    }

    public Color generateBrightColor() {
        float hue = rand.nextFloat();
        float saturation = 0.5f + rand.nextFloat() * 0.5f;
        float brightness = 0.85f + rand.nextFloat() * 0.15f;
        return Color.getHSBColor(hue, saturation, brightness);
    }


    private void ensureColor(String name, boolean isSrcFile) {
        String pkg = removeJavaExtension(name);
        if (isSrcFile) {
            fileColors.put(pkg, generateBrightColor());
            //packageColors.put(pkg, new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
        } else {
            int grayValue = 80 + rand.nextInt(120);
            fileColors.putIfAbsent(pkg, new Color(grayValue, grayValue, grayValue));
        }
    }

    private void recalculateNodePositions() {
        nodePositions.clear();

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - padding;

        int centerX = width / 2;
        int centerY = height / 2;

        int totalNodes = allNodes.size();
        int i = 0;
        for (String node : allNodes) {
            double angle = 2 * Math.PI * i / totalNodes;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            nodePositions.put(node, new Point(x, y));
            i++;
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

        // draw edges
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

        // draw nodes
        for (var entry : nodePositions.entrySet()) {
            String name = entry.getKey();
            Point p = entry.getValue();

            String pkg = removeJavaExtension(name);
            Color color = fileColors.getOrDefault(pkg, Color.BLUE);

            g2d.setColor(color);
            g2d.fillOval(p.x - 10, p.y - 10, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, p.x - 20, p.y - 15);
        }
    }
}
