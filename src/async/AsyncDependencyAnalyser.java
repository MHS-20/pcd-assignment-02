package async;

import async.reports.ClassDepsReport;
import async.reports.PackageDepsReport;
import async.reports.ProjectDepsReport;
import async.verticles.ClassAnalyserVerticle;
import async.verticles.PackageAnalyserVerticle;
import async.verticles.ProjectAnalyserVerticle;
import io.vertx.core.*;

import java.io.File;

public class AsyncDependencyAnalyser {
    public static Future<ProjectDepsReport> getProjectDependencies(Vertx vertx, File projectSrcFolder) {
        Promise<ProjectDepsReport> promise = Promise.promise();
        vertx.deployVerticle(new ProjectAnalyserVerticle(projectSrcFolder, promise));
        return promise.future();
    }

    public static Future<PackageDepsReport> getPackageDependencies(Vertx vertx, File packageSrcFolder) {
        Promise<PackageDepsReport> promise = Promise.promise();
        vertx.deployVerticle(new PackageAnalyserVerticle(packageSrcFolder, promise));
        return promise.future();
    }

    public static Future<ClassDepsReport> getClassDependencies(Vertx vertx, File classSrcFile) {
        Promise<ClassDepsReport> promise = Promise.promise();
        vertx.deployVerticle(new ClassAnalyserVerticle(classSrcFile, promise));
        return promise.future();
    }
}
