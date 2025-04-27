package reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DependencyGraphView extends JFrame {

    private static final Path DEFAULT_PATH = Path.of("src/");
    private Path selectedPath = DEFAULT_PATH;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static AtomicInteger fileCount = new AtomicInteger(0);
    public static AtomicInteger packageCount = new AtomicInteger(0);
    public static AtomicInteger dependencyCount = new AtomicInteger(0);

    private final JButton selectFolderButton = new JButton("Select Source Root");
    private final JButton startButton = new JButton("Start Analysis");
    private final JLabel selectedFolderLabel = new JLabel("Selected folder: " + DEFAULT_PATH);
    private final JLabel fileCountLabel = new JLabel("Classes/Interfaces: 0");
    private final JLabel packageCountLabel = new JLabel("Packages: 0");
    private final JLabel depCountLabel = new JLabel("Dependencies: 0");

    private final DependencyGraphPanel graphPanel = new DependencyGraphPanel();
    LegendPanel legendPanel = new LegendPanel(graphPanel.getPackageColors());
    private final PublishSubject<ActionEvent> clickStream = PublishSubject.create();

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

    private void resetCounters() {
        fileCount.set(0);
        packageCount.set(0);
        dependencyCount.set(0);
        fileCountLabel.setText("Classes/Interfaces: 0");
        packageCountLabel.setText("Packages: 0");
        depCountLabel.setText("Dependencies: 0");
    }

    private void onStartAnalysis(ActionEvent e) {
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this, "Please select a source root folder first.");
            return;
        }

        //ReactiveDependencyAnalyser.resetCounters();
        graphPanel.reset();
        resetCounters();

        ReactiveDependencyAnalyser.analyzeProject(selectedPath)
                .observeOn(Schedulers.computation())
                .doOnNext(pkg -> SwingUtilities.invokeLater(() -> {
                    packageCount.incrementAndGet();
                    packageCountLabel.setText(" Packages: " + packageCount.get());
                }))
                .flatMap(pkg -> Observable.fromIterable(pkg.fileDependencies))
                .concatMap(file -> Observable.just(file).delay(100, TimeUnit.MILLISECONDS))
                .subscribe(file -> SwingUtilities.invokeLater(() -> {
                            String fileName = file.filePath.toString();
                            graphPanel.addFileWithDependencies(fileName, file.dependencies);
                            fileCount.incrementAndGet();
                            dependencyCount.addAndGet(file.dependencies.size());
                            legendPanel.updateLegend();
                            fileCountLabel.setText(" Classes/Interfaces: " + fileCount.get());
                            depCountLabel.setText(" Dependencies: " + dependencyCount.get());
                        }), ex -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error: " + ex)),
                        () -> SwingUtilities.invokeLater(() -> {
                            //legendPanel.updateLegend();
                            JOptionPane.showMessageDialog(this, "Analysis complete.");
                        }));
    }
}