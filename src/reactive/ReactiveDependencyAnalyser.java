package reactive;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import reactive.reports.SingleDependencyResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactiveDependencyAnalyser {

    public static AtomicInteger fileCount = new AtomicInteger(0);
    public static AtomicInteger packageCount = new AtomicInteger(0);
    public static AtomicInteger dependencyCount = new AtomicInteger(0);

    public static Observable<SingleDependencyResult> analyzeProject(Path rootProjectPath) {
        try (Stream<Path> packages = Files.walk(rootProjectPath)) {
            List<Path> packageDirs = packages
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            return Observable.fromIterable(packageDirs)
                    .flatMap(ReactiveDependencyAnalyser::analyzePackage)
                    .subscribeOn(Schedulers.io());
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static Observable<SingleDependencyResult> analyzePackage(Path packagePath) {
        packageCount.incrementAndGet();
        try (Stream<Path> files = Files.list(packagePath)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            return Observable.fromIterable(javaFiles)
                    .flatMap(file -> analyzeFile(file, packagePath.toString()));
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static Observable<SingleDependencyResult> analyzeFile(Path filePath, String packageName) {
        fileCount.incrementAndGet();
        return extractDependencies(filePath)
                .doOnNext(dep -> dep.setFileName(filePath.getFileName().toString()))
                .doOnNext(dep -> dep.setPackageName(packageName))
                .subscribeOn(Schedulers.io());
    }

    public static Observable<SingleDependencyResult> extractDependencies(Path filePath) {
        return Observable.<SingleDependencyResult>create(emitter -> {
            try (FileInputStream in = new FileInputStream(filePath.toFile())) {
                JavaParser jp = new JavaParser();
                CompilationUnit cu = jp.parse(in).getResult().get();
                Set<String> dependencies = new HashSet<>();

                cu.findAll(ClassOrInterfaceType.class).forEach(decl -> {
                    String name = decl.getNameAsString();
                    dependencies.add(name);
                });

                for (String dep : dependencies) {
                    dependencyCount.incrementAndGet();
                    emitter.onNext(new SingleDependencyResult(filePath, dep));
                }
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }


    public static void printProjectAnalysis(Path rootProjectPath) {
        analyzeProject(rootProjectPath)
                .subscribe(
                        result -> {
                            System.out.println("Package: " + result.getPackageName());
                            System.out.println("\tFile: " + result.getFileName());
                            System.out.println("\t\tDependency: " + result.getDependency());
                        },
                        throwable -> System.err.println("Error: " + throwable),
                        () -> {
                            System.out.println("Project Analysis Completed");
                            System.out.println("Analyzed packages: " + packageCount.get());
                            System.out.println("Analyzed files: " + fileCount.get());
                            System.out.println("Dependencies found: " + dependencyCount.get());
                        }
                );
    }
}
