package reactive;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        // test();
        SwingUtilities.invokeLater(() -> new DependencyGraphView().setVisible(true));
    }

    public static void test() {
        Path projectRoot = Paths.get("src/");
        System.out.println("Analyzing project...");
        ReactiveDependencyAnalyser.printProjectAnalysis(projectRoot);
    }
}
