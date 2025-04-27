package reactive.reports;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class FileDependencies {

    public Path filePath;
    public List<SingleDependencyResult> dependencies;

    public FileDependencies(Path filePath, List<SingleDependencyResult> dependencies) {
        this.filePath = filePath;
        this.dependencies = dependencies;
    }
}
