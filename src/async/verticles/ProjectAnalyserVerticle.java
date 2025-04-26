package async.verticles;

import async.reports.PackageDepsReport;
import async.reports.ProjectDepsReport;
import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class ProjectAnalyserVerticle extends AbstractVerticle {
    private final File projectFolder;
    private final Promise<ProjectDepsReport> resultPromise;

    public ProjectAnalyserVerticle(File projectFolder, Promise<ProjectDepsReport> resultPromise) {
        this.projectFolder = projectFolder;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        listPackageFolders(projectFolder)
                .compose(this::analyzePackages)
                .onSuccess(resultPromise::complete)
                .onFailure(resultPromise::fail);
    }

    private Future<List<File>> listPackageFolders(File rootFolder) {
        Promise<List<File>> promise = Promise.promise();
        vertx.fileSystem().readDir(rootFolder.getAbsolutePath(), ar -> {
            if (ar.succeeded()) {
                List<File> folders = ar.result().stream()
                        .map(File::new)
                        .filter(File::isDirectory)
                        .toList();
                promise.complete(folders);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    private Future<ProjectDepsReport> analyzePackages(List<File> packageFolders) {
        List<Future> futures = new ArrayList<>();
        Map<String, Map<String, Set<String>>> allDeps = new HashMap<>();

        for (File packageFolder : packageFolders) {
            Promise<PackageDepsReport> packagePromise = Promise.promise();
            vertx.deployVerticle(new PackageAnalyserVerticle(packageFolder, packagePromise));
            futures.add(packagePromise.future().onSuccess(report -> {
                String relativePackageName = getRelativePath(projectFolder, packageFolder);
                allDeps.put(relativePackageName, report.getClassDependencies());
            }));

            futures.add(exploreSubPackages(packageFolder, allDeps));
        }

        Promise<ProjectDepsReport> result = Promise.promise();
        CompositeFuture.all(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                result.complete(new ProjectDepsReport(allDeps));
            } else {
                result.fail(ar.cause());
            }
        });
        return result.future();
    }

    private Future<Void> exploreSubPackages(File packageFolder, Map<String, Map<String, Set<String>>> allDeps) {
        Promise<Void> promise = Promise.promise();
        listPackageFolders(packageFolder).onComplete(ar -> {
            if (ar.succeeded()) {
                List<File> subFolders = ar.result();

                if (subFolders.isEmpty()) {
                    promise.complete();
                    return;
                }

                List<Future> subFutures = new ArrayList<>();
                for (File subFolder : subFolders) {
                    Promise<PackageDepsReport> subPromise = Promise.promise();
                    vertx.deployVerticle(new PackageAnalyserVerticle(subFolder, subPromise));
                    subFutures.add(subPromise.future().onSuccess(report -> {
                        String relativePackageName = getRelativePath(projectFolder, subFolder);
                        allDeps.put(relativePackageName, report.getClassDependencies());
                    }));
                }

                CompositeFuture.all(subFutures).onComplete(res -> {
                    if (res.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(res.cause());
                    }
                });
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }
    
    private String getRelativePath(File root, File file) {
        return root.toURI().relativize(file.toURI()).getPath();
    }

}