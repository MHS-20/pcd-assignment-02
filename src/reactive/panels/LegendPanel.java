package reactive.panels;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LegendPanel extends JPanel {

    private final Map<String, Color> packageColors;

    public LegendPanel(Map<String, Color> packageColors) {
        this.packageColors = packageColors;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Color Legend"));
        setBackground(Color.WHITE);
        updateLegend();
    }

    public void updateLegend() {
        removeAll();
        for (Map.Entry<String, Color> entry : packageColors.entrySet()) {
            add(createLegendItem(entry.getKey(), entry.getValue()));
        }
        revalidate();
        repaint();
    }

    private JPanel createLegendItem(String packageName, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        item.setOpaque(false);

        JLabel colorBox = new JLabel();
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel label = new JLabel(packageName);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        item.add(colorBox);
        item.add(Box.createHorizontalStrut(8));
        item.add(label);
        return item;
    }
}
