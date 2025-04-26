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
        vertx.fileSystem().readDir(packageFolder.getAbsolutePath(), ".*\\.java$", ar -> {
            if (ar.succeeded()) {
                List<String> javaFiles = ar.result();
                if (javaFiles.isEmpty()) {
                    resultPromise.complete(new PackageDepsReport(packageFolder.getName(), Collections.emptyMap()));
                    return;
                }

                Map<String, Set<String>> deps = new HashMap<>();
                List<Future> futures = new ArrayList<>();

                for (String filePath : javaFiles) {
                    Promise<ClassDepsReport> classPromise = Promise.promise();
                    vertx.deployVerticle(new ClassAnalyserVerticle(new File(filePath), classPromise));
                    futures.add(classPromise.future().onSuccess(report -> {
                        deps.put(report.getClassName(), report.getUsedTypes());
                    }));
                }

                CompositeFuture.all(futures).onComplete(all -> {
                    if (all.succeeded()) {
                        resultPromise.complete(new PackageDepsReport(packageFolder.getName(), deps));
                    } else {
                        resultPromise.fail(all.cause());
                    }
                });
            } else {
                resultPromise.fail(ar.cause());
            }
        });
    }

}
