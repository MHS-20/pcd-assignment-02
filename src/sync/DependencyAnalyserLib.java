package sync;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DependencyAnalyserLib {

    public static ClassDepsReport getClassDependencies(File classSrcFile) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(classSrcFile);
        Set<String> usedTypes = new HashSet<>();

        cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
            usedTypes.add(type.getNameAsString());
        });

        return new ClassDepsReport(usedTypes);
    }

    public static PackageDepsReport getPackageDependencies(File packageSrcFolder) throws FileNotFoundException {
        Map<String, ClassDepsReport> classDeps = new HashMap<>();

        for (File file : Objects.requireNonNull(packageSrcFolder.listFiles((dir, name) -> name.endsWith(".java")))) {
            ClassDepsReport report = getClassDependencies(file);
            classDeps.put(file.getName(), report);
        }

        return new PackageDepsReport(classDeps);
    }

    public static ProjectDepsReport getProjectDependencies(File projectSrcFolder) throws FileNotFoundException {
        Map<String, PackageDepsReport> packageDeps = new HashMap<>();

        for (File pkg : Objects.requireNonNull(projectSrcFolder.listFiles(File::isDirectory))) {
            PackageDepsReport report = getPackageDependencies(pkg);
            packageDeps.put(pkg.getName(), report);
        }

        return new ProjectDepsReport(packageDeps);
    }
}
