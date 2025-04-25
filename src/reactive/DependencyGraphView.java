package reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Set;

public class DependencyGraphView extends JFrame {

    private static final Path DEFAULT_PATH = Path.of("src/");

    private final JButton selectFolderButton = new JButton("Select Source Root");
    private final JButton startButton = new JButton("Start Analysis");
    private final JLabel selectedFolderLabel = new JLabel("Selected folder: " + DEFAULT_PATH);
    private final JLabel fileCountLabel = new JLabel("Classes/Interfaces: 0");
    private final JLabel packageCountLabel = new JLabel("Packages: 0");
    private final JLabel depCountLabel = new JLabel("Dependencies: 0");

    private final DependencyGraphPanel graphPanel = new DependencyGraphPanel();
    LegendPanel legendPanel = new LegendPanel(graphPanel.getPackageColors());

    private Path selectedPath = DEFAULT_PATH;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public DependencyGraphView() {
        setTitle("Dependency Analyzer");
        setSize(1200, 800);
        //setSize(screenSize.width, screenSize.height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        graphPanel.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

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

        //add(new JScrollPane(graphPanel), BorderLayout.CENTER);
        //add(new JScrollPane(legendPanel), BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(graphPanel), new JScrollPane(legendPanel));
        splitPane.setDividerLocation(800);
        add(splitPane, BorderLayout.CENTER);

        selectFolderButton.addActionListener(this::onSelectFolder);
        startButton.addActionListener(this::onStartAnalysis);
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

        ReactiveDependencyAnalyser.resetCounters();
        graphPanel.reset();

        ReactiveDependencyAnalyser.analyzeProject(selectedPath)
                .flatMap(pkg -> Observable.fromIterable(pkg.fileDependencies))
                .concatMap(file -> Observable.just(file).delay(100, java.util.concurrent.TimeUnit.MILLISECONDS))
                .observeOn(Schedulers.io())
                .subscribe(file -> SwingUtilities.invokeLater(() -> {
                    // System.out.println("Processing file: " + file.filePath);
                            String fileName = file.filePath.getFileName().toString();
                            Set<String> deps = file.dependencies;
                            graphPanel.addFileWithDependencies(fileName, deps);
                            fileCountLabel.setText(" Classes/Interfaces: " + ReactiveDependencyAnalyser.fileCount.get());
                            packageCountLabel.setText(" Packages: " + ReactiveDependencyAnalyser.packageCount.get());
                            depCountLabel.setText(" Dependencies: " + ReactiveDependencyAnalyser.dependencyCount.get());
                        }), ex -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error: " + ex)),
                        () -> SwingUtilities.invokeLater(() -> {
                            legendPanel.updateLegend();
                            JOptionPane.showMessageDialog(this, "Analysis complete.");
                        }));

        legendPanel.updateLegend();

    }
}

