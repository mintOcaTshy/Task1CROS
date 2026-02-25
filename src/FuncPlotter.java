import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;

public class FuncPlotter extends JFrame {
    private ChartPanel chartPanel;
    private JTextField formulaField = new JTextField("exp(x)*sin(x)", 15);
    private JButton loadButton = new JButton("Відкрити файл");

    public FuncPlotter() {
        setTitle("Аналіз функцій - Step 3 (Refactored)");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("f(x) = "));
        controlPanel.add(formulaField);
        controlPanel.add(loadButton);

        chartPanel = createChartPanel();

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private ChartPanel createChartPanel() {
        XYSeries seriesF = new XYSeries("f(x)");
        XYSeries seriesD = new XYSeries("f'(x)");

        for (double x = -Math.PI; x <= Math.PI; x += 0.1) {
            double y = Math.exp(x) * Math.sin(x);
            double dy = Math.exp(x) * (Math.sin(x) + Math.cos(x)); // Похідна
            seriesF.add(x, y);
            seriesD.add(x, dy);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesF);
        dataset.addSeries(seriesD);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Графік функції та її похідної", "Вісь X", "Вісь Y",
                dataset
        );

        return new ChartPanel(chart);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FuncPlotter().setVisible(true));
    }
}