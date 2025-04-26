package async.reports;

import java.util.Map;
import java.util.Set;

public class ProjectDepsReport {
    private final Map<String, Map<String, Set<String>>> packageDependencies;

    public ProjectDepsReport(Map<String, Map<String, Set<String>>> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    public Map<String, Map<String, Set<String>>> getPackageDependencies() {
        return packageDependencies;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, Set<String>>> outerEntry : packageDependencies.entrySet()) {
            String outerKey = outerEntry.getKey();
            sb.append("Package: ").append(outerKey).append("\n");

            Map<String, Set<String>> innerMap = outerEntry.getValue();
            for (Map.Entry<String, Set<String>> innerEntry : innerMap.entrySet()) {
                sb.append("\t" + innerEntry.getKey())
                        .append(" -> ")
                        .append(innerEntry.getValue())
                        .append("\n");
            }
        }
        return "Project Dependencies:\n" + sb;
    }
}
