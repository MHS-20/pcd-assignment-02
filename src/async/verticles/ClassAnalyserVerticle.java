package async.verticles;

import async.reports.ClassDepsReport;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import com.github.javaparser.StaticJavaParser;
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
                .compose(this::parseToAST)
                .compose(this::extractTypes)
                .onSuccess(resultPromise::complete)
                .onFailure(resultPromise::fail);
    }

    private Future<String> readFile(File file) {
        return vertx.fileSystem().readFile(file.toPath().toString())
                .map(Buffer::toString);
    }

    private Future<CompilationUnit> parseToAST(String content) {
        return Future.succeededFuture(StaticJavaParser.parse(content)); // Step 2: parsing
    }

    private Future<ClassDepsReport> extractTypes(CompilationUnit cu) {
        Set<String> usedTypes = new HashSet<>();
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceType type, Void arg) {
                usedTypes.add(type.getNameAsString());
                super.visit(type, arg);
            }
        }, null);

        String className = classFile.getName().replace(".java", "");
        ClassDepsReport report = new ClassDepsReport(className, usedTypes);

        return Future.succeededFuture(report);
    }
}
