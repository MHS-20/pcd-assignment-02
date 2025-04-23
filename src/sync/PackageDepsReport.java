package sync;

import java.util.Map;
import java.util.Set;

public class PackageDepsReport {

    private final Map<String, ClassDepsReport> classDependencies;

    public PackageDepsReport(Map<String, ClassDepsReport> classDependencies) {
        this.classDependencies = classDependencies;
    }

    public Map<String, ClassDepsReport> getClassDependencies() {
        return classDependencies;
    }

    public Set<String> getAllUsedTypes() {
        return classDependencies.values().stream()
                .flatMap(report -> report.getUsedTypes().stream())
                .collect(java.util.stream.Collectors.toSet());
    }
}
