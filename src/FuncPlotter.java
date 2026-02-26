import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

class FunctionLogic {
    private TreeMap<Double, Double> points = new TreeMap<>();
    private TreeSet<Double> xValues = new TreeSet<>();

// зчитування з csv
    public void loadFromCSV(File file) throws Exception {
        points.clear();
        xValues.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("[,;]");
                if (parts.length >= 2) {
                    double x = Double.parseDouble(parts[0].trim().replace(',', '.'));
                    double y = Double.parseDouble(parts[1].trim().replace(',', '.'));
                    points.put(x, y);
                    xValues.add(x);
                }
            }
        }
    }

    public void calculateAnalytical(double a, double start, double end, double step) {
        points.clear();
        xValues.clear();
        for (double x = start; x <= end; x += step) {
            double y = Math.exp(-a * x) * Math.sin(x);
            points.put(x, y);
            xValues.add(x);
        }
    }

    public TreeMap<Double, Double> getPoints() {
        return points;
    }
}

public class FuncPlotter extends JFrame {
    private FunctionLogic logic = new FunctionLogic();
    private ChartPanel chartPanel;
    private JTextField paramAField = new JTextField("1.0", 5);

    public FuncPlotter() {
        setupUI();
        logic.calculateAnalytical(1.0, -3.14, 3.14, 0.1);
        refreshGraph("f(x) = exp(-ax)*sin(x)");
    }

    private void setupUI() {
        setTitle("Аналіз функцій - Step 5: Final");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        JButton btnLoad = new JButton("Відкрити CSV");
        JButton btnCalc = new JButton("Побудувати (параметр a)");

        controlPanel.add(new JLabel("Параметр a:"));
        controlPanel.add(paramAField);
        controlPanel.add(btnCalc);
        controlPanel.add(new JLabel(" | "));
        controlPanel.add(btnLoad);

// для обробки аналітичного рохрахунку
        btnCalc.addActionListener(e -> {
            try {
                double a = Double.parseDouble(paramAField.getText());
                logic.calculateAnalytical(a, -3.14, 3.14, 0.1);
                refreshGraph("Аналітична (a=" + a + ")");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка!! Введіть числове значення a.");
            }
        });

// обробник csv файлів
        btnLoad.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    logic.loadFromCSV(jfc.getSelectedFile());
                    refreshGraph("Дані з файлу: " + jfc.getSelectedFile().getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка!! Використовуйте: x, y");
                }
            }
        });

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
    }

    private void refreshGraph(String seriesName) {
        XYSeries series = new XYSeries(seriesName);
        logic.getPoints().forEach(series::add);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Графік аналізу функцій", "Вісь X", "Вісь Y",
                new XYSeriesCollection(series)
        );

        if (chartPanel != null) remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FuncPlotter().setVisible(true));
    }
}