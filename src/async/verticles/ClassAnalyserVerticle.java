package async.verticles;

import async.reports.ClassDepsReport;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
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
        readFile(classFile)
                .compose(this::parseAST)
                .compose(this::extractTypes)
                .onSuccess(resultPromise::complete)
                .onFailure(resultPromise::fail);
    }

    private Future<String> readFile(File file) {
        return vertx.fileSystem().readFile(file.toPath().toString())
                .map(Buffer::toString);
    }

    private Future<CompilationUnit> parseAST(String content) {
        return this.getVertx().executeBlocking(() -> {
            JavaParser jp = new JavaParser();
            return jp.parse(content).getResult()
                    .orElseThrow(() -> new RuntimeException("Failed to parse Java file: " + classFile.getName()));
        });
    }

    private Future<ClassDepsReport> extractTypes(CompilationUnit cu) {
        return vertx.executeBlocking(() -> {
            Set<String> usedTypes = new HashSet<>();
            cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                usedTypes.add(type.getNameAsString());
            });
            String className = classFile.getName().replace(".java", "");
            return new ClassDepsReport(className, usedTypes);
        });
    }
}