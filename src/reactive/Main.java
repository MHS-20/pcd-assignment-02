package reactive;

import reactive.panels.HierarchicalDependencyGraphPanel;
import reactive.panels.PolygonDependencyGraphPanel;
import reactive.panels.RandomDependencyGraphPanel;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // test();

        var randomGraphPanel = new RandomDependencyGraphPanel();
        var polygonGraphPanel = new PolygonDependencyGraphPanel();
        var hierarchicalGraphPanel = new HierarchicalDependencyGraphPanel();
        SwingUtilities.invokeLater(() -> new DependencyGraphView(polygonGraphPanel).setVisible(true));
    }

    public static void test() {
        Path projectRoot = Paths.get("src/");
        System.out.println("Analyzing project...");
        ReactiveDependencyAnalyser.printProjectAnalysis(projectRoot);
    }
}
