import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

class Favourites extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton searchButton, backButton, logoutButton;
    private User user;

    public Favourites(User user) {
        this.user = user;
        setTitle("Favourite Recipes");
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
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        searchField = new JTextField(15);
        searchButton = createStyledButton("Search");

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            Navigator.clear();

            new Login();
        });

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        searchButton.addActionListener(e -> loadFavourites());
    }

    private void setupTable() {
        model = new DefaultTableModel(new String[]{"Title", "Added At"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                String title = (String) model.getValueAt(table.getSelectedRow(), 0);
                Navigator.push(this);
                new RecipeDetails(user, title, this); // openedFromFavourites = true
                loadFavourites();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadFavourites();
    }

    private void setupBottomBar() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        backButton = createStyledButton("Go Back");
        backButton.addActionListener(e -> Navigator.goBack(this));

        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadFavourites() {
        model.setRowCount(0);
        String search = searchField.getText().trim();

        String query = """
            SELECT r.title, f.added_at
            FROM favourites f
            JOIN recipes r ON f.recipe_id = r.recipe_id
            WHERE f.user_id = ?
            """ + (search.isEmpty() ? "" : "AND r.title LIKE ?") +
                " ORDER BY f.added_at DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, getUserId(user.getFirst_name()));
            if (!search.isEmpty()) {
                stmt.setString(2, "%" + search + "%");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("title"), rs.getTimestamp("added_at")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading favourites.");
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

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(UIGlobal.BUTTON_COLOR);
        button.setFont(UIGlobal.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        return button;
    }
}
