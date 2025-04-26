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
        vertx.fileSystem().readDir(projectFolder.getAbsolutePath(), ar -> {
            if (ar.succeeded()) {
                List<String> packageDirs = ar.result();
                if (packageDirs.isEmpty()) {
                    resultPromise.complete(new ProjectDepsReport(Collections.emptyMap()));
                    return;
                }

                Map<String, Map<String, Set<String>>> allDeps = new HashMap<>();
                List<Future> futures = new ArrayList<>();

                for (String packagePath : packageDirs) {
                    File pkg = new File(packagePath);
                    if (!pkg.isDirectory()) continue;

                    // parse java files in the package
                    Promise<PackageDepsReport> pkgPromise = Promise.promise();
                    vertx.deployVerticle(new PackageAnalyserVerticle(pkg, pkgPromise));
                    futures.add(pkgPromise.future().onSuccess(report -> {
                        allDeps.put(getRelativePath(projectFolder, pkg), report.getClassDependencies());
                    }));

                    // parse sub-packages
                    vertx.fileSystem().readDir(packagePath, ar2 -> {
                        if (ar2.succeeded()) {
                            var packageFiles = ar2.result();
                            for (String subPackagePath : packageFiles) {
                                File subPkg = new File(subPackagePath);
                                if (subPkg.isDirectory()) {
                                    //System.out.println("Sub-package found: " + subPackagePath);
                                    // parse java files in the sub-package
                                    Promise<PackageDepsReport> subPkgPromise = Promise.promise();
                                    vertx.deployVerticle(new PackageAnalyserVerticle(subPkg, subPkgPromise));
                                    futures.add(subPkgPromise.future().onSuccess(report -> {
                                        allDeps.put(getRelativePath(projectFolder, subPkg), report.getClassDependencies());
                                    }));
                                }
                            }
                        }
                    });
                }

                CompositeFuture.all(futures).onComplete(all -> {
                    if (all.succeeded()) {
                        resultPromise.complete(new ProjectDepsReport(allDeps));
                    } else {
                        resultPromise.fail(all.cause());
                    }
                });
            } else {
                resultPromise.fail(ar.cause());
            }
        });
    }

    private String getRelativePath(File root, File file) {
        return root.toURI().relativize(file.toURI()).getPath();
    }

}