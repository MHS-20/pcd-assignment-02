package async.verticles;

import async.reports.ClassDepsReport;
import async.reports.PackageDepsReport;
import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class PackageAnalyserVerticle extends AbstractVerticle {
    private final File packageFolder;
    private final Promise<PackageDepsReport> resultPromise;

    public PackageAnalyserVerticle(File packageFolder, Promise<PackageDepsReport> resultPromise) {
        this.packageFolder = packageFolder;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        listJavaFiles(packageFolder)
                .compose(this::analyzeFiles)
                .onSuccess(resultPromise::complete)
                .onFailure(resultPromise::fail);
    }

    private Future<List<File>> listJavaFiles(File folder) {
        Promise<List<File>> promise = Promise.promise();
        vertx.fileSystem().readDir(folder.getAbsolutePath(), ar -> {
            if (ar.succeeded()) {
                List<File> javaFiles = ar.result().stream()
                        .filter(path -> path.endsWith(".java"))
                        .map(File::new)
                        .toList();
                promise.complete(javaFiles);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    private Future<PackageDepsReport> analyzeFiles(List<File> javaFiles) {
        List<Future> futures = new ArrayList<>();
        Map<String, Set<String>> classDeps = new HashMap<>();

        for (File javaFile : javaFiles) {
            Promise<ClassDepsReport> classPromise = Promise.promise();
            vertx.deployVerticle(new ClassAnalyserVerticle(javaFile, classPromise));
            futures.add(classPromise.future().onSuccess(report -> {
                classDeps.put(report.getClassName(), report.getUsedTypes());
            }));
        }

        Promise<PackageDepsReport> result = Promise.promise();
        CompositeFuture.all(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                result.complete(new PackageDepsReport(packageFolder.getName(), classDeps));
            } else {
                result.fail(ar.cause());
            }
        });
        return result.future();
    }


}
