import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class BrowseRecipes extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sortBox, durationBox;
    private JTextField searchField;
    private JButton addButton, backButton, logoutButton;
    private JCheckBox cookableCheckBox;
    private User user;

    public BrowseRecipes(User user) {
        this.user = user;
        setTitle("Browse Recipes");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupTopBar();
        setupTable();
        setupBottomBar();

        setVisible(true);
    }

    private void setupTopBar() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Logo on Left ---
        JLabel logoLabel = new JLabel(UIGlobal.LOGO_ICON);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        topPanel.add(logoLabel, gbc);

        gbc.gridheight = 1;

        // --- Search Label ---
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        topPanel.add(new JLabel("Search:"), gbc);

        // --- Search Field ---
        searchField = new JTextField(20);
        gbc.gridx = 2;
        gbc.weightx = 1;  // Important: allow field to expand
        topPanel.add(searchField, gbc);

        // --- Sort Dropdown ---
        sortBox = new JComboBox<>(new String[]{"Sort by Title", "Sort by Duration", "Sort by Price"});
        gbc.gridx = 3;
        gbc.weightx = 0;
        topPanel.add(sortBox, gbc);

        // --- Duration Filter Dropdown ---
        durationBox = new JComboBox<>(new String[]{
                "All Durations", "< 15 min", "< 30 min", "< 60 min", "< 90 min"
        });
        gbc.gridx = 4;
        topPanel.add(durationBox, gbc);

        // --- Logout Button on Right ---
        logoutButton = createStyledButton("Logout");
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        topPanel.add(logoutButton, gbc);

        // --- Cookable Checkbox (Bottom Line) ---
        cookableCheckBox = new JCheckBox("Only Cookable Recipes");
        cookableCheckBox.setBackground(UIGlobal.BACKGROUND_COLOR);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(cookableCheckBox, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadRecipes(); }
            public void removeUpdate(DocumentEvent e) { loadRecipes(); }
            public void changedUpdate(DocumentEvent e) { loadRecipes(); }
        });
        sortBox.addActionListener(e -> loadRecipes());
        durationBox.addActionListener(e -> loadRecipes());
        cookableCheckBox.addActionListener(e -> loadRecipes());
        logoutButton.addActionListener(e -> {
            Navigator.clear();
            new Login();
        });
    }


    private void setupTable() {
        model = new DefaultTableModel(new String[]{"Title", "Duration", "Price"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                Navigator.push(this);
                String title = (String) model.getValueAt(table.getSelectedRow(), 0);
                new RecipeDetails(user, title, this);
                loadRecipes();
            }
        });

        loadRecipes();
    }

    private void setupBottomBar() {
        JPanel bottomPanel = new JPanel();
        addButton = createStyledButton("Add Recipe");
        backButton = createStyledButton("Go Back");

        addButton.addActionListener(e -> {
            Navigator.push(this);
            new AddRecipe(user);
        });
        backButton.addActionListener(e -> Navigator.goBack(this));

        bottomPanel.add(addButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadRecipes() {
        model.setRowCount(0);
        String searchText = searchField.getText().trim();
        int selectedDuration = getSelectedDuration();

        Set<String> availableIngredients = new HashSet<>();
        if (cookableCheckBox.isSelected()) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
                 PreparedStatement stmt = conn.prepareStatement("""
                 SELECT i.name FROM my_inventory mi JOIN ingredients i ON mi.ingredient_id = i.ingredient_id WHERE mi.user_id = ?
                 """)) {
                stmt.setInt(1, getUserId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    availableIngredients.add(rs.getString("name").toLowerCase());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }

        String query = "SELECT recipe_id, title, duration, price FROM recipes WHERE 1=1";
        if (!searchText.isEmpty()) query += " AND title LIKE ?";
        if (selectedDuration > 0) query += " AND duration <= " + selectedDuration;

        switch (sortBox.getSelectedIndex()) {
            case 1 -> query += " ORDER BY duration ASC";
            case 2 -> query += " ORDER BY price ASC";
            default -> query += " ORDER BY title ASC";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int recipeId = rs.getInt("recipe_id");
                String title = rs.getString("title");
                double duration = rs.getDouble("duration");
                double price = rs.getDouble("price");

                if (cookableCheckBox.isSelected()) {
                    if (canCookRecipe(recipeId, availableIngredients)) {
                        model.addRow(new Object[]{title, duration, price});
                    }
                } else {
                    model.addRow(new Object[]{title, duration, price});
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean canCookRecipe(int recipeId, Set<String> availableIngredients) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {

            // 1. Load required ingredients and quantities
            PreparedStatement stmt = conn.prepareStatement("""
            SELECT i.name, ri.quantity
            FROM recipe_ingredients ri
            JOIN ingredients i ON ri.ingredient_id = i.ingredient_id
            WHERE ri.recipe_id = ?
        """);
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String requiredName = rs.getString("name").toLowerCase();
                String requiredQuantityStr = rs.getString("quantity");

                // 2. If user doesn't have the ingredient at all
                if (!availableIngredients.contains(requiredName)) {
                    return false;
                }

                // 3. Now check if the user's quantity is enough:
                double requiredAmount = parseQuantity(requiredQuantityStr);
                double availableAmount = getAvailableIngredientQuantity(requiredName);

                if (availableAmount < requiredAmount) {
                    return false;
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private double getAvailableIngredientQuantity(String ingredientName) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            PreparedStatement stmt = conn.prepareStatement("""
            SELECT mi.quantity
            FROM my_inventory mi
            JOIN ingredients i ON mi.ingredient_id = i.ingredient_id
            WHERE i.name = ? AND mi.user_id = ?
        """);
            stmt.setString(1, ingredientName);
            stmt.setInt(2, getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return parseQuantity(rs.getString("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // not found
    }


    private int getSelectedDuration() {
        return switch (durationBox.getSelectedIndex()) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 60;
            case 4 -> 90;
            default -> -1;
        };
    }

    private int getUserId() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE name = ?")) {
            stmt.setString(1, user.getFirst_name());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        }
    }
    private double parseQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.isBlank()) return 0;

        quantityStr = quantityStr.trim().toLowerCase();
        double value = 0;
        String numberPart = quantityStr.replaceAll("[^0-9.,]", "").replace(",", ".");
        String unitPart = quantityStr.replaceAll("[0-9.,\\s]", "").trim();

        try {
            value = Double.parseDouble(numberPart);
        } catch (NumberFormatException e) {
            return 0;
        }

        switch (unitPart) {
            case "kg": return value * 1000;
            case "g": return value;
            case "mg": return value / 1000;
            case "l": return value * 1000;
            case "L": return value * 1000;
            case "ml": return value;
            case "pcs": return value;
            default: return value;
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