package async.verticles;

import async.reports.PackageDepsReport;
import async.reports.ProjectDepsReport;
import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class ProjectAnalyserVerticle extends AbstractVerticle {
    private final File projectFolder;
    private final Promise<ProjectDepsReport> resultPromise;
    Map<String, Map<String, Set<String>>> allDep = new HashMap<>();

    public ProjectAnalyserVerticle(File projectFolder, Promise<ProjectDepsReport> resultPromise) {
        this.projectFolder = projectFolder;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        analyzeAndExplore(projectFolder)
                //.compose(this::waitAll)
                .map(this::buildReport)
                .onSuccess(resultPromise::complete)
                .onFailure(resultPromise::fail);
    }

    // private Future<List<Future<?>>>
    private Future<Void> analyzeAndExplore(File folder) {
        List<Future<?>> subTasks = new ArrayList<>();
        return analyzeCurrentPackage(folder)
                .compose(v -> listSubPackages(folder))
                .compose(subfolders -> {
                    for (File subfolder : subfolders) {
                        subTasks.add(analyzeAndExplore(subfolder));
                    }
                    return waitAll(subTasks);
                    //return Future.succeededFuture(subTasks);
                });
    }

    private Future<List<File>> listSubPackages(File folder) {
        Promise<List<File>> promise = Promise.promise();
        vertx.fileSystem().readDir(folder.getAbsolutePath(), ar -> {
            if (ar.succeeded()) {
                List<File> subfolders = ar.result().stream()
                        .map(File::new)
                        .filter(File::isDirectory)
                        .toList();
                promise.complete(subfolders);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    private Future<Void> analyzeCurrentPackage(File folder) {
        Promise<Void> promise = Promise.promise();
        Promise<PackageDepsReport> packagePromise = Promise.promise();
        vertx.deployVerticle(new PackageAnalyserVerticle(folder, packagePromise));
        packagePromise.future()
                .onSuccess(report -> {
                    String relativePackageName = getRelativePath(projectFolder, folder);
                    allDep.put(relativePackageName, report.getClassDependencies());
                    promise.complete();
                })
                .onFailure(promise::fail);
        return promise.future();
    }

    private Future<Void> waitAll(List<Future<?>> futures) {
        return Future.all(futures)
                .mapEmpty();
    }

    private ProjectDepsReport buildReport(Void unused) {
        return new ProjectDepsReport(allDep);
    }

    private String getRelativePath(File root, File file) {
        return root.toURI().relativize(file.toURI()).getPath();
    }

}