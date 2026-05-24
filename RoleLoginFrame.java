import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public abstract class RoleLoginFrame extends JFrame {
    private final JTextField username;
    private final JPasswordField password;
    private final String role;
    private final String demoUser;
    private final String demoPassword;

    public RoleLoginFrame(String windowTitle, String pageTitle, String demoUser, String demoPassword,
                          Color brandColor, String role) {
        super(windowTitle);
        this.demoUser = demoUser;
        this.demoPassword = demoPassword;
        this.role = role;
        this.username = new JTextField(demoUser);
        this.password = new JPasswordField(demoPassword);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setContentPane(buildUi(pageTitle, brandColor));
        setSize(960, 620);
        setLocationRelativeTo(null);
    }

    private JPanel buildUi(String pageTitle, Color brandColor) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(239, 245, 240));

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(brandColor);
        brand.setPreferredSize(new Dimension(410, 620));
        JLabel mark = new JLabel("<html><div style='font-size:40px;color:white;font-weight:bold;'>" + pageTitle + "</div>"
                + "<div style='font-size:16px;color:#e7f0ec;margin-top:12px;'>Smart Farm Monitoring System</div>"
                + "<div style='font-size:13px;color:#d5e5de;margin-top:28px;'>Secure role-based login</div></html>");
        brand.add(mark);

        JPanel formShell = new JPanel(new GridBagLayout());
        formShell.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 228, 222)),
                BorderFactory.createEmptyBorder(34, 36, 34, 36)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JLabel title = new JLabel(pageTitle);
        title.setFont(Dashboard.font(26, true));
        form.add(title, c);

        c.gridy++;
        form.add(label("Username"), c);
        c.gridy++;
        form.add(field(username), c);
        c.gridy++;
        form.add(label("Password"), c);
        c.gridy++;
        form.add(field(password), c);
        c.gridy++;

        JButton login = Dashboard.button("Login");
        customizeLoginButton(login);
        login.addActionListener(e -> doLogin());
        form.add(login, c);

        addExtraActions(form, c);

        c.gridy++;
        JButton back = new JButton("Back to Login Type");
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        back.addActionListener(e -> backToSelector());
        form.add(back, c);

        c.gridy++;
        JLabel hint = new JLabel("Demo credentials: " + demoUser + " / " + demoPassword);
        hint.setForeground(new Color(101, 116, 130));
        form.add(hint, c);

        formShell.add(form);
        root.add(brand, BorderLayout.WEST);
        root.add(formShell, BorderLayout.CENTER);
        return root;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Dashboard.font(13, true));
        label.setForeground(new Color(64, 79, 91));
        return label;
    }

    private JTextField field(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        f.setPreferredSize(new Dimension(310, 42));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 218, 211)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return f;
    }

    private void doLogin() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        if (DatabaseConnection.validateLogin(user, pass, role)) {
            onSuccessfulLogin(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid " + role + " username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToSelector() {
        dispose();
        new Login().setVisible(true);
    }

    protected void customizeLoginButton(JButton button) {
    }

    protected void addExtraActions(JPanel form, GridBagConstraints c) {
    }

    protected abstract void onSuccessfulLogin(String username);
}
