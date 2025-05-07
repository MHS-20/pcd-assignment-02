package reactive.panels;
import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public interface DependencyGraphPanel {
    void reset();
    void addDependency(SingleDependencyResult dependency);
    Map<String, Color> getFileColors();
}