import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

class FunctionProcessor {
    private TreeMap<Double, Double> functionPoints = new TreeMap<>();

    public void calculateAnalytical(double start, double end, double step) {
        functionPoints.clear();
        for (double x = start; x <= end; x += step) {
            double y = Math.exp(x) * Math.sin(x);
            functionPoints.put(x, y);
        }
    }

    public TreeMap<Double, Double> getPoints() {
        return functionPoints;
    }
}
// граф інтерфейс
public class FuncPlotter extends JFrame {
    private FunctionProcessor processor = new FunctionProcessor();
    private ChartPanel chartPanel;

    public FuncPlotter() {
        setupUI();
        updateGraph();
    }

    private void setupUI() {
        setTitle("Аналіз функцій - Step 2: Refactored");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        JButton loadCsvBtn = new JButton("Завантажити з Excel");
        topPanel.add(loadCsvBtn);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
    }

    private void updateGraph() {
        processor.calculateAnalytical(-3, 3, 0.1);

        XYSeries series = new XYSeries("f(x)");
        processor.getPoints().forEach(series::add);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Результат рефакторингу", "X", "Y",
                new XYSeriesCollection(series)
        );

        if (chartPanel != null) remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FuncPlotter().setVisible(true));
    }
}