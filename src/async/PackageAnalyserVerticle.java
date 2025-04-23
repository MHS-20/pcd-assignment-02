package async;

import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class PackageAnalyserVerticle extends AbstractVerticle {
    private final File packageFolder;
    private final Promise<Void> doneSignal;

    public PackageAnalyserVerticle(File packageFolder, Promise<Void> doneSignal) {
        this.packageFolder = packageFolder;
        this.doneSignal = doneSignal;
    }

    @Override
    public void start() {
        File[] javaFiles = packageFolder.listFiles((dir, name) -> name.endsWith(".java"));
        if (javaFiles == null) {
            doneSignal.complete();
            return;
        }

        List<Future> classFutures = new ArrayList<>();

        for (File file : javaFiles) {
            Promise<Void> classPromise = Promise.promise();
            classFutures.add(classPromise.future());
            vertx.deployVerticle(new ClassAnalyserVerticle(file, classPromise));
        }

        CompositeFuture.all(classFutures).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Package analysed: " + packageFolder.getName());
                doneSignal.complete();
            } else {
                ar.cause().printStackTrace();
                doneSignal.fail(ar.cause());
            }
        });
    }
}
