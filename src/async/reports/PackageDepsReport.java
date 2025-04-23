package async.reports;

import java.util.Map;
import java.util.Set;

public class PackageDepsReport {
    private final String packageName;
    private final Map<String, Set<String>> classDependencies;

    public PackageDepsReport(String packageName, Map<String, Set<String>> classDependencies) {
        this.packageName = packageName;
        this.classDependencies = classDependencies;
    }

    public Map<String, Set<String>> getClassDependencies() {
        return classDependencies;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
            sb.append("\t" + entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return "Package: " + packageName + "\nClass Deps: \n" + sb;
    }
}
