package async;

import io.vertx.core.*;

import java.io.File;

public class DependencyAnalyserLib {

    public static void deployAnalysis(Vertx vertx, File projectSrcFolder) {
        vertx.deployVerticle(new ProjectAnalyserVerticle(projectSrcFolder));
    }

    public static void deployPackageAnalysis(Vertx vertx, File packageSrcFolder) {
        Promise<Void> dummy = Promise.promise();
        vertx.deployVerticle(new PackageAnalyserVerticle(packageSrcFolder, dummy));
    }

    public static void deployClassAnalysis(Vertx vertx, File classSrcFile) {
        Promise<Void> dummy = Promise.promise();
        vertx.deployVerticle(new ClassAnalyserVerticle(classSrcFile, dummy));
    }
}
