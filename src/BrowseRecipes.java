// Inventory.java already provided above
// Below: BrowseRecipes.java, RecipeDetails.java, Favourites.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class BrowseRecipes extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sortBox;
    private JTextField searchField;
    private JButton searchButton, addButton, deleteButton, backButton;
    private JButton logoutButton;
    private User user;

    public BrowseRecipes(User user) {
        this.user = user;
        setTitle("Browse Recipes");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);
        logoutButton = new JButton("Logout");
        logoutButton.setFont(UIGlobal.BUTTON_FONT);
        logoutButton.setBackground(UIGlobal.BUTTON_COLOR);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            dispose();
            new Login();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        sortBox = new JComboBox<>(new String[]{"Sort by Title", "Sort by Duration", "Sort by Price"});
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(sortBox);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Title", "Duration", "Price"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        addButton = new JButton("Add Recipe");
        deleteButton = new JButton("Delete Recipe");
        backButton = new JButton("Go Back");

        for (JButton btn : new JButton[]{addButton, deleteButton, backButton}) {
            btn.setFont(UIGlobal.BUTTON_FONT);
            btn.setBackground(UIGlobal.BUTTON_COLOR);
            btn.setFocusPainted(false);
            bottomPanel.add(btn);
        }

        add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> loadRecipes());
        sortBox.addActionListener(e -> loadRecipes());
        backButton.addActionListener(e -> {
            dispose();
            Navigator.goBack(this);
        });
        addButton.addActionListener(e -> {
            new AddRecipe(user);
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String title = (String) model.getValueAt(row, 0);
                    new RecipeDetails(user, title);
                }
            }
        });

        loadRecipes();
        setVisible(true);
    }

    private void loadRecipes() {
        model.setRowCount(0);
        String query = "SELECT recipe_id, title, duration, price FROM recipes";
        String search = searchField.getText();
        if (!search.isBlank()) {
            query += " WHERE title LIKE '%" + search + "%'";
        }

        switch (sortBox.getSelectedIndex()) {
            case 1: query += " ORDER BY duration"; break;
            case 2: query += " ORDER BY price"; break;
            default: query += " ORDER BY title"; break;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("title"), rs.getDouble("duration"), rs.getDouble("price")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// RecipeDetails.java – Improved UI and Image Display

class RecipeDetails extends JFrame {
    private JLabel titleLabel, imageLabel;
    private JTextArea descriptionArea, instructionsArea, ingredientsArea;
    private JButton favButton, backButton;

    public RecipeDetails(User user, String recipeTitle) {
        setTitle("Recipe Details: " + recipeTitle);
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIGlobal.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        titleLabel = new JLabel(recipeTitle);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Image + Info
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        loadRecipeImage(recipeTitle);
        centerPanel.add(imageLabel, BorderLayout.NORTH);

        // Text Areas
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        textPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        descriptionArea = createTextArea("Description");
        instructionsArea = createTextArea("Instructions");
        ingredientsArea = createTextArea("Ingredients");

        textPanel.add(new JScrollPane(descriptionArea));
        textPanel.add(new JScrollPane(ingredientsArea));
        textPanel.add(new JScrollPane(instructionsArea));

        centerPanel.add(textPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        favButton = new JButton("Add to Favourites");
        backButton = new JButton("Go Back");

        styleButton(favButton);
        styleButton(backButton);

        bottomPanel.add(favButton);
        bottomPanel.add(backButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadRecipeDetails(recipeTitle);

        favButton.addActionListener(e -> addToFavourites(user, recipeTitle));
        backButton.addActionListener(e -> Navigator.goBack(this));

        setVisible(true);
    }

    private JTextArea createTextArea(String label) {
        JTextArea area = new JTextArea(label + "...\n");
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setBackground(new Color(255, 255, 245));
        area.setBorder(BorderFactory.createTitledBorder(label));
        area.setEditable(false);
        return area;
    }

    private void styleButton(JButton button) {
        button.setFont(UIGlobal.BUTTON_FONT);
        button.setBackground(UIGlobal.BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
    }

    private void loadRecipeDetails(String title) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM recipes WHERE title = ?")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                descriptionArea.setText(rs.getString("description"));
                instructionsArea.setText(rs.getString("instructions"));
                ingredientsArea.setText(getIngredients(rs.getInt("recipe_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecipeImage(String title) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT image FROM recipes WHERE title = ?")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                byte[] imageData = rs.getBytes("image");
                if (imageData != null) {
                    ImageIcon icon = new ImageIcon(imageData);
                    Image scaled = icon.getImage().getScaledInstance(250, 200, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                } else {
                    imageLabel.setText("No image available");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getIngredients(int recipeId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT i.name, ri.quantity FROM recipe_ingredients ri JOIN ingredients i ON ri.ingredient_id = i.ingredient_id WHERE ri.recipe_id = ?")) {
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("name")).append(" - ").append(rs.getString("quantity")).append("\n");
            }
        }
        return sb.toString();
    }

    private void addToFavourites(User user, String title) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO favourites (user_id, recipe_id) VALUES (?, ?)");
            stmt.setInt(1, getUserId(user.getFirst_name()));
            stmt.setInt(2, getRecipeIdByTitle(title));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Added to Favourites!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to add to favourites.");
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

    private int getRecipeIdByTitle(String title) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT recipe_id FROM recipes WHERE title = ?")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("recipe_id") : -1;
        }
    }
}


class Favourites extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton searchButton, removeButton, backButton, logoutButton;

    public Favourites(User user) {
        setTitle("Favourite Recipes");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(UIGlobal.BUTTON_FONT);
        logoutButton.setBackground(UIGlobal.BUTTON_COLOR);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            dispose();
            new Login();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Title", "Added At"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        removeButton = new JButton("Remove from Favourites");
        backButton = new JButton("Go Back");
        for (JButton btn : new JButton[]{removeButton, backButton}) {
            btn.setFont(UIGlobal.BUTTON_FONT);
            btn.setBackground(UIGlobal.BUTTON_COLOR);
            btn.setFocusPainted(false);
            bottomPanel.add(btn);
        }

        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            dispose();
            Navigator.goBack(this);
        });

        searchButton.addActionListener(e -> loadFavourites(user, searchField.getText()));
        removeButton.addActionListener(e -> removeFromFavourites(user));

        loadFavourites(user, "");
        setVisible(true);
    }

    private void loadFavourites(User user, String search) {
        model.setRowCount(0);
        String query = "SELECT r.title, f.added_at FROM favourites f JOIN recipes r ON f.recipe_id = r.recipe_id WHERE f.user_id = ?";
        if (!search.isBlank()) {
            query += " AND r.title LIKE ?";
        }
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, getUserId(user.getFirst_name()));
            if (!search.isBlank()) stmt.setString(2, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("title"), rs.getTimestamp("added_at")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeFromFavourites(User user) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String title = (String) model.getValueAt(row, 0);
        // Remove by title (you should use recipe_id)
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("DELETE f FROM favourites f JOIN recipes r ON f.recipe_id = r.recipe_id WHERE f.user_id = ? AND r.title = ?")) {
            stmt.setInt(1, getUserId(user.getFirst_name()));
            stmt.setString(2, title);
            stmt.executeUpdate();
            model.removeRow(row);
        } catch (SQLException e) {
            e.printStackTrace();
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
