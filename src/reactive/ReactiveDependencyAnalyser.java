package reactive;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import reactive.reports.FileDependencies;
import reactive.reports.PackageDependencies;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactiveDependencyAnalyser {

    public static AtomicInteger fileCount = new AtomicInteger(0);
    public static AtomicInteger packageCount = new AtomicInteger(0);
    public static AtomicInteger dependencyCount = new AtomicInteger(0);

    public static Observable<FileDependencies> analyzeFile(Path filePath) {
        fileCount.incrementAndGet();
        return Observable.fromCallable(() ->
                        new FileDependencies(filePath, extractDependencies(filePath)))
                .subscribeOn(Schedulers.io());
    }

    public static Observable<PackageDependencies> analyzePackage(Path packagePath) {
        packageCount.incrementAndGet();
        try (Stream<Path> files = Files.walk(packagePath)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            return Observable.fromIterable(javaFiles)
                    .flatMap(ReactiveDependencyAnalyser::analyzeFile)
                    .toList()
                    .map(fileResults -> new PackageDependencies(packagePath, fileResults))
                    .toObservable();
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static Observable<PackageDependencies> analyzeProject(Path rootProjectPath) {
        try (Stream<Path> packages = Files.walk(rootProjectPath)) {
            List<Path> packageDirs = packages
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            return Observable.fromIterable(packageDirs)
                    .flatMap(ReactiveDependencyAnalyser::analyzePackage);
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static Set<String> extractDependencies(Path filePath) {
        Set<String> dependencies = new HashSet<>();
        try (FileInputStream in = new FileInputStream(filePath.toFile())) {
            CompilationUnit cu = StaticJavaParser.parse(in);
            // cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
            cu.findAll(ImportDeclaration.class).forEach(type -> {
                String name = type.getNameAsString();
                // System.out.println("Found dependency: " + name);
                dependencies.add(name);
                dependencyCount.incrementAndGet();
            });
            cu.getImports().forEach(importDec -> dependencies.add(importDec.getNameAsString()));
        } catch (Exception e) {
            System.err.println("Error while parsing: " + filePath + " -> " + e.getMessage());
        }
        return dependencies;
    }

    public static void printProjectAnalysis(Path rootProjectPath) {
        analyzeProject(rootProjectPath)
                .subscribe(
                        pkgResult -> {
                            System.out.println("Package done: " + pkgResult.packagePath);
                            pkgResult.fileDependencies.forEach(fileResult -> {
                                System.out.println("\tFile: " + fileResult.filePath);
                                System.out.println("\t\tDependencies: " + fileResult.dependencies);
                            });
                        },
                        throwable -> System.err.println("Error: " + throwable),
                        () -> System.out.println("Project Analysis Completed")
                );
        System.out.println("Analyzed packaged: " + packageCount.get());
        System.out.println("Analyzed files: " + fileCount.get());
        System.out.println("Dependencies found: " + dependencyCount.get());
    }

    public static void resetCounters() {
        fileCount.set(0);
        packageCount.set(0);
        dependencyCount.set(0);
    }
}
