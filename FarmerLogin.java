import javax.swing.JButton;

public class FarmerLogin extends RoleLoginFrame {
    public FarmerLogin() {
        super("Farmer Login", "Farmer Access", "farmer", "farmer123",
                new java.awt.Color(28, 99, 73), "farmer");
    }

    protected void onSuccessfulLogin(String username) {
        dispose();
        new Dashboard(username).setVisible(true);
    }

    protected void customizeLoginButton(JButton button) {
        button.setText("Login as Farmer");
    }

    protected void addExtraActions(javax.swing.JPanel form, java.awt.GridBagConstraints c) {
        c.gridy++;
        JButton register = new JButton("Create Farmer Account");
        register.setFocusPainted(false);
        register.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));
        register.addActionListener(e -> {
            dispose();
            new FarmerRegistration().setVisible(true);
        });
        form.add(register, c);
    }
}
