package reactive;

import reactive.panels.PolygonDependencyGraphPanel;
import reactive.panels.RandomDependencyGraphPanel;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // test();

        RandomDependencyGraphPanel randomDependencyGraphPanel = new RandomDependencyGraphPanel();
        PolygonDependencyGraphPanel polygonDependencyGraphPanel = new PolygonDependencyGraphPanel();
        SwingUtilities.invokeLater(() -> new DependencyGraphView(polygonDependencyGraphPanel).setVisible(true));
    }

    public static void test() {
        Path projectRoot = Paths.get("src/");
        System.out.println("Analyzing project...");
        ReactiveDependencyAnalyser.printProjectAnalysis(projectRoot);
    }
}
