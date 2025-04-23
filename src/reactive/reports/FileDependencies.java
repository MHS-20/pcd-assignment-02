package reactive.reports;

import java.nio.file.Path;
import java.util.Set;

public class FileDependencies {

    public Path filePath;
    public Set<String> dependencies;

    public FileDependencies(Path filePath, Set<String> dependencies) {
        this.filePath = filePath;
        this.dependencies = dependencies;
    }
}
