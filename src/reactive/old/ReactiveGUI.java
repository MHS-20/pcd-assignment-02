package reactive.old;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactiveGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReactiveGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Reactive Dependency Analyser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout());

        // Top: folder chooser and button
        JPanel topPanel = new JPanel(new FlowLayout());
        JTextField folderField = new JTextField(40);
        JButton browseButton = new JButton("Browse");
        JButton analyseButton = new JButton("Analyse");
        topPanel.add(folderField);
        topPanel.add(browseButton);
        topPanel.add(analyseButton);

        // Center: output graph panel (JGraphX component)
        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graph.getModel().endUpdate();
        JScrollPane scrollPane = new JScrollPane(graphComponent);
        scrollPane.setBorder(new TitledBorder("Dependencies Graph"));

        // Bottom: summary boxes
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JLabel classesLabel = new JLabel("Classes/Interfaces Analysed: 0");
        JLabel dependenciesLabel = new JLabel("Dependencies Found: 0");
        bottomPanel.add(classesLabel);
        bottomPanel.add(dependenciesLabel);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        analyseButton.addActionListener(e -> {
            File projectFolder = new File(folderField.getText());
            if (!projectFolder.exists() || !projectFolder.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Invalid directory selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AtomicInteger classCount = new AtomicInteger(0);
            AtomicInteger depCount = new AtomicInteger(0);

            Set<String> nodeNames = new HashSet<>();

            Observable<String> dependencyStream = Observable.create(emitter -> {
                try {
                    Files.walk(projectFolder.toPath())
                            .filter(p -> p.toString().endsWith(".java"))
                            .map(Path::toFile)
                            .forEach(file -> {
                                classCount.incrementAndGet();
                                ReactiveDependencyAnalyser.getClassDependencies(file)
                                        .subscribeOn(Schedulers.io())
                                        .blockingForEach(dep -> {
                                            depCount.incrementAndGet();
                                            emitter.onNext(dep);
                                        });
                            });
                    emitter.onComplete();
                } catch (IOException ex) {
                    emitter.onError(ex);
                }
            });

            dependencyStream
                    .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                    .subscribe(
                            dep -> {
                                // Add a new node to the graph
                                if (!nodeNames.contains(dep)) {
                                    nodeNames.add(dep);
                                    graph.getModel().beginUpdate();
                                    try {
                                        Object node = graph.insertVertex(parent, null, dep, 20, 20, 80, 30);
                                    } catch (Exception e1) {
                                        Thread.currentThread().interrupt();
                                    } finally {
                                        graph.getModel().endUpdate();
                                        Thread.sleep(10);  // Delay between renders
                                    }
                                }

                                // Update labels
                                classesLabel.setText("Classes/Interfaces Analysed: " + classCount.get());
                                dependenciesLabel.setText("Dependencies Found: " + depCount.get());
                            },
                            err -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)),
                            () -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Analysis complete.", "Done", JOptionPane.INFORMATION_MESSAGE))
                    );
        });
    }
}

