// RecipeDetails.java – Improved UI and Image Display

// [Existing RecipeDetails class remains unchanged here]

// AddRecipe.java – Full Page for Adding New Recipes

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

    public AddRecipe(User user) {
        setTitle("Add New Recipe");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField = new JTextField(20);
        durationField = new JTextField(20);
        priceField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        instructionsArea = new JTextArea(5, 20);
        ingredientsArea = new JTextArea(5, 20);

        uploadImageButton = new JButton("Upload Image");
        saveButton = new JButton("Save Recipe");
        cancelButton = new JButton("Cancel");
        imageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        int y = 0;
        panelAdd(panel, new JLabel("Title:"), gbc, 0, y);
        panelAdd(panel, titleField, gbc, 1, y++);
        panelAdd(panel, new JLabel("Duration (min):"), gbc, 0, y);
        panelAdd(panel, durationField, gbc, 1, y++);
        panelAdd(panel, new JLabel("Price ($):"), gbc, 0, y);
        panelAdd(panel, priceField, gbc, 1, y++);
        panelAdd(panel, new JLabel("Description:"), gbc, 0, y);
        panelAdd(panel, new JScrollPane(descriptionArea), gbc, 1, y++);
        panelAdd(panel, new JLabel("Instructions:"), gbc, 0, y);
        panelAdd(panel, new JScrollPane(instructionsArea), gbc, 1, y++);
        panelAdd(panel, new JLabel("Ingredients (name:quantity per line):"), gbc, 0, y);
        panelAdd(panel, new JScrollPane(ingredientsArea), gbc, 1, y++);

        panelAdd(panel, uploadImageButton, gbc, 0, y);
        panelAdd(panel, imageLabel, gbc, 1, y++);
        panelAdd(panel, saveButton, gbc, 0, y);
        panelAdd(panel, cancelButton, gbc, 1, y++);

        add(panel);

        uploadImageButton.addActionListener(e -> selectImage());
        cancelButton.addActionListener(e -> Navigator.goBack(this));
        saveButton.addActionListener(e -> saveRecipe(user));

        setVisible(true);
    }


    private void panelAdd(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImageFile.getAbsolutePath()).getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        }
    }

    private void saveRecipe(User user) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO recipes (user_id, title, description, instructions, duration, price, image) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, getUserId(user.getFirst_name()));
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
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding recipe.");
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
                PreparedStatement insert = conn.prepareStatement("INSERT INTO ingredients (name, quantity) VALUES (?, ?) ", Statement.RETURN_GENERATED_KEYS);
                insert.setString(1, name);
                insert.setString(2, "0");
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

    private int getUserId(String username) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE name = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        }
    }
}
