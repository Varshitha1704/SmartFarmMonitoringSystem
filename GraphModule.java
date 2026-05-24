import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class GraphModule extends JPanel {
    private final SensorModule sensorModule;
    private JPanel chartHost = new JPanel(new BorderLayout());

    public GraphModule(SensorModule sensorModule) {
        this.sensorModule = sensorModule;
        setOpaque(false);
        setLayout(new BorderLayout());
        chartHost.setOpaque(false);
        add(chartHost, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        chartHost.removeAll();
        JPanel jFree = tryJFreeChart();
        chartHost.add(jFree != null ? jFree : new FallbackChart(sensorModule.getHistory()), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel tryJFreeChart() {
        try {
            Class<?> datasetClass = Class.forName("org.jfree.data.category.DefaultCategoryDataset");
            Object dataset = datasetClass.getConstructor().newInstance();
            Method addValue = datasetClass.getMethod("addValue", Number.class, Comparable.class, Comparable.class);
            int i = 1;
            for (SensorReading r : sensorModule.getHistory()) {
                String label = String.valueOf(i++);
                addValue.invoke(dataset, r.soilMoisture, "Soil moisture", label);
                addValue.invoke(dataset, r.temperature, "Temperature", label);
                addValue.invoke(dataset, r.waterUsage, "Water usage", label);
            }
            Class<?> orientation = Class.forName("org.jfree.chart.plot.PlotOrientation");
            Object vertical = Enum.valueOf((Class<Enum>) orientation.asSubclass(Enum.class), "VERTICAL");
            Class<?> factory = Class.forName("org.jfree.chart.ChartFactory");
            Object chart = factory.getMethod("createLineChart", String.class, String.class, String.class,
                            Class.forName("org.jfree.data.category.CategoryDataset"), orientation, boolean.class, boolean.class, boolean.class)
                    .invoke(null, "Farm Analytics", "Reading", "Value", dataset, vertical, true, true, false);
            Class<?> panelClass = Class.forName("org.jfree.chart.ChartPanel");
            Constructor<?> ctor = panelClass.getConstructor(Class.forName("org.jfree.chart.JFreeChart"));
            return (JPanel) ctor.newInstance(chart);
        } catch (Exception ex) {
            return null;
        }
    }

    private static class FallbackChart extends JPanel {
        private final List<SensorReading> data;

        FallbackChart(List<SensorReading> data) {
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(720, 360));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int left = 56;
            int top = 28;
            int right = 24;
            int bottom = 48;
            g2.setColor(new Color(255, 255, 255));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(new Color(224, 230, 236));
            for (int i = 0; i <= 5; i++) {
                int y = top + (h - top - bottom) * i / 5;
                g2.drawLine(left, y, w - right, y);
            }
            g2.setColor(new Color(29, 42, 57));
            g2.drawString("Farm Analytics (JFreeChart-ready fallback)", left, 20);
            drawLine(g2, r -> r.soilMoisture, new Color(34, 139, 87), left, top, w - right, h - bottom, "Soil moisture");
            drawLine(g2, r -> r.temperature, new Color(226, 83, 58), left, top, w - right, h - bottom, "Temperature");
            drawLine(g2, r -> Math.min(100, r.waterUsage), new Color(44, 111, 187), left, top, w - right, h - bottom, "Water usage");
            legend(g2, left, h - 22);
            g2.dispose();
        }

        private void drawLine(Graphics2D g2, Metric metric, Color color, int x1, int y1, int x2, int y2, String name) {
            if (data.size() < 2) {
                g2.setColor(new Color(93, 105, 117));
                g2.drawString("Press Refresh to collect readings.", x1, (y1 + y2) / 2);
                return;
            }
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int lastX = 0;
            int lastY = 0;
            for (int i = 0; i < data.size(); i++) {
                int x = x1 + (x2 - x1) * i / Math.max(1, data.size() - 1);
                int y = y2 - (int) ((y2 - y1) * (metric.value(data.get(i)) / 100.0));
                if (i > 0) {
                    g2.drawLine(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        }

        private void legend(Graphics2D g2, int x, int y) {
            String[] names = {"Soil moisture", "Temperature", "Water usage"};
            Color[] colors = {new Color(34, 139, 87), new Color(226, 83, 58), new Color(44, 111, 187)};
            for (int i = 0; i < names.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRoundRect(x + i * 150, y - 10, 18, 8, 8, 8);
                g2.setColor(new Color(65, 76, 88));
                g2.drawString(names[i], x + 24 + i * 150, y);
            }
        }
    }

    private interface Metric {
        double value(SensorReading reading);
    }
}
