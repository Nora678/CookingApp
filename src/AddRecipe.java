import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;

class AddRecipe extends JFrame {
    private JTextField titleField, durationField, priceField;
    private JTextArea descriptionArea, instructionsArea, ingredientsArea;
    private JButton uploadImageButton, saveButton, cancelButton;
    private JLabel imageLabel;
    private File selectedImageFile;
    private User user;

    public AddRecipe(User user) {

        this.user = user;
        setTitle("Add New Recipe");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        titleField = new JTextField(20);
        durationField = new JTextField(20);
        priceField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        instructionsArea = new JTextArea(5, 20);
        ingredientsArea = new JTextArea(5, 20);

        uploadImageButton = createStyledButton("Upload Image");
        saveButton = createStyledButton("Save Recipe");
        cancelButton = createStyledButton("Cancel");

        imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        addField(panel, new JLabel("Title:"), titleField, gbc, y++);
        addField(panel, new JLabel("Duration (minutes):"), durationField, gbc, y++);
        addField(panel, new JLabel("Price ($):"), priceField, gbc, y++);
        addField(panel, new JLabel("Description:"), new JScrollPane(descriptionArea), gbc, y++);
        addField(panel, new JLabel("Instructions:"), new JScrollPane(instructionsArea), gbc, y++);
        addField(panel, new JLabel("Ingredients (name:quantity per line):"), new JScrollPane(ingredientsArea), gbc, y++);

        addField(panel, uploadImageButton, imageLabel, gbc, y++);
        addField(panel, saveButton, cancelButton, gbc, y++);

        add(panel);

        uploadImageButton.addActionListener(e -> chooseImage());
        saveButton.addActionListener(e -> saveRecipe());
        cancelButton.addActionListener(e -> Navigator.goBack(this));

        setVisible(true);
    }

    private void addField(JPanel panel, Component c1, Component c2, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(c1, gbc);

        gbc.gridx = 1;
        panel.add(c2, gbc);
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

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath()).getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        }
    }

    private void saveRecipe() {
        if (titleField.getText().isBlank() || durationField.getText().isBlank() || priceField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO recipes (user_id, title, description, instructions, duration, price, image) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setInt(1, getUserId());
            stmt.setString(2, titleField.getText());
            stmt.setString(3, descriptionArea.getText());
            stmt.setString(4, instructionsArea.getText());
            stmt.setDouble(5, Double.parseDouble(durationField.getText()));
            stmt.setDouble(6, Double.parseDouble(priceField.getText()));

            if (selectedImageFile != null) {
                stmt.setBinaryStream(7, new FileInputStream(selectedImageFile));
            } else {
                stmt.setNull(7, Types.BLOB);
            }

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int recipeId = keys.getInt(1);
                saveIngredients(conn, recipeId);
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Recipe added successfully!");
            Navigator.goBack(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving recipe.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveIngredients(Connection conn, int recipeId) throws SQLException {
        String[] lines = ingredientsArea.getText().split("\\n");
        for (String line : lines) {
            if (!line.contains(":")) continue;
            String[] parts = line.split(":");
            String name = parts[0].trim();
            String qty = parts[1].trim();

            PreparedStatement check = conn.prepareStatement("SELECT ingredient_id FROM ingredients WHERE name = ?");
            check.setString(1, name);
            ResultSet rs = check.executeQuery();

            int id;
            if (rs.next()) {
                id = rs.getInt("ingredient_id");
            } else {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO ingredients (name, quantity) VALUES (?, '0')", Statement.RETURN_GENERATED_KEYS);
                insert.setString(1, name);
                insert.executeUpdate();
                ResultSet keys = insert.getGeneratedKeys();
                keys.next();
                id = keys.getInt(1);
            }

            PreparedStatement addLink = conn.prepareStatement("INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity) VALUES (?, ?, ?)");
            addLink.setInt(1, recipeId);
            addLink.setInt(2, id);
            addLink.setString(3, qty);
            addLink.executeUpdate();
        }
    }

    private int getUserId() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE name = ?")) {
            stmt.setString(1, user.getFirst_name());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}