package async;

import java.util.Map;
import java.util.Set;

public class ProjectDepsReport {
    private final Map<String, Map<String, Set<String>>> packageDependencies;

    public ProjectDepsReport(Map<String, Map<String, Set<String>>> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    public String toString() {
        return "Project Dependencies:\n" + packageDependencies;
    }
}
