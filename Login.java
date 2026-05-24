import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class Login extends JFrame {
    public Login() {
        super("Smart Farm System - Select Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setContentPane(buildUi());
        setSize(960, 620);
        setLocationRelativeTo(null);
    }

    private JPanel buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(239, 245, 240));

        JPanel hero = new JPanel(new GridBagLayout());
        hero.setBackground(new Color(28, 99, 73));
        hero.setPreferredSize(new Dimension(410, 620));
        JLabel mark = new JLabel("<html><div style='font-size:42px;color:white;font-weight:bold;'>Smart Farm</div>"
                + "<div style='font-size:17px;color:#dcefe6;margin-top:12px;'>Monitoring and Disease Prediction</div>"
                + "<div style='font-size:13px;color:#b9dccb;margin-top:28px;'>Sensors | Weather | AI | Alerts</div></html>");
        hero.add(mark);

        JPanel selectorShell = new JPanel(new GridBagLayout());
        selectorShell.setOpaque(false);
        JPanel selector = new JPanel(new GridBagLayout());
        selector.setBackground(Color.WHITE);
        selector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 228, 222)),
                BorderFactory.createEmptyBorder(34, 36, 34, 36)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JLabel title = new JLabel("Choose Login Type");
        title.setFont(Dashboard.font(28, true));
        title.setForeground(new Color(29, 42, 57));
        selector.add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("Separate access for farmers and administrators");
        subtitle.setForeground(new Color(101, 116, 130));
        selector.add(subtitle, c);

        c.gridy++;
        JButton farmer = Dashboard.button("Farmer Login");
        farmer.addActionListener(e -> openFarmerLogin());
        selector.add(farmer, c);

        c.gridy++;
        JButton admin = Dashboard.button("Admin Login");
        admin.setBackground(new Color(43, 95, 145));
        admin.addActionListener(e -> openAdminLogin());
        selector.add(admin, c);

        c.gridy++;
        JLabel hint = new JLabel("<html>Farmer: farmer / farmer123<br>Admin: admin / admin123</html>");
        hint.setForeground(new Color(101, 116, 130));
        selector.add(hint, c);

        selectorShell.add(selector);
        root.add(hero, BorderLayout.WEST);
        root.add(selectorShell, BorderLayout.CENTER);
        return root;
    }

    private void openFarmerLogin() {
        dispose();
        new FarmerLogin().setVisible(true);
    }

    private void openAdminLogin() {
        dispose();
        new AdminLogin().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
