package async.verticles;

import async.reports.ClassDepsReport;
import async.reports.PackageDepsReport;
import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class PackageAnalyserVerticle extends AbstractVerticle {
    private final File packageFolder;
    private final Promise<PackageDepsReport> resultPromise;
    Map<String, Set<String>> classDeps = new HashMap<>();

    public PackageAnalyserVerticle(File packageFolder, Promise<PackageDepsReport> resultPromise) {
        this.packageFolder = packageFolder;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        listJavaFiles(packageFolder)
                .compose(this::analyzeClasses)
                //.compose(this::waitAll)
                .map(this::buildReport)
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

    // Future<List<Future<ClassDepsReport>>>
    private Future<Void> analyzeClasses(List<File> javaFiles) {
        List<Future<ClassDepsReport>> futures = new ArrayList<>();
        classDeps.clear();
        for (File javaFile : javaFiles) {
            Promise<ClassDepsReport> classPromise = Promise.promise();
            vertx.deployVerticle(new ClassAnalyserVerticle(javaFile, classPromise));
            futures.add(classPromise.future().onSuccess(report ->
                    classDeps.put(report.getClassName(), report.getUsedTypes())));
        }
        //return Future.succeededFuture(futures);
        return waitAll(futures);
    }
    
    private Future<Void> waitAll(List<Future<ClassDepsReport>> futures) {
        return Future.all(futures)
                .mapEmpty();
    }

    private PackageDepsReport buildReport(Void unused) {
        return new PackageDepsReport(packageFolder.getName(), classDeps);
    }
}