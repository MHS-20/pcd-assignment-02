package async;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        File srcFile = new File("src/async/DependencyAnalyserLib.java");
        System.out.println("Dependecies of source " + srcFile.getName() + ": ");
        ClassDepsReport report = DependencyAnalyserLib.getClassDependencies(srcFile);
        System.out.println(report);

        File packageFolder = new File("src/async/");
        System.out.println("Dependecies of package " + packageFolder.getName() + ": ");
        PackageDepsReport pkgReport = DependencyAnalyserLib.getPackageDependencies(packageFolder);
        System.out.println(pkgReport.getAllUsedTypes());

        File projectFolder = new File("src/");
        System.out.println("Dependecies of project " + projectFolder.getName() + ": ");
        ProjectDepsReport projReport = DependencyAnalyserLib.getProjectDependencies(projectFolder);
        System.out.println(projReport.getAllUsedTypes());
    }
}
