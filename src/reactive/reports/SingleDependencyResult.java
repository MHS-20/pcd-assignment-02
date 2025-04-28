package reactive.reports;

import java.nio.file.Path;

public class SingleDependencyResult {
    public Path filePath;
    public String dependency;
    public String packageName;
    public String fileName;

    public SingleDependencyResult(Path filePath, String dependency) {
        this.filePath = filePath;
        this.dependency = dependency;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDependency() {
        return dependency;
    }

    public Path getFilePath() {
        return filePath;
    }


}
