package reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import reactive.panels.LegendPanel;
import reactive.panels.DependencyGraphPanel;
import reactive.reports.SingleDependencyResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DependencyGraphView extends JFrame {

    private static final Path DEFAULT_PATH = Path.of("src/");
    private Path selectedPath = DEFAULT_PATH;

    public static AtomicInteger fileCount = new AtomicInteger(0);
    public static AtomicInteger packageCount = new AtomicInteger(0);
    public static AtomicInteger dependencyCount = new AtomicInteger(0);

    private final JButton selectFolderButton = new JButton("Select Source Root");
    private final JButton startButton = new JButton("Start Analysis");
    private final JLabel selectedFolderLabel = new JLabel("Selected folder: " + DEFAULT_PATH);
    private final JLabel fileCountLabel = new JLabel("Classes/Interfaces: 0");
    private final JLabel packageCountLabel = new JLabel("Packages: 0");
    private final JLabel depCountLabel = new JLabel("Dependencies: 0");

    private final DependencyGraphPanel graphPanel;
    private LegendPanel legendPanel;

    private final PublishSubject<ActionEvent> startAnalysisClicks = PublishSubject.create();
    private final PublishSubject<ActionEvent> selectFolderClicks = PublishSubject.create();

    public DependencyGraphView(DependencyGraphPanel graphPanel) {
        this.graphPanel = graphPanel;
        this.legendPanel = new LegendPanel(graphPanel.getFileColors());

        setTitle("Dependency Analyzer");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(selectFolderButton);
        topPanel.add(startButton);
        topPanel.add(selectedFolderLabel);
        add(topPanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(fileCountLabel);
        statusPanel.add(packageCountLabel);
        statusPanel.add(depCountLabel);
        add(statusPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane((Component) graphPanel), new JScrollPane(legendPanel));
        splitPane.setDividerLocation(800);
        add(splitPane, BorderLayout.CENTER);

        //selectFolderButton.addActionListener(this::onSelectFolder);
        //startButton.addActionListener(this::onStartAnalysis);

        selectFolderButton.addActionListener(selectFolderClicks::onNext);
        startButton.addActionListener(startAnalysisClicks::onNext);
        setupReactiveButtons();
    }

    private void onSelectFolder(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPath = chooser.getSelectedFile().toPath();
            selectedFolderLabel.setText("Selected: " + selectedPath);
        }
    }

    private void onStartAnalysis(ActionEvent e) {
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this, "Please select a source root folder first.");
            return;
        }
        graphPanel.reset();
        resetCounters();
        startProjectAnalysis();
    }

    private void resetCounters() {
        fileCount.set(0);
        packageCount.set(0);
        dependencyCount.set(0);
        fileCountLabel.setText("Classes/Interfaces: 0");
        packageCountLabel.setText("Packages: 0");
        depCountLabel.setText("Dependencies: 0");
    }

    private void setupReactiveButtons() {
        startAnalysisClicks
                .observeOn(Schedulers.computation())
                .subscribe(e -> {
                    if (selectedPath == null) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "Please select a source root folder first."));
                        return;
                    }
                    graphPanel.reset();
                    resetCounters();
                    startProjectAnalysis();
                });

        selectFolderClicks
                .observeOn(Schedulers.computation())
                .subscribe(e -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        selectedPath = chooser.getSelectedFile().toPath();
                        selectedFolderLabel.setText("Selected: " + selectedPath);
                    }
                });
    }


    public void startProjectAnalysis() {
        ReactiveDependencyAnalyser.analyzeProject(selectedPath)
                .observeOn(Schedulers.computation())
                .concatMap(dep -> Observable.just(dep).delay(25, TimeUnit.MILLISECONDS))
                .subscribe(dep -> SwingUtilities.invokeLater(() -> updatePanel(dep)),
                        ex -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error: " + ex)),
                        () -> SwingUtilities.invokeLater(() -> {
                            //legendPanel.updateLegend();
                            JOptionPane.showMessageDialog(this, "Analysis complete.");
                        }));
    }

    public void updatePanel(SingleDependencyResult dep) {
        String fileName = selectedPath.relativize(dep.filePath).toString();
        graphPanel.addDependency(dep);
        legendPanel.updateLegend();
        dependencyCount.incrementAndGet();
        fileCountLabel.setText(" Classes/Interfaces: " + ReactiveDependencyAnalyser.fileCount.get());
        depCountLabel.setText(" Dependencies: " + dependencyCount.get());
        packageCountLabel.setText(" Packages: " + ReactiveDependencyAnalyser.packageCount.get());
    }
}