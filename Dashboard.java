import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;

public class Dashboard extends JFrame {
    private final PumpController pumpController = new PumpController();
    private final SensorModule sensorModule = new SensorModule(pumpController);
    private final GraphModule graphModule = new GraphModule(sensorModule);
    private final WeatherModule weatherModule = new WeatherModule();
    private final DiseasePrediction diseasePrediction = new DiseasePrediction();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel tempValue = valueLabel("-- C");
    private final JLabel humidityValue = valueLabel("-- %");
    private final JLabel moistureValue = valueLabel("-- %");
    private final JLabel waterValue = valueLabel("-- %");
    private final JLabel pumpValue = valueLabel("OFF");
    private final JTextArea notifications = new JTextArea();
    private Timer refreshTimer;

    public Dashboard(String username) {
        super("Smart Farm Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1160, 760));
        setContentPane(buildUi(username));
        setSize(1160, 760);
        setLocationRelativeTo(null);
        updateSensors();
        weatherModule.fetchWeather();
        refreshTimer = new Timer(8000, e -> updateSensors());
        refreshTimer.start();
    }

    private JPanel buildUi(String username) {
        JPanel root = new FarmBackgroundPanel();
        root.setLayout(new BorderLayout());
        root.add(sidebar(username), BorderLayout.WEST);

        JPanel page = new JPanel(new BorderLayout(18, 18));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        page.add(header(username), BorderLayout.NORTH);

        content.setOpaque(false);
        content.add(sensorPage(), "dashboard");
        content.add(section("Weather Prediction", weatherModule), "weather");
        content.add(section("Graph and Analytics", graphModule), "graphs");
        content.add(section("Disease Prediction", diseasePrediction), "disease");
        page.add(content, BorderLayout.CENTER);
        root.add(page, BorderLayout.CENTER);
        return root;
    }

    private JPanel sidebar(String username) {
        JPanel side = new SidebarPanel();
        side.setLayout(new javax.swing.BoxLayout(side, javax.swing.BoxLayout.Y_AXIS));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(235, 760));
        side.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));
        JLabel brand = new JLabel("<html><b>Smart Farm</b><br><span style='font-size:11px;color:#cce8dc;'>Farmer: " + username + "</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(font(22, true));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(Box.createVerticalStrut(28));
        side.add(nav("[Home] Dashboard", "dashboard"));
        side.add(nav("[Sun] Weather", "weather"));
        side.add(nav("[Chart] Analytics", "graphs"));
        side.add(nav("[AI] Disease AI", "disease"));
        side.add(Box.createVerticalGlue());
        JButton logout = Dashboard.button("Logout");
        logout.setBackground(new Color(182, 78, 62));
        logout.setMaximumSize(new Dimension(200, 44));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> logout());
        side.add(new JSeparator());
        side.add(Box.createVerticalStrut(12));
        side.add(logout);
        side.add(Box.createVerticalStrut(12));
        JLabel foot = new JLabel("Auto alerts enabled");
        foot.setForeground(new Color(198, 221, 211));
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(foot);
        return side;
    }

    private JButton nav(String text, String target) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(JButton.LEFT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        b.setBackground(new Color(45, 82, 71));
        b.setForeground(Color.WHITE);
        b.setFont(font(15, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> cards.show(content, target));
        return b;
    }

    private JPanel sensorPage() {
        JPanel page = new JPanel(new BorderLayout(16, 16));
        page.setOpaque(false);
        page.add(farmHero(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setOpaque(false);
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(card("Temperature", tempValue, "[T]"));
        grid.add(card("Humidity", humidityValue, "[H]"));
        grid.add(card("Soil Moisture", moistureValue, "[S]"));
        grid.add(card("Water Level", waterValue, "[W]"));
        grid.add(card("Pump Status", pumpValue, "[P]"));
        JButton refresh = button("Refresh Sensors");
        refresh.addActionListener(e -> updateSensors());
        JPanel refreshCard = wrapCard("Control", refresh);
        grid.add(refreshCard);

        notifications.setEditable(false);
        notifications.setFont(font(14, false));
        notifications.setLineWrap(true);
        notifications.setWrapStyleWord(true);
        notifications.setText("Notification center ready.");

        center.add(grid, BorderLayout.NORTH);
        center.add(wrapCard("Farmer Notifications", new JScrollPane(notifications)), BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel header(String username) {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JLabel heading = new JLabel("Smart Farm Monitoring");
        heading.setFont(font(30, true));
        heading.setForeground(new Color(24, 45, 53));
        JLabel sub = new JLabel("Live farm sensors, weather intelligence, disease AI, and irrigation alerts");
        sub.setFont(font(14, false));
        sub.setForeground(new Color(87, 104, 113));
        JPanel text = new JPanel(new BorderLayout());
        text.setOpaque(false);
        text.add(heading, BorderLayout.NORTH);
        text.add(sub, BorderLayout.SOUTH);

        JLabel farmer = new JLabel("Logged in: " + username);
        farmer.setOpaque(true);
        farmer.setBackground(Color.WHITE);
        farmer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 228, 222)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        farmer.setForeground(new Color(43, 88, 67));
        farmer.setFont(font(13, true));
        header.add(text, BorderLayout.WEST);
        header.add(farmer, BorderLayout.EAST);
        return header;
    }

    private JPanel farmHero() {
        JPanel hero = new JPanel(new BorderLayout(16, 8)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint paint = new GradientPaint(0, 0, new Color(30, 112, 80), getWidth(), getHeight(), new Color(95, 145, 64));
                g2.setPaint(paint);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(255, 255, 255, 38));
                for (int x = -40; x < getWidth(); x += 70) {
                    g2.fillOval(x, getHeight() - 52, 120, 92);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel title = new JLabel("Field Health Command");
        title.setForeground(Color.WHITE);
        title.setFont(font(24, true));
        JLabel copy = new JLabel("Automated irrigation reacts to low moisture while alerts keep the farmer informed.");
        copy.setForeground(new Color(225, 242, 232));
        copy.setFont(font(14, false));
        JPanel text = new JPanel(new BorderLayout());
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(copy, BorderLayout.SOUTH);
        JLabel status = new JLabel("LIVE");
        status.setOpaque(true);
        status.setBackground(new Color(246, 214, 86));
        status.setForeground(new Color(72, 59, 16));
        status.setFont(font(13, true));
        status.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        hero.add(text, BorderLayout.WEST);
        hero.add(status, BorderLayout.EAST);
        return hero;
    }

    private JPanel section(String title, JPanel body) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(font(22, true));
        label.setForeground(new Color(35, 52, 65));
        panel.add(label, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private void updateSensors() {
        SensorReading r = sensorModule.nextReading();
        tempValue.setText(r.temperature + " C");
        humidityValue.setText(r.humidity + " %");
        moistureValue.setText(r.soilMoisture + " %");
        waterValue.setText(r.waterLevel + " %");
        pumpValue.setText(r.pumpOn ? "ON" : "OFF");
        pumpValue.setForeground(r.pumpOn ? new Color(34, 139, 87) : new Color(93, 105, 117));
        graphModule.refresh();

        if (PumpController.isLowMoisture(r.soilMoisture)) {
            alert("Warning! Soil moisture is low. Pump switched ON.");
        }
        if (r.temperature > 36) {
            alert("Warning! Temperature exceeds safe limit: " + r.temperature + " C.");
        }
        append("Reading saved at " + r.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + " | Soil " + r.soilMoisture + "% | Pump " + (r.pumpOn ? "ON" : "OFF"));
    }

    private void alert(String message) {
        append(message);
        SMSAlert.send(message);
        VoiceAlert.speak(message);
    }

    private void append(String message) {
        notifications.append("\n" + message);
        notifications.setCaretPosition(notifications.getDocument().getLength());
    }

    private void logout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        dispose();
        new Login().setVisible(true);
    }

    public static JPanel card(String title, JLabel value, String icon) {
        JPanel panel = new AccentCardPanel(accentFor(icon));
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 226, 219)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel top = new JLabel(icon + "  " + title);
        top.setFont(font(14, true));
        top.setForeground(new Color(84, 101, 111));
        panel.add(top, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    public static JPanel wrapCard(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 229, 223)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel label = new JLabel(title);
        label.setFont(font(16, true));
        label.setForeground(new Color(41, 57, 70));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    public static JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(30, true));
        label.setForeground(new Color(29, 42, 57));
        return label;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(font(14, true));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(36, 130, 88));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 12, 8, 12));
        return b;
    }

    public static Font font(int size, boolean bold) {
        return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
    }

    private static Color accentFor(String icon) {
        if ("[T]".equals(icon)) {
            return new Color(230, 111, 68);
        }
        if ("[H]".equals(icon)) {
            return new Color(44, 139, 169);
        }
        if ("[S]".equals(icon)) {
            return new Color(62, 148, 86);
        }
        if ("[W]".equals(icon)) {
            return new Color(55, 116, 191);
        }
        if ("[P]".equals(icon)) {
            return new Color(139, 92, 174);
        }
        return new Color(36, 130, 88);
    }

    private static class FarmBackgroundPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(231, 241, 235), getWidth(), getHeight(), new Color(242, 237, 221)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(46, 123, 85, 22));
            g2.fillOval(getWidth() - 260, 70, 420, 260);
            g2.setColor(new Color(217, 158, 88, 22));
            g2.fillOval(260, getHeight() - 180, 420, 240);
            g2.dispose();
        }
    }

    private static class SidebarPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, new Color(22, 53, 45), getWidth(), getHeight(), new Color(39, 91, 68)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 24));
            g2.fillOval(-80, 80, 180, 180);
            g2.fillOval(120, 520, 170, 170);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class AccentCardPanel extends JPanel {
        private final Color accent;

        AccentCardPanel(Color accent) {
            this.accent = accent;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, 7, getHeight(), 8, 8);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
            g2.fillOval(getWidth() - 70, -30, 120, 120);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
