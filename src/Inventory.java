import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// (Import statements stay the same)

import java.util.HashMap;
import java.util.Map;

public class Inventory extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sortBox;
    private JButton addButton, deleteButton, backButton, logoutButton;
    private User user;

    private Map<Integer, String> originalNames = new HashMap<>();

    public Inventory(User user) {
        this.user = user;
        setTitle("My Inventory");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupTopBar();    // create sortBox FIRST
        setupTable();     // table depends on sortBox
        setupBottomBar();

        setVisible(true);
    }


    private void setupTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        // LOGOUT BUTTON (right side)
        logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            Navigator.clear();
            dispose();
            new Login();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        // SORT DROPDOWN (center nicely)
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        sortBox = new JComboBox<>(new String[]{"Sort by Name", "Sort by Quantity"});
        sortBox.addActionListener(e -> loadIngredients());
        centerPanel.add(new JLabel("Sort:"));
        centerPanel.add(sortBox);

        topPanel.add(centerPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
    }


    private boolean suppressEvents = false; // 🛡 global field to suppress events temporarily

    private void setupTable() {
        model = new DefaultTableModel(new String[]{"Ingredient", "Quantity"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        model.addTableModelListener(e -> {
            if (suppressEvents) return; // 🛡 Ignore if suppressing

            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && col >= 0) {
                    String name = (String) model.getValueAt(row, 0);
                    String quantity = (String) model.getValueAt(row, 1);

                    if (col == 0) {
                        String correctName = originalNames.get(row);
                        if (correctName != null && !name.equals(correctName)) {
                            JOptionPane.showMessageDialog(this,
                                    "Editing Ingredient Name is not allowed!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);

                            suppressEvents = true; // 🛡 Suppress events before restoring
                            model.setValueAt(correctName, row, 0);
                            suppressEvents = false; // 🔓 Reactivate
                        }
                    } else if (col == 1) {
                        updateIngredientQuantity(name, quantity);
                        flashCell(row, col);
                    }
                }
            }
        });

        loadIngredients();
    }




    private void updateIngredient(String name, String quantity) {
        if (name == null || name.isBlank() || quantity == null || quantity.isBlank()) return;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE ingredients SET quantity = ? WHERE name = ?")) {
                stmt.setString(1, quantity);
                stmt.setString(2, name);
                int updated = stmt.executeUpdate();
                if (updated > 0) conn.commit();
                else conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void flashCell(int row, int col) {
        final Color original = table.getSelectionBackground();
        table.changeSelection(row, col, false, false);
        table.setSelectionBackground(Color.YELLOW);

        Timer timer = new Timer(500, evt -> table.setSelectionBackground(original));
        timer.setRepeats(false);
        timer.start();
    }


    private void setupBottomBar() {
        JPanel bottomPanel = new JPanel();
        addButton = createStyledButton("Add Ingredient");
        deleteButton = createStyledButton("Delete Ingredient");
        backButton = createStyledButton("Go Back");

        addButton.addActionListener(e -> addIngredient());
        deleteButton.addActionListener(e -> deleteIngredient());
        backButton.addActionListener(e -> Navigator.goBack(this));

        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void loadIngredients() {
        model.setRowCount(0);
        originalNames.clear(); // 💥 Clear before reloading

        List<Object[]> data = new ArrayList<>();

        String query = """
        SELECT i.name, mi.quantity
        FROM my_inventory mi
        JOIN ingredients i ON mi.ingredient_id = i.ingredient_id
        WHERE mi.user_id = ?
    """;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, getUserId());
            ResultSet rs = stmt.executeQuery();
            int row = 0;
            while (rs.next()) {
                String name = rs.getString("name");
                String quantity = rs.getString("quantity");
                data.add(new Object[]{name, quantity});
                originalNames.put(row++, name); // 🛡 Save original name linked to row
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory.");
            e.printStackTrace();
        }

        if (sortBox != null && sortBox.getSelectedIndex() == 1) {
            data.sort((o1, o2) -> Double.compare(
                    parseQuantity((String) o1[1]),
                    parseQuantity((String) o2[1])
            ));
        }
        if (sortBox != null && sortBox.getSelectedIndex() == 0) {
            data.sort((o1, o2) -> CharSequence.compare(
                    ((String) o1[0]),
                    ((String) o2[0])
            ));
        }

        for (Object[] row : data) {
            model.addRow(row);
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
            case "kg":
                return value * 1000; // kilograms to grams
            case "g":
                return value;        // grams
            case "mg":
                return value / 1000; // milligrams to grams
            case "l":
                return value * 1000; // liters to milliliters
            case "L":
                return value * 1000;
            case "ml":
                return value;        // milliliters
            case "pcs":
                return value;        // pieces
            default:
                return value;        // unknown units treated as-is
        }
    }


    private void addIngredient() {
        String name = JOptionPane.showInputDialog(this, "Enter ingredient name:");
        if (name == null || name.isBlank()) return;
        String quantity = JOptionPane.showInputDialog(this, "Enter quantity (e.g., 500 g):");
        if (quantity == null || quantity.isBlank()) return;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);

            int ingredientId = getIngredientIdByName(name, conn);
            if (ingredientId == -1) {
                JOptionPane.showMessageDialog(this, "Ingredient does not exist.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO my_inventory (user_id, ingredient_id, quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = ?
                """);
            stmt.setInt(1, getUserId());
            stmt.setInt(2, ingredientId);
            stmt.setString(3, quantity);
            stmt.setString(4, quantity);
            stmt.executeUpdate();

            conn.commit();
            loadIngredients();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding ingredient to your inventory.");
            e.printStackTrace();
        }
    }

    private void deleteIngredient() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an ingredient to delete.");
            return;
        }

        String name = (String) model.getValueAt(row, 0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);

            int ingredientId = getIngredientIdByName(name, conn);
            if (ingredientId == -1) {
                JOptionPane.showMessageDialog(this, "Ingredient not found.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM my_inventory
                WHERE user_id = ? AND ingredient_id = ?
                """);
            stmt.setInt(1, getUserId());
            stmt.setInt(2, ingredientId);
            stmt.executeUpdate();

            conn.commit();
            model.removeRow(row);
            JOptionPane.showMessageDialog(this, "Ingredient removed from your inventory.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting ingredient.");
            e.printStackTrace();
        }
    }

    private void updateIngredientQuantity(String name, String quantity) {
        if (name == null || name.isBlank() || quantity == null || quantity.isBlank()) return;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123")) {
            conn.setAutoCommit(false);

            int ingredientId = getIngredientIdByName(name, conn);
            if (ingredientId == -1) {
                JOptionPane.showMessageDialog(this, "Ingredient not found.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("""
                UPDATE my_inventory
                SET quantity = ?
                WHERE user_id = ? AND ingredient_id = ?
                """);
            stmt.setString(1, quantity);
            stmt.setInt(2, getUserId());
            stmt.setInt(3, ingredientId);

            int updated = stmt.executeUpdate();
            if (updated > 0) conn.commit();
            else conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadIngredientName(int row, String oldName) {
        model.setValueAt(oldName, row, 0);
    }

    private int getUserId() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users WHERE name = ?")) {
            stmt.setString(1, user.getFirst_name());
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("user_id") : -1;
        }
    }

    private int getIngredientIdByName(String name, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("""
            SELECT ingredient_id FROM ingredients WHERE name = ?
            """);
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("ingredient_id");
        }
        return -1;
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
