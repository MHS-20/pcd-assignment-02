package reactive.old;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.reactivex.rxjava3.core.Observable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ReactiveDependencyAnalyser {

    public static Observable<String> getClassDependencies(File classSrcFile) {
        return Observable.create(emitter -> {
            try (FileInputStream in = new FileInputStream(classSrcFile)) {
                CompilationUnit cu = StaticJavaParser.parse(in);
                Set<String> dependencies = new HashSet<>();

                cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                    String name = type.getNameAsString();
                    dependencies.add(name);
                });

                dependencies.forEach(emitter::onNext);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    public static Observable<String> getPackageDependencies(File packageSrcFolder) {
        return Observable.create(emitter -> {
            try {
                Set<String> dependencies = new HashSet<>();
                Files.walk(packageSrcFolder.toPath())
                        .filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toFile)
                        .forEach(file -> getClassDependencies(file).blockingForEach(dependencies::add));
                dependencies.forEach(emitter::onNext);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    public static Observable<String> getProjectDependencies(File projectSrcFolder) {
        return Observable.create(emitter -> {
            try {
                Set<String> dependencies = new HashSet<>();
                Files.walk(projectSrcFolder.toPath())
                        .filter(p -> p.toString().endsWith(".java"))
                        .map(Path::toFile)
                        .forEach(file -> getClassDependencies(file).blockingForEach(dependencies::add));
                dependencies.forEach(emitter::onNext);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }
}