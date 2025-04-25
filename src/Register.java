import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.io.IOException;
import org.mindrot.jbcrypt.BCrypt;

public class Register extends JFrame {
    private JPanel panel1;
    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton selectImageButton;
    private JLabel imageLabel;
    private File selectedImageFile;
    private JButton loginInsteadButton;

    private void styleButton(JButton button) {
        button.setBackground(UIGlobal.BUTTON_COLOR);
        button.setFont(UIGlobal.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
    }

    public Register() {
        setTitle("Cooking App Sign Up");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panel1 = new JPanel(new GridBagLayout());
        panel1.setBackground(UIGlobal.BACKGROUND_COLOR);  // Set background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Label
        JLabel title = new JLabel("Sign Up");
        title.setFont(UIGlobal.TITLE_FONT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel1.add(title, gbc);

        // Reset grid width
        gbc.gridwidth = 1;

        // Name Field
        JLabel nameLabel = new JLabel("Name:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel1.add(nameLabel, gbc);

        nameField = new JTextField(20);
        gbc.gridx = 1;
        panel1.add(nameField, gbc);

        // Username Field
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel1.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel1.add(usernameField, gbc);

        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel1.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel1.add(passwordField, gbc);

        // Image Button
        selectImageButton = new JButton("Select Profile Image");
        styleButton(selectImageButton);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel1.add(selectImageButton, gbc);

        // Image preview
        imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 200));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        gbc.gridy = 5;
        panel1.add(imageLabel, gbc);

        // Register Button
        registerButton = new JButton("Register");
        styleButton(registerButton);
        gbc.gridy = 6;
        panel1.add(registerButton, gbc);

        // Switch to login
        loginInsteadButton = new JButton("Login instead");
        styleButton(loginInsteadButton);
        gbc.gridy = 7;
        panel1.add(loginInsteadButton, gbc);

        setContentPane(panel1);
        setVisible(true);


        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = fileChooser.getSelectedFile();
                    try {
                        Image image = ImageIO.read(selectedImageFile);
                        ImageIcon icon = new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                        imageLabel.setIcon(icon);
                        imageLabel.setText("");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error loading image.");
                    }
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "All fields are required.");
                    return;
                }

                if (selectedImageFile == null) {
                    JOptionPane.showMessageDialog(null, "Please select an image.");
                    return;
                }

                try {
                    FileInputStream imageStream = new FileInputStream(selectedImageFile);  // Convert File to InputStream

                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    connect.addUser(name, username, hashedPassword, imageStream);


                    JOptionPane.showMessageDialog(null, "Registration Successful!");
                    System.out.println("Registered: " + name + " (" + username + ")");
                    System.out.println("Selected Image: " + selectedImageFile.getAbsolutePath());

                    imageStream.close();
                    new Login();
                    // Close the stream to prevent resource leaks
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading image file.");
                }
            }
        });


        loginInsteadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Login();
            }
        });
    }
}
