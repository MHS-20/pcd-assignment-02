package async;

import io.vertx.core.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class ClassAnalyserVerticle extends AbstractVerticle {
    private final File classFile;
    private final Promise<Void> doneSignal;

    public ClassAnalyserVerticle(File classFile, Promise<Void> doneSignal) {
        this.classFile = classFile;
        this.doneSignal = doneSignal;
    }

    @Override
    public void start() {
        vertx.executeBlocking(promise -> {
            try {
                CompilationUnit cu = StaticJavaParser.parse(classFile);
                Set<String> types = new HashSet<>();
                cu.findAll(ClassOrInterfaceType.class)
                        .forEach(type -> types.add(type.getNameAsString()));
                System.out.println("Class " + classFile.getName() + " uses types: " + types);
                promise.complete();
            } catch (FileNotFoundException e) {
                promise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                doneSignal.complete();
            } else {
                doneSignal.fail(res.cause());
            }
        });
    }
}