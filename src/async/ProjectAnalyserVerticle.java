package async;

import io.vertx.core.*;

import java.io.File;
import java.util.*;

public class ProjectAnalyserVerticle extends AbstractVerticle {
    private final File projectSrcFolder;

    public ProjectAnalyserVerticle(File projectSrcFolder) {
        this.projectSrcFolder = projectSrcFolder;
    }

    @Override
    public void start() {
        File[] packageDirs = projectSrcFolder.listFiles(File::isDirectory);
        if (packageDirs == null) return;

        List<Future> packageFutures = new ArrayList<>();

        for (File pkg : packageDirs) {
            Promise<Void> promise = Promise.promise();
            packageFutures.add(promise.future());
            vertx.deployVerticle(new PackageAnalyserVerticle(pkg, promise));
        }

        CompositeFuture.all(packageFutures).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Project analysis complete.");
            } else {
                ar.cause().printStackTrace();
            }
            vertx.close();
        });
    }
}
