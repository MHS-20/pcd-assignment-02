package reactive;

import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

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

//    public void addFileWithDependencies(String file, List<SingleDependencyResult> deps) {
//        ensureNode(file);
//        for (SingleDependencyResult dep : deps) {
//            ensureNode(dep.dependency);
//            edges.computeIfAbsent(file, k -> new HashSet<>()).add(dep.dependency);
//        }
//        repaint();
//    }

    public void addFileWithDependencies(String file, List<SingleDependencyResult> deps) {
        ensureNode(file, true);
        for (SingleDependencyResult dep : deps) {
            ensureNode(dep.dependency, false);
            edges.computeIfAbsent(file, k -> new HashSet<>()).add(dep.dependency);
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

//    private void ensureNode(String name, boolean isMainFile) {
//        if (!nodePositions.containsKey(name)) {
//            nodePositions.put(name, new Point(
//                    padding + rand.nextInt(screenSize.width - padding * 2),
//                    padding + rand.nextInt(screenSize.height - padding * 2)
//            ));
//        }
//
//        String pkg = removeJavaExtension(name);
//        packageColors.computeIfAbsent(pkg, k -> {
//            if (isMainFile) {
//                return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); // random per i file principali
//            } else {
//                return Color.GRAY; // fisso grigio per le dipendenze
//            }
//        });
//    }

    private void ensureNode(String name, boolean isSrcFile) {
        if (!nodePositions.containsKey(name)) {
            nodePositions.put(name, new Point(
                    padding + rand.nextInt(screenSize.width - padding * 2),
                    padding + rand.nextInt(screenSize.height - padding * 2)
            ));
        }

        String pkg = removeJavaExtension(name);
        packageColors.computeIfAbsent(pkg, k -> {
            if (isSrcFile) {
                return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            } else {
                int grayValue = 80 + rand.nextInt(120); // sfumature tra 80 e 200
                return new Color(grayValue, grayValue, grayValue);
            }
        });
    }


    public String removeJavaExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".java")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return fileName
                .replace("/", ".")
                .replace("\\", ".");
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
            g2d.fillOval(p.x - 20, p.y - 20, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawString(name, p.x - 20, p.y - 25);
        }
    }
}

