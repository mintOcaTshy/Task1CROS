import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

class FunctionLogic {
    private TreeMap<Double, Double> funcPoints = new TreeMap<>();
    private TreeMap<Double, Double> derivPoints = new TreeMap<>();
    private TreeSet<Double> xValues = new TreeSet<>();

    public void loadFromCSV(File file) throws Exception {
        funcPoints.clear();
        derivPoints.clear();
        xValues.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("[,;]");
                if (parts.length >= 2) {
                    double x = Double.parseDouble(parts[0].trim().replace(',', '.'));
                    double y = Double.parseDouble(parts[1].trim().replace(',', '.'));
                    funcPoints.put(x, y);
                    xValues.add(x);
                }
            }
        }
    }

    public void calculateAnalytical(double a, double start, double end, double step) {
        funcPoints.clear();
        derivPoints.clear();
        xValues.clear();
        double h = 0.0001;

        for (double x = start; x <= end; x += step) {
            double y = Math.exp(-a * x) * Math.sin(x);

            double dy_analyt = Math.exp(-a * x) * (Math.cos(x) - a * Math.sin(x));

            funcPoints.put(x, y);
            derivPoints.put(x, dy_analyt);
            xValues.add(x);
        }
    }

    public TreeMap<Double, Double> getFuncPoints() { return funcPoints; }
    public TreeMap<Double, Double> getDerivPoints() { return derivPoints; }
}

public class FuncPlotter extends JFrame {
    private FunctionLogic logic = new FunctionLogic();
    private ChartPanel chartPanel;
    private JTextField paramAField = new JTextField("1.0", 5);

    public FuncPlotter() {
        setupUI();
        logic.calculateAnalytical(1.0, -3.14, 3.14, 0.1);
        refreshGraph();
    }

    private void setupUI() {
        setTitle("Аналіз функцій - Step 8: Final");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        JButton btnLoad = new JButton("Відкрити CSV");
        JButton btnCalc = new JButton("Побудувати (a)");

        controlPanel.add(new JLabel("Параметр a:"));
        controlPanel.add(paramAField);
        controlPanel.add(btnCalc);
        controlPanel.add(new JLabel(" | "));
        controlPanel.add(btnLoad);

        btnCalc.addActionListener(e -> {
            try {
                double a = Double.parseDouble(paramAField.getText());
                logic.calculateAnalytical(a, -3.14, 3.14, 0.1);
                refreshGraph();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Введіть числове значення!");
            }
        });

        btnLoad.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    logic.loadFromCSV(jfc.getSelectedFile());
                    refreshGraph();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка формату CSV!");
                }
            }
        });

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
    }

    private void refreshGraph() {
        XYSeries s1 = new XYSeries("Функція f(x)");
        XYSeries s2 = new XYSeries("Похідна f'(x)");

        logic.getFuncPoints().forEach(s1::add);
        logic.getDerivPoints().forEach(s2::add);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);

        JFreeChart chart = ChartFactory.createXYLineChart("Аналіз функції", "X", "Y", dataset);

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        plot.setRenderer(renderer);

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