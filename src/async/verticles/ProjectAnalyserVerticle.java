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
        File[] packageDirs = projectFolder.listFiles(File::isDirectory);
        if (packageDirs == null || packageDirs.length == 0) {
            resultPromise.complete(new ProjectDepsReport(Collections.emptyMap()));
            return;
        }

        Map<String, Map<String, Set<String>>> allDeps = new HashMap<>();
        List<Future> futures = new ArrayList<>();

        for (File pkg : packageDirs) {
            Promise<PackageDepsReport> pkgPromise = Promise.promise();
            vertx.deployVerticle(new PackageAnalyserVerticle(pkg, pkgPromise));
            futures.add(pkgPromise.future().onSuccess(report -> {
                allDeps.put(pkg.getName(), report.getClassDependencies());
            }));
        }

        CompositeFuture.all(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                resultPromise.complete(new ProjectDepsReport(allDeps));
            } else {
                resultPromise.fail(ar.cause());
            }
        });
    }
}