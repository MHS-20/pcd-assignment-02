package reactive;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        Path projectRoot = Paths.get("src/");
        System.out.println("Analyzing project...");
        ReactiveDependencyAnalyser.printProjectAnalysis(projectRoot);
    }
}
