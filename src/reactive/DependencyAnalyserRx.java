package reactive;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.reactivex.rxjava3.core.Observable;
import reactive.reports.FileDependencies;
import reactive.reports.PackageDependencies;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DependencyAnalyserRx {

    public static Observable<FileDependencies> analyzeFile(Path filePath) {
        return Observable.fromCallable(() ->
                new FileDependencies(filePath, extractDependencies(filePath)));
        //.subscribeOn(Schedulers.io()); // Different thread for each file
    }

    public static Observable<PackageDependencies> analyzePackage(Path packagePath) {
        try (Stream<Path> files = Files.walk(packagePath)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
//            System.out.println("Analyzing package: " + packagePath);
//            System.out.println("Java files: " + javaFiles);
            return Observable.fromIterable(javaFiles)
                    .flatMap(DependencyAnalyserRx::analyzeFile)
                    .toList()
                    .map(fileResults -> new PackageDependencies(packagePath, fileResults))
                    .toObservable();
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static Observable<PackageDependencies> analyzeProject(Path rootProjectPath) {
        try (Stream<Path> packages = Files.list(rootProjectPath)) {
            List<Path> packageDirs = packages
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());

            return Observable.fromIterable(packageDirs)
                    .flatMap(DependencyAnalyserRx::analyzePackage); // uno stream per package
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static void printProjectAnalysis(Path rootProjectPath) {
        analyzeProject(rootProjectPath)
                .subscribe(
                        pkgResult -> {
                            System.out.println("Package done: " + pkgResult.packagePath);
                            pkgResult.fileDependencies.forEach(fileResult -> {
                                System.out.println("  File: " + fileResult.filePath);
                                System.out.println("    Dependencies: " + fileResult.dependencies);
                            });
                        },
                        throwable -> System.err.println("Error: " + throwable),
                        () -> System.out.println("Project Analysis Completed")
                );
    }


    public static Set<String> extractDependencies(Path filePath) {
        Set<String> dependencies = new HashSet<>();
        try (FileInputStream in = new FileInputStream(filePath.toFile())) {
            CompilationUnit cu = StaticJavaParser.parse(in);
            cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                String name = type.getNameAsString();
                dependencies.add(name);
                // System.out.println("Dependencies: " + dependencies);
            });
            cu.getImports().forEach(importDecl -> {
                dependencies.add(importDecl.getNameAsString());
            });
//            System.out.println("Dependencies: " + dependencies);
        } catch (Exception e) {
            System.err.println("Error while parsing: " + filePath + " -> " + e.getMessage());
        }
        return dependencies;
    }
}
