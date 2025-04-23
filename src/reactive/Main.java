package reactive;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        //Path projectRoot = Paths.get("src/");
        Path projectRoot = new File("src/").toPath();
        System.out.println("Analyzing project...");
        DependencyAnalyserRx.printProjectAnalysis(projectRoot);
    }

//    public static void main(String[] args) {
//        File srcFile = new File("src/example/ExampleClass.java");
//        File packageDir = new File("src/example/");
//        File projectDir = new File("src/");
//
//        ReactiveDependencyAnalyser.getClassDependencies(srcFile).subscribe(dep -> System.out.println("Class dep: " + dep));
//        ReactiveDependencyAnalyser.getPackageDependencies(packageDir).subscribe(dep -> System.out.println("Package dep: " + dep));
//        ReactiveDependencyAnalyser.getProjectDependencies(projectDir).subscribe(dep -> System.out.println("Project dep: " + dep));
//    }
}
