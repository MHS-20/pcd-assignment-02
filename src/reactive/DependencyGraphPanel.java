package reactive;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class DependencyGraphPanel extends JPanel {

    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Color> packageColors = new HashMap<>();
    private final Random rand = new Random(42);

    private int colorIndex = 0;
    private final Color[] predefinedColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA,
            Color.CYAN, Color.PINK, Color.YELLOW, Color.GRAY, Color.DARK_GRAY
    };

    int padding = 150;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public void reset() {
        nodePositions.clear();
        edges.clear();
        packageColors.clear();
        colorIndex = 0;
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

        String pkg = removeJavaExtension(name);
        packageColors.computeIfAbsent(pkg, k -> new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
        //packageColors.computeIfAbsent(pkg, k -> predefinedColors[colorIndex++ % predefinedColors.length]);
    }

    public String removeJavaExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".java")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        fileName = fileName.replace("/", ".");
        fileName = fileName.replace("\\", ".");
        return fileName;
    }


    private String extractPackage(String fileName) {
        System.out.println("Extracting package from: " + fileName);
        return fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : "default";
    }

    public Map<String, Color> getPackageColors() {
        return Collections.unmodifiableMap(packageColors);
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
            String name = entry.getKey();
            Point p = entry.getValue();

            String pkg = removeJavaExtension(name);
            Color color = packageColors.getOrDefault(pkg, Color.BLUE);

            g2d.setColor(color);
            //g2d.setColor(Color.BLUE);
            g2d.fillOval(p.x - 20, p.y - 20, 25, 25);
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, p.x - 20, p.y - 25);
        }
    }
}

