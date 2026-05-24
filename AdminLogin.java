import javax.swing.JButton;

public class AdminLogin extends RoleLoginFrame {
    public AdminLogin() {
        super("Admin Login", "Admin Control", "admin", "admin123",
                new java.awt.Color(43, 95, 145), "admin");
    }

    protected void onSuccessfulLogin(String username) {
        dispose();
        new AdminDashboard(username).setVisible(true);
    }

    protected void customizeLoginButton(JButton button) {
        button.setText("Login as Admin");
        button.setBackground(new java.awt.Color(43, 95, 145));
    }
}
