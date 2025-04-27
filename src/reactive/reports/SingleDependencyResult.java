package reactive.reports;

import java.nio.file.Path;

public class SingleDependencyResult {
    public final Path filePath;
    public final String dependency;

    public SingleDependencyResult(Path filePath, String dependency) {
        this.filePath = filePath;
        this.dependency = dependency;
    }
}
