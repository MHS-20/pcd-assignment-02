package reactive.reports;

import java.util.List;
import java.nio.file.Path;

public class PackageDependencies {
    public Path packagePath;
    public List<FileDependencies> fileDependencies;

    public PackageDependencies(Path packagePath, List<FileDependencies> fileDependencies) {
        this.packagePath = packagePath;
        this.fileDependencies = fileDependencies;
    }
}
