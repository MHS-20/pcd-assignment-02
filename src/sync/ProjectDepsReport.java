package sync;

import java.util.Map;
import java.util.Set;

public class ProjectDepsReport {
    private final Map<String, PackageDepsReport> packageDependencies;

    public ProjectDepsReport(Map<String, PackageDepsReport> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    public Map<String, PackageDepsReport> getPackageDependencies() {
        return packageDependencies;
    }

    public Set<String> getAllUsedTypes() {
        return packageDependencies.values().stream()
                .flatMap(p -> p.getAllUsedTypes().stream())
                .collect(java.util.stream.Collectors.toSet());
    }
}
