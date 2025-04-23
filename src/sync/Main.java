package sync;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        File srcFile = new File("src/sync/DependencyAnalyserLib.java");
        System.out.println("Dependencies of source " + srcFile.getName() + ": ");
        ClassDepsReport report = DependencyAnalyserLib.getClassDependencies(srcFile);
        System.out.println(report+ "\n");

        File srcFile2 = new File("src/sync/ClassDepsReport.java");
        System.out.println("Dependencies of source " + srcFile2.getName() + ": ");
        ClassDepsReport report2 = DependencyAnalyserLib.getClassDependencies(srcFile2);
        System.out.println(report2+ "\n");

        File srcFile3 = new File("src/sync/PackageDepsReport.java");
        System.out.println("Dependencies of source " + srcFile3.getName() + ": ");
        ClassDepsReport report3 = DependencyAnalyserLib.getClassDependencies(srcFile3);
        System.out.println(report3+ "\n");

        File srcFile4 = new File("src/sync/ProjectDepsReport.java");
        System.out.println("Dependencies of source " + srcFile4.getName() + ": ");
        ClassDepsReport report4 = DependencyAnalyserLib.getClassDependencies(srcFile4);
        System.out.println(report4+ "\n");

        File srcFile5 = new File("src/example/ExampleClass.java");
        System.out.println("Dependencies of source " + srcFile5.getName() + ": ");
        ClassDepsReport report5 = DependencyAnalyserLib.getClassDependencies(srcFile5);
        System.out.println(report5+ "\n");

        File packageFolder = new File("src/sync/");
        System.out.println("Dependencies of package " + packageFolder.getName() + ": ");
        PackageDepsReport pkgReport = DependencyAnalyserLib.getPackageDependencies(packageFolder);
        System.out.println(pkgReport.getAllUsedTypes() + "\n");

        File projectFolder = new File("src/");
        System.out.println("Dependencies of project " + projectFolder.getName() + ": ");
        ProjectDepsReport projReport = DependencyAnalyserLib.getProjectDependencies(projectFolder);
        System.out.println(projReport.getAllUsedTypes() + "\n");
    }
}
