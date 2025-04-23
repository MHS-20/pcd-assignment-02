package reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class DependencyGraphViewer extends JFrame {

    private final GraphPanel graphPanel;

    public DependencyGraphViewer(Path projectRoot) {
        setTitle("Dependency Graph Viewer");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graphPanel = new GraphPanel();
        add(new JScrollPane(graphPanel));

        DependencyAnalyserRx.analyzeProject(projectRoot)
                .flatMap(pkg -> Observable.fromIterable(pkg.fileDependencies))
                .concatMap(file -> Observable.just(file).delay(500, java.util.concurrent.TimeUnit.MILLISECONDS))
                .observeOn(Schedulers.io())
                .subscribe(file -> {
                    SwingUtilities.invokeLater(() -> {
                        graphPanel.addFileWithDependencies(file.filePath.getFileName().toString(), file.dependencies);
                    });
                }, Throwable::printStackTrace);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Path path = Paths.get("src/");
            DependencyGraphViewer viewer = new DependencyGraphViewer(path);
            viewer.setVisible(true);
        });
    }

    // ===== Simple Graph Panel =====
    static class GraphPanel extends JPanel {

        private final Map<String, Point> nodePositions = new HashMap<>();
        private final Map<String, Set<String>> edges = new HashMap<>();
        private final Random rand = new Random();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            Graphics2D g2d = (Graphics2D) g;

            // Edges
            g2d.setColor(Color.GRAY);
            for (var entry : edges.entrySet()) {
                Point from = nodePositions.get(entry.getKey());
                for (String toNode : entry.getValue()) {
                    Point to = nodePositions.get(toNode);
                    if (from != null && to != null) {
                        g2d.drawLine(from.x, from.y, to.x, to.y);
                    }
                }
            }

            // Nodes
            for (var entry : nodePositions.entrySet()) {
                String node = entry.getKey();
                Point p = entry.getValue();
                g2d.setColor(Color.ORANGE);
                g2d.fillOval(p.x - 20, p.y - 20, 40, 40);
                g2d.setColor(Color.BLACK);
                g2d.drawString(node, p.x - 20, p.y - 25);
            }
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
                nodePositions.put(name, new Point(rand.nextInt(900) + 50, rand.nextInt(700) + 50));
            }
        }
    }
}
