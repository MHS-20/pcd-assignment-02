package async.verticles;

import async.reports.PackageDepsReport;
import async.reports.ProjectDepsReport;
import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class ProjectAnalyserVerticle extends AbstractVerticle {
    private final File projectFolder;
    private final Promise<ProjectDepsReport> resultPromise;
    Map<String, Map<String, Set<String>>> allDeps = new HashMap<>();

    public ProjectAnalyserVerticle(File projectFolder, Promise<ProjectDepsReport> resultPromise) {
        this.projectFolder = projectFolder;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        listPackageFolders(projectFolder)
                .compose(this::analyzePackages)
                .compose(this::waitAll)
                .map(this::buildReport)
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

    private Future<List<Future<?>>> analyzePackages(List<File> packageFolders) {
        List<Future<?>> futures = new ArrayList<>();
        //Map<String, Map<String, Set<String>>> allDeps = new HashMap<>();
        allDeps.clear();
        for (File packageFolder : packageFolders) {
            Promise<PackageDepsReport> packagePromise = Promise.promise();
            vertx.deployVerticle(new PackageAnalyserVerticle(packageFolder, packagePromise));
            futures.add(packagePromise.future().onSuccess(report -> {
                String relativePackageName = getRelativePath(projectFolder, packageFolder);
                allDeps.put(relativePackageName, report.getClassDependencies());
            }));
            futures.add(exploreSubPackages(packageFolder, allDeps));
        }
        return Future.succeededFuture(futures);

    }

    private Future<Void> exploreSubPackages(File packageFolder, Map<String, Map<String, Set<String>>> allDeps) {
        return listPackageFolders(packageFolder).compose(subFolders -> {
            if (subFolders.isEmpty()) {
                return Future.succeededFuture();
            }
            List<Future<?>> subFutures = new ArrayList<>();
            for (File subFolder : subFolders) {
                Promise<PackageDepsReport> subPromise = Promise.promise();
                vertx.deployVerticle(new PackageAnalyserVerticle(subFolder, subPromise));
                subFutures.add(subPromise.future().onSuccess(report -> {
                    String relativePackageName = getRelativePath(projectFolder, subFolder);
                    allDeps.put(relativePackageName, report.getClassDependencies());
                }));
            }
            return waitAll(subFutures);
        });
    }

    private Future<Void> waitAll(List<Future<?>> futures) {
        return CompositeFuture.all(new ArrayList<>(futures))
                .mapEmpty();
    }

    private ProjectDepsReport buildReport(Void unused) {
        return new ProjectDepsReport(allDeps);
    }

    private String getRelativePath(File root, File file) {
        return root.toURI().relativize(file.toURI()).getPath();
    }

}