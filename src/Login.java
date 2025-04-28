import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerInsteadButton;
    private JPanel panel;

    public Login() {
        setTitle("Cooking App - Login");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        // Load Logo
        JLabel logoLabel = new JLabel(UIGlobal.LOGO_ICON);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(logoLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome to Cooking App!");
        titleLabel.setFont(UIGlobal.TITLE_FONT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);


        loginButton = createStyledButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 10, 0); // Add background space above
        panel.add(loginButton, gbc);
        registerInsteadButton = createStyledButton("Register Instead");


        gbc.gridy = 4;
        panel.add(registerInsteadButton, gbc);

        add(panel);
        setVisible(true);

        loginButton.addActionListener(this::loginAction);
        registerInsteadButton.addActionListener(e -> {
            dispose();
            new Register();
        });
    }

    private void loginAction(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = connect.Login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome " + user.getFirst_name() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new Welcome(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(UIGlobal.BUTTON_COLOR);
        button.setFont(UIGlobal.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        return button;
    }
}

