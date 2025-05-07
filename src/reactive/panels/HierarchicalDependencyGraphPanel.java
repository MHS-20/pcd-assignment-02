package reactive.panels;

import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class HierarchicalDependencyGraphPanel extends JPanel implements DependencyGraphPanel {

    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Point> nodePositions = new HashMap<>();
    private final Map<String, Color> packageColors = new HashMap<>();

    private final int nodeWidth = 120;
    private final int nodeHeight = 30;
    private final int levelSpacing = 80;
    private final int nodeSpacing = 30;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public void reset() {
        edges.clear();
        nodePositions.clear();
        packageColors.clear();
        repaint();
    }

    public void addDependency(SingleDependencyResult dependency) {
        String from = removeJavaExtension(dependency.fileName);
        String to = removeJavaExtension(dependency.dependency);
        edges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
        assignColor(from, true);
        assignColor(to, false);
        layoutGraph();
        repaint();
    }

    private void assignColor(String name, boolean isSrcFile) {
        if (!packageColors.containsKey(name)) {
            if (isSrcFile) {
                float hue = new Random(name.hashCode()).nextFloat();
                float saturation = 0.3f + new Random(name.hashCode()).nextFloat() * 0.5f;
                float brightness = 0.85f + new Random(name.hashCode()).nextFloat() * 0.15f;
                packageColors.put(name, Color.getHSBColor(hue, saturation, brightness));
            } else {
                int gray = 100 + new Random(name.hashCode()).nextInt(100);
                packageColors.put(name, new Color(gray, gray, gray));
            }
        }
    }

    private void layoutGraph() {
        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> allNodes = new HashSet<>(edges.keySet());
        edges.values().forEach(allNodes::addAll);

        for (String node : allNodes) {
            inDegree.put(node, 0);
        }
        for (String from : edges.keySet()) {
            for (String to : edges.get(from)) {
                inDegree.put(to, inDegree.get(to) + 1);
            }
        }

        Map<String, Integer> levels = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
                levels.put(entry.getKey(), 0);
            }
        }

        while (!queue.isEmpty()) {
            String node = queue.poll();
            int level = levels.get(node);
            for (String neighbor : edges.getOrDefault(node, Collections.emptySet())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                    levels.put(neighbor, level + 1);
                }
            }
        }

        Map<Integer, List<String>> levelMap = new TreeMap<>();
        for (var entry : levels.entrySet()) {
            levelMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        nodePositions.clear();
        int panelWidth = getWidth() > 0 ? getWidth() : 800;

        for (var entry : levelMap.entrySet()) {
            int level = entry.getKey();
            List<String> nodes = entry.getValue();
            int totalWidth = nodes.size() * (nodeWidth + nodeSpacing) - nodeSpacing;
            int startX = (panelWidth - totalWidth) / 2;

            for (int i = 0; i < nodes.size(); i++) {
                int x = startX + i * (nodeWidth + nodeSpacing);
                int y = level * (nodeHeight + levelSpacing) + 50;
                nodePositions.put(nodes.get(i), new Point(x, y));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.LIGHT_GRAY);
        this.setPreferredSize(screenSize);
        Graphics2D g2d = (Graphics2D) g;

        // Draw edges
        g2d.setColor(Color.DARK_GRAY);
        for (var from : edges.keySet()) {
            Point fromP = nodePositions.get(from);
            for (var to : edges.get(from)) {
                Point toP = nodePositions.get(to);
                if (fromP != null && toP != null) {
                    g2d.drawLine(fromP.x + nodeWidth / 2, fromP.y + nodeHeight,
                            toP.x + nodeWidth / 2, toP.y);
                }
            }
        }

        // Draw nodes
        for (var entry : nodePositions.entrySet()) {
            String name = entry.getKey();
            Point p = entry.getValue();
            Color color = packageColors.getOrDefault(name, Color.LIGHT_GRAY);

            g2d.setColor(color);
            g2d.fillRoundRect(p.x, p.y, nodeWidth, nodeHeight, 10, 10);

            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(p.x, p.y, nodeWidth, nodeHeight, 10, 10);
            g2d.drawString(name, p.x + 5, p.y + 20);
        }
    }

    public String removeJavaExtension(String fileName) {
        if (fileName != null && fileName.endsWith(".java")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return fileName.replace("/", ".").replace("\\", ".");
    }

    public Map<String, Color> getFileColors() {
        return Collections.unmodifiableMap(packageColors);
    }
}
