import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

public class AdminDashboard extends JFrame {
    public AdminDashboard(String username) {
        super("Smart Farm Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setContentPane(buildUi(username));
        setSize(1120, 720);
        setLocationRelativeTo(null);
    }

    private JPanel buildUi(String username) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(236, 242, 239));
        root.add(sidebar(username), BorderLayout.WEST);

        JPanel page = new JPanel(new BorderLayout(18, 18));
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JLabel heading = new JLabel("Admin Control Center");
        heading.setFont(Dashboard.font(28, true));
        heading.setForeground(new Color(28, 42, 54));
        page.add(heading, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(Dashboard.card("Registered Farmers", Dashboard.valueLabel(DatabaseConnection.countUsersByRole("farmer") + ""), "[F]"));
        grid.add(Dashboard.card("Sensor Records", Dashboard.valueLabel(DatabaseConnection.countRows("sensor_readings") + ""), "[S]"));
        grid.add(Dashboard.card("Disease Predictions", Dashboard.valueLabel(DatabaseConnection.countRows("disease_predictions") + ""), "[D]"));
        grid.add(adminActions());

        JTextArea notes = new JTextArea();
        notes.setEditable(false);
        notes.setFont(Dashboard.font(14, false));
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setText("Admin usage:\n\n"
                + "- Monitor total sensor readings and disease prediction logs.\n"
                + "- Use database tools to add farmers or reset passwords.\n"
                + "- Configure OpenWeather, Twilio, MySQL, JFreeChart, and FreeTTS from project setup files.\n\n"
                + "Default admin credentials: admin / admin123");

        page.add(grid, BorderLayout.NORTH);
        page.add(Dashboard.wrapCard("System Notes", new JScrollPane(notes)), BorderLayout.CENTER);
        root.add(page, BorderLayout.CENTER);
        return root;
    }

    private JPanel sidebar(String username) {
        JPanel side = new JPanel();
        side.setLayout(new javax.swing.BoxLayout(side, javax.swing.BoxLayout.Y_AXIS));
        side.setBackground(new Color(31, 58, 86));
        side.setPreferredSize(new Dimension(235, 720));
        side.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));

        JLabel brand = new JLabel("<html><b>Admin Panel</b><br><span style='font-size:11px;color:#cbdced;'>Welcome, " + username + "</span></html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(Dashboard.font(22, true));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(Box.createVerticalStrut(28));

        JButton logout = Dashboard.button("Logout");
        logout.setBackground(new Color(67, 111, 153));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(200, 44));
        logout.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
        side.add(logout);
        side.add(Box.createVerticalGlue());
        return side;
    }

    private JPanel adminActions() {
        JPanel panel = Dashboard.wrapCard("Admin Actions", new JLabel("Database and integration controls"));
        JButton openFarmer = Dashboard.button("Open Farmer Dashboard");
        openFarmer.setBackground(new Color(67, 111, 153));
        openFarmer.addActionListener(e -> new Dashboard("admin-view").setVisible(true));
        panel.add(openFarmer, BorderLayout.SOUTH);
        return panel;
    }
}
