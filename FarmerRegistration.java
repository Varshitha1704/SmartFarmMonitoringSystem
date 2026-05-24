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

public class FarmerRegistration extends JFrame {
    private final JTextField fullName = new JTextField();
    private final JTextField username = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField crop = new JTextField();
    private final JTextField location = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmPassword = new JPasswordField();

    public FarmerRegistration() {
        super("Farmer Registration");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 700));
        setContentPane(buildUi());
        setSize(980, 700);
        setLocationRelativeTo(null);
    }

    private JPanel buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(239, 245, 240));

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(new Color(28, 99, 73));
        brand.setPreferredSize(new Dimension(390, 700));
        JLabel mark = new JLabel("<html><div style='font-size:38px;color:white;font-weight:bold;'>Farmer<br>Registration</div>"
                + "<div style='font-size:15px;color:#e7f0ec;margin-top:14px;'>Create a farmer account for farm monitoring access.</div></html>");
        brand.add(mark);

        JPanel formShell = new JPanel(new GridBagLayout());
        formShell.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 228, 222)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        JLabel title = new JLabel("Register Farmer");
        title.setFont(Dashboard.font(26, true));
        title.setForeground(new Color(29, 42, 57));
        form.add(title, c);

        addField(form, c, "Full Name", fullName);
        addField(form, c, "Username", username);
        addField(form, c, "Phone Number", phone);
        addField(form, c, "Primary Crop", crop);
        addField(form, c, "Farm Location", location);
        addField(form, c, "Password", password);
        addField(form, c, "Confirm Password", confirmPassword);

        c.gridy++;
        JButton register = Dashboard.button("Register Farmer");
        register.addActionListener(e -> registerFarmer());
        form.add(register, c);

        c.gridy++;
        JButton back = new JButton("Back to Farmer Login");
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        back.addActionListener(e -> {
            dispose();
            new FarmerLogin().setVisible(true);
        });
        form.add(back, c);

        formShell.add(form);
        root.add(brand, BorderLayout.WEST);
        root.add(formShell, BorderLayout.CENTER);
        return root;
    }

    private void addField(JPanel form, GridBagConstraints c, String labelText, JTextField field) {
        c.gridy++;
        JLabel label = new JLabel(labelText);
        label.setFont(Dashboard.font(13, true));
        label.setForeground(new Color(64, 79, 91));
        form.add(label, c);
        c.gridy++;
        form.add(styleField(field), c);
    }

    private JTextField styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(330, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 218, 211)),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        return field;
    }

    private void registerFarmer() {
        String name = fullName.getText().trim();
        String user = username.getText().trim();
        String mobile = phone.getText().trim();
        String cropName = crop.getText().trim();
        String farmLocation = location.getText().trim();
        String pass = new String(password.getPassword());
        String confirm = new String(confirmPassword.getPassword());

        if (name.isBlank() || user.isBlank() || mobile.isBlank() || cropName.isBlank() || farmLocation.isBlank() || pass.isBlank()) {
            showError("Please fill all farmer registration fields.");
            return;
        }
        if (user.length() < 4) {
            showError("Username must be at least 4 characters.");
            return;
        }
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Password and confirm password do not match.");
            return;
        }

        String result = DatabaseConnection.registerFarmer(user, pass, name, mobile, cropName, farmLocation);
        if (result == null) {
            JOptionPane.showMessageDialog(this, "Farmer account created successfully.", "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new FarmerLogin().setVisible(true);
        } else {
            showError(result);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Failed", JOptionPane.ERROR_MESSAGE);
    }
}
