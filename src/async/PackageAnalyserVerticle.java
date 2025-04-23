package async;

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
        File[] javaFiles = packageFolder.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null || javaFiles.length == 0) {
            resultPromise.complete(new PackageDepsReport(packageFolder.getName(), Collections.emptyMap()));
            return;
        }

        Map<String, Set<String>> deps = new HashMap<>();
        List<Future> futures = new ArrayList<>();

        for (File javaFile : javaFiles) {
            Promise<ClassDepsReport> classPromise = Promise.promise();
            vertx.deployVerticle(new ClassAnalyserVerticle(javaFile, classPromise));
            futures.add(classPromise.future().onSuccess(report -> {
                deps.put(report.getClassName(), report.getUsedTypes());
            }));
        }

        CompositeFuture.all(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                resultPromise.complete(new PackageDepsReport(packageFolder.getName(), deps));
            } else {
                resultPromise.fail(ar.cause());
            }
        });
    }
}
