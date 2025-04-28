import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class RecipeDetails extends JFrame {
    private JTextField titleField, durationField, priceField;
    private JTextArea descriptionArea, instructionsArea, ingredientsArea;
    private JButton saveButton, deleteButton, backButton, favouriteButton;
    private User user;
    private String originalTitle;
    private int recipeId;
    private final JFrame parentWindow;

    public RecipeDetails(User user, String title, JFrame parentWindow) {
        this.user = user;
        this.originalTitle = title;
        this.parentWindow = parentWindow;

        setTitle("Recipe Details: " + title);
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        titleField = new JTextField(20);
        durationField = new JTextField(20);
        priceField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        instructionsArea = new JTextArea(5, 20);
        ingredientsArea = new JTextArea(5, 20);

        panelAdd(mainPanel, new JLabel("Title:"), gbc, 0, y);
        panelAdd(mainPanel, titleField, gbc, 1, y++);
        panelAdd(mainPanel, new JLabel("Duration (min):"), gbc, 0, y);
        panelAdd(mainPanel, durationField, gbc, 1, y++);
        panelAdd(mainPanel, new JLabel("Price ($):"), gbc, 0, y);
        panelAdd(mainPanel, priceField, gbc, 1, y++);
        panelAdd(mainPanel, new JLabel("Description:"), gbc, 0, y);
        panelAdd(mainPanel, new JScrollPane(descriptionArea), gbc, 1, y++);
        panelAdd(mainPanel, new JLabel("Ingredients (name:quantity per line):"), gbc, 0, y);
        panelAdd(mainPanel, new JScrollPane(ingredientsArea), gbc, 1, y++);
        panelAdd(mainPanel, new JLabel("Instructions:"), gbc, 0, y);
        panelAdd(mainPanel, new JScrollPane(instructionsArea), gbc, 1, y++);

        saveButton = createStyledButton("Save Changes");
        deleteButton = createStyledButton("Delete Recipe");
        backButton = createStyledButton("Go Back");

        loadRecipeDetails(title); // also loads recipeId

        favouriteButton = createStyledButton(isInFavourites() ? "Remove from Favourites" : "Add to Favourites");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(favouriteButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        saveButton.addActionListener(this::saveChanges);
        deleteButton.addActionListener(this::deleteRecipe);
        backButton.addActionListener(e -> Navigator.goBack(this));
        favouriteButton.addActionListener(e -> toggleFavourite());

        setVisible(true);
    }

    private void panelAdd(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(comp, gbc);
    }

    private void loadRecipeDetails(String title) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recipes WHERE title = ?")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                recipeId = rs.getInt("recipe_id");
                titleField.setText(rs.getString("title"));
                durationField.setText(String.valueOf(rs.getDouble("duration")));
                priceField.setText(String.valueOf(rs.getDouble("price")));
                descriptionArea.setText(rs.getString("description"));
                instructionsArea.setText(rs.getString("instructions"));
                ingredientsArea.setText(loadIngredients(recipeId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String loadIngredients(int recipeId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT i.name, ri.quantity FROM recipe_ingredients ri
                JOIN ingredients i ON ri.ingredient_id = i.ingredient_id
                WHERE ri.recipe_id = ?
            """)) {
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("name")).append(":").append(rs.getString("quantity")).append("\n");
            }
        }
        return sb.toString();
    }

    private void saveChanges(ActionEvent e) {
        String newTitle = titleField.getText().trim();
        String newDescription = descriptionArea.getText().trim();
        String newInstructions = instructionsArea.getText().trim();
        String newDurationStr = durationField.getText().trim();
        String newPriceStr = priceField.getText().trim();

        if (newTitle.isEmpty() || newDurationStr.isEmpty() || newPriceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, Duration and Price cannot be empty.");
            return;
        }

        try {
            double duration = Double.parseDouble(newDurationStr);
            double price = Double.parseDouble(newPriceStr);

            if (duration <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Duration and Price must be positive.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
                conn.setAutoCommit(false);

                PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE recipes
                    SET title = ?, description = ?, instructions = ?, duration = ?, price = ?
                    WHERE recipe_id = ?
                """);
                stmt.setString(1, newTitle);
                stmt.setString(2, newDescription);
                stmt.setString(3, newInstructions);
                stmt.setDouble(4, duration);
                stmt.setDouble(5, price);
                stmt.setInt(6, recipeId);
                stmt.executeUpdate();

                conn.commit();

                if (parentWindow instanceof BrowseRecipes browseRecipes) {
                    browseRecipes.loadRecipes();
                } else if (parentWindow instanceof Favourites favourites) {
                    favourites.loadFavourites();
                }

                JOptionPane.showMessageDialog(this, "Recipe updated successfully!");
                Navigator.goBack(this);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error during save.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format in Duration or Price.");
        }
    }

    private void deleteRecipe(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this recipe?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM recipes WHERE recipe_id = ?")) {
            stmt.setInt(1, recipeId);
            stmt.executeUpdate();

            if (parentWindow instanceof BrowseRecipes browseRecipes) {
                browseRecipes.loadRecipes();
            } else if (parentWindow instanceof Favourites favourites) {
                favourites.loadFavourites();
            }

            JOptionPane.showMessageDialog(this, "Recipe deleted successfully.");
            Navigator.goBack(this);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting recipe.");
        }
    }

    private void toggleFavourite() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            if (isInFavourites()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM favourites WHERE user_id = ? AND recipe_id = ?");
                stmt.setInt(1, getUserId());
                stmt.setInt(2, recipeId);
                stmt.executeUpdate();
                favouriteButton.setText("Add to Favourites");
                JOptionPane.showMessageDialog(this, "Removed from Favourites");
            } else {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO favourites (user_id, recipe_id) VALUES (?, ?)");
                stmt.setInt(1, getUserId());
                stmt.setInt(2, recipeId);
                stmt.executeUpdate();
                favouriteButton.setText("Remove from Favourites");
                JOptionPane.showMessageDialog(this, "Added to Favourites");
            }

            if (parentWindow instanceof Favourites favourites) {
                favourites.loadFavourites();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isInFavourites() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM favourites WHERE user_id = ? AND recipe_id = ?")) {
            stmt.setInt(1, getUserId());
            stmt.setInt(2, recipeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getUserId() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE name = ?")) {
            stmt.setString(1, user.getFirst_name());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
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
