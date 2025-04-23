package sync;

import java.util.Set;

public class ClassDepsReport {
    private final Set<String> usedTypes;

    public ClassDepsReport(Set<String> usedTypes) {
        this.usedTypes = usedTypes;
    }

    public Set<String> getUsedTypes() {
        return usedTypes;
    }

    @Override
    public String toString() {
        return "" + usedTypes;
    }
}
