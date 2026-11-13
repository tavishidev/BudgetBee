
package ui;

import services.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final UserService userService = new UserService();

    public LoginFrame() {
        setTitle("BudgetBee | Login");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("Welcome to BudgetBee");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        panel.add(title);
        panel.add(new JLabel("Email"));
        panel.add(emailField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);
    }

    private void login() {
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();

        try {
            if (email.isBlank() || password.length == 0) {
                JOptionPane.showMessageDialog(
                        this, "Enter your email and password.");
                return;
            }

            int userId = userService.login(email, new String(password));

            if (userId != -1) {
                dispose();
                DashboardFrame dashboard = new DashboardFrame(userId);
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                        this, "Invalid email or password.");
            }
        } finally {
            java.util.Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private void register() {
        JTextField nameField = new JTextField();
        JTextField emailInput = new JTextField();
        JPasswordField passwordInput = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Full name"));
        form.add(nameField);
        form.add(new JLabel("Email"));
        form.add(emailInput);
        form.add(new JLabel("Password (at least 8 characters)"));
        form.add(passwordInput);

        int choice = JOptionPane.showConfirmDialog(
                this, form, "Create BudgetBee Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        char[] password = passwordInput.getPassword();

        try {
            if (password.length < 8) {
                JOptionPane.showMessageDialog(
                        this, "Password must contain at least 8 characters.");
                return;
            }

            boolean success = userService.register(
                    nameField.getText(),
                    emailInput.getText(),
                    new String(password));

            if (success) {
                emailField.setText(emailInput.getText().trim());
                passwordField.setText("");

                JOptionPane.showMessageDialog(
                        this, "Account created! Please log in.");
            } else {
                JOptionPane.showMessageDialog(
                        this, "Registration failed. Check the details; "
                        + "the email may already be registered.");
            }
        } finally {
            java.util.Arrays.fill(password, '\0');
            passwordInput.setText("");
        }
    }
}

