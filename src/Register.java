import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;
import javax.imageio.ImageIO;

public class Register extends JFrame {
    private JTextField nameField, usernameField;
    private JPasswordField passwordField;
    private JButton registerButton, selectImageButton, loginInsteadButton;
    private JLabel imageLabel;
    private File selectedImageFile;

    public Register() {
        setTitle("Cooking App - Register");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Your Account");
        title.setFont(UIGlobal.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Select Image Button
        selectImageButton = createStyledButton("Select Profile Image");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(selectImageButton, gbc);

        // Image Preview Label
        gbc.gridx = 1;
        imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(150, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.add(imageLabel, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        registerButton = createStyledButton("Register");
        panel.add(registerButton, gbc);

        // Login Instead Button
        gbc.gridy = 6;
        loginInsteadButton = createStyledButton("Already have an account?");
        panel.add(loginInsteadButton, gbc);

        add(panel);
        setVisible(true);

        // Action Listeners
        selectImageButton.addActionListener(this::chooseImage);
        registerButton.addActionListener(this::registerAction);
        loginInsteadButton.addActionListener(e -> {
            dispose();
            new Login();
        });
    }

    private void chooseImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            try {
                Image image = ImageIO.read(selectedImageFile);
                if (image != null) {
                    Image scaled = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Error loading image.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading image file.");
            }
        }
    }

    private void registerAction(ActionEvent e) {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (name.isBlank() || username.isBlank() || password.isBlank() || selectedImageFile == null) {
            JOptionPane.showMessageDialog(this, "All fields must be filled, and image selected!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (FileInputStream imgStream = new FileInputStream(selectedImageFile)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            connect.addUser(name, username, hashedPassword, imgStream);
            JOptionPane.showMessageDialog(this, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            new Login();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading image file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(UIGlobal.BUTTON_COLOR);
        button.setFont(UIGlobal.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UIGlobal.BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIGlobal.BUTTON_COLOR);
            }
        });

        return button;
    }
}
