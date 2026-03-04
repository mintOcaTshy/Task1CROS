import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.TreeMap;

class FunctionLogic {
    private TreeMap<Double, Double> funcPoints = new TreeMap<>();
    private TreeMap<Double, Double> derivPoints = new TreeMap<>();

    public double eval(final String str, double xVal, double aVal) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() { nextChar(); return parseExpression(); }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm(); else if (eat('-')) x -= parseTerm(); else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor(); else if (eat('/')) x /= parseFactor(); else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor(); if (eat('-')) return -parseFactor();
                double x; int startPos = this.pos;
                if (eat('(')) { x = parseExpression(); eat(')'); }
                else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (func.equals("x")) x = xVal;
                    else if (func.equals("a")) x = aVal;
                    else {
                        x = parseFactor();
                        if (func.equals("sin")) x = Math.sin(x);
                        else if (func.equals("cos")) x = Math.cos(x);
                        else if (func.equals("exp")) x = Math.exp(x);
                    }
                } else throw new RuntimeException("Error");
                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }

    public void calculate(String formula, double a, double start, double end, double step) {
        funcPoints.clear();
        derivPoints.clear();
        double h = 0.0001;
        for (double x = start; x <= end; x += step) {
            try {
                double y = eval(formula, x, a);
                double dy = (eval(formula, x + h, a) - eval(formula, x - h, a)) / (2 * h);
                funcPoints.put(x, y);
                derivPoints.put(x, dy);
            } catch (Exception ignored) {}
        }
    }

    public TreeMap<Double, Double> getFuncPoints() { return funcPoints; }
    public TreeMap<Double, Double> getDerivPoints() { return derivPoints; }
}

public class FuncPlotter extends JFrame {
    private FunctionLogic logic = new FunctionLogic();
    private ChartPanel chartPanel;

    private JTextField formulaField = new JTextField("exp(-a*x^2)*sin(x)", 15);
    private JTextField paramA = new JTextField("0.5", 4);
    private JTextField startField = new JTextField("-5", 4);
    private JTextField endField = new JTextField("5", 4);
    private JTextField stepField = new JTextField("0.1", 4);

    public FuncPlotter() {
        setupUI();
        updatePlot();
    }

    private void setupUI() {
        setTitle("Аналізатор функцій");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controls.setBackground(new Color(240, 240, 240));
        controls.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);

        addLabeledField(controls, "f(x) =", formulaField, labelFont);
        addLabeledField(controls, "a =", paramA, labelFont);
        addLabeledField(controls, "Початок:", startField, labelFont);
        addLabeledField(controls, "Кінець:", endField, labelFont);
        addLabeledField(controls, "Крок:", stepField, labelFont);

        JButton btnPlot = new JButton("Сформувати");
        btnPlot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPlot.setBackground(new Color(220, 230, 255));
        btnPlot.addActionListener(e -> updatePlot());
        controls.add(btnPlot);

        setLayout(new BorderLayout());
        add(controls, BorderLayout.NORTH);
    }

    private void addLabeledField(JPanel panel, String label, JTextField field, Font font) {
        JLabel l = new JLabel(label);
        l.setFont(font);
        panel.add(l);
        panel.add(field);
    }

    private void updatePlot() {
        try {
            double a = Double.parseDouble(paramA.getText());
            double start = Double.parseDouble(startField.getText());
            double end = Double.parseDouble(endField.getText());
            double step = Double.parseDouble(stepField.getText());

            logic.calculate(formulaField.getText(), a, start, end, step);
            refreshGraph();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка вводу параметрів!");
        }
    }

    private void refreshGraph() {
        XYSeries s1 = new XYSeries("f(x)");
        XYSeries s2 = new XYSeries("f'(x)");

        logic.getFuncPoints().forEach(s1::add);
        logic.getDerivPoints().forEach(s2::add);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1); dataset.addSeries(s2);

        JFreeChart chart = ChartFactory.createXYLineChart("Аналіз функції", "X", "Y", dataset);

        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, new Color(220, 50, 50)); // Червона функція
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesPaint(1, new Color(50, 50, 220)); // Синя похідна
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        if (chartPanel != null) remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Додаємо внутрішні поля
        add(chartPanel, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FuncPlotter().setVisible(true));
    }
}