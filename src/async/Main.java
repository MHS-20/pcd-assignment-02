package async;

import io.vertx.core.Vertx;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        File projectDir = new File("src/");
        File packageDir = new File("src/example/");
        File classFile = new File("src/example/ExampleClass.java");

        System.out.println("\n--- Class Analysis ---");
        DependencyAnalyserLib.deployClassAnalysis(vertx, classFile);

        vertx.setTimer(3000, id -> {
            System.out.println("\n--- Package Analysis ---");
            DependencyAnalyserLib.deployPackageAnalysis(vertx, packageDir);
        });

        vertx.setTimer(6000, id -> {
            System.out.println("\n--- Project Analysis ---");
            DependencyAnalyserLib.deployAnalysis(vertx, projectDir);
        });
    }
}
