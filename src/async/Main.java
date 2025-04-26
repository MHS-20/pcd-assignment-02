package async;

import io.vertx.core.Vertx;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        File projectDir = new File("src/");
        File packageDir = new File("src/sync/");
        File classFile = new File("src/example/ExampleClass.java");

        AsyncDependencyAnalyser.getClassDependencies(vertx, classFile).onComplete(cls -> {
            if (cls.succeeded()) {
                System.out.println("\n--- Class Report ---\n" + cls.result());
            } else {
                System.out.println("\n--- Class Report Error ---\n" + cls.cause());
            }
        });

        AsyncDependencyAnalyser.getPackageDependencies(vertx, packageDir).onComplete(pkg -> {
            if (pkg.succeeded()) {
                System.out.println("\n--- Package Report ---\n" + pkg.result());
            } else {
                System.out.println("\n--- Package Report Error ---\n" + pkg.cause());
            }
        });

        AsyncDependencyAnalyser.getProjectDependencies(vertx, projectDir).onComplete(project -> {
            if (project.succeeded()) {
                System.out.println("\n--- Project Report ---\n" + project.result());
            } else {
                System.out.println("\n--- Project Report Error ---\n" + project.cause());
            }
            vertx.close();
        });

        System.out.println("Waiting...");

    }
}
