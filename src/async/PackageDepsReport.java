package async;

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
        return "Package: " + packageName + "\nClass Deps: " + classDependencies;
    }
}
