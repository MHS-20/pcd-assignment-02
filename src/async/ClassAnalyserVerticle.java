package async;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.vertx.core.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.nio.file.Files;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class ClassAnalyserVerticle extends AbstractVerticle {
    private final File classFile;
    private final Promise<ClassDepsReport> resultPromise;

    public ClassAnalyserVerticle(File classFile, Promise<ClassDepsReport> resultPromise) {
        this.classFile = classFile;
        this.resultPromise = resultPromise;
    }

    @Override
    public void start() {
        vertx.executeBlocking(promise -> {
            try {
                String content = Files.readString(classFile.toPath());
                CompilationUnit cu = StaticJavaParser.parse(content);
                Set<String> usedTypes = new HashSet<>();
                cu.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(ClassOrInterfaceType type, Void arg) {
                        usedTypes.add(type.getNameAsString());
                        super.visit(type, arg);
                    }
                }, null);

                String className = classFile.getName().replace(".java", "");
                promise.complete(new ClassDepsReport(className, usedTypes));
            } catch (Exception e) {
                promise.fail(e);
            }
        }, resultPromise);
    }
}
