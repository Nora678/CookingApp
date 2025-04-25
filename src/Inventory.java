import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class Inventory extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sortBox;
    private JButton addButton, deleteButton, backButton;
    private JPanel topPanel, centerPanel, bottomPanel;
    private JButton logoutButton;

    public Inventory(User user) {
        setTitle("My Inventory");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        topPanel = new JPanel(new BorderLayout());
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
        add(topPanel, BorderLayout.NORTH);

        centerPanel = new JPanel(new BorderLayout());
        model = new DefaultTableModel(new String[]{"Ingredient", "Quantity"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        sortBox = new JComboBox<>(new String[]{"Sort by Name", "Sort by Quantity"});
        sortBox.addActionListener(e -> loadIngredients(sortBox.getSelectedIndex()));
        centerPanel.add(sortBox, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        addButton = new JButton("Add Item");
        deleteButton = new JButton("Delete Item");
        backButton = new JButton("Go Back");

        addButton.addActionListener(e -> addIngredient());
        deleteButton.addActionListener(e -> deleteIngredient());
        backButton.addActionListener(e -> {
            dispose();
            Navigator.goBack(this);
        });

        for (JButton btn : new JButton[]{addButton, deleteButton, backButton}) {
            btn.setFont(UIGlobal.BUTTON_FONT);
            btn.setBackground(UIGlobal.BUTTON_COLOR);
            btn.setFocusPainted(false);
            bottomPanel.add(btn);
        }
        add(bottomPanel, BorderLayout.SOUTH);

        loadIngredients(0);
        setVisible(true);
    }

    private void loadIngredients(int sortIndex) {
        model.setRowCount(0);
        String query = "SELECT name, quantity FROM ingredients";
        if (sortIndex == 1) query += " ORDER BY quantity";
        else query += " ORDER BY name";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("quantity")});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory.");
            e.printStackTrace();
        }
    }

    private void addIngredient() {
        String name = JOptionPane.showInputDialog(this, "Enter ingredient name:");
        String quantity = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (name == null || quantity == null || name.isBlank() || quantity.isBlank()) return;

        String query = "INSERT INTO ingredients (name, quantity) VALUES (?, ?) ON DUPLICATE KEY UPDATE quantity = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, quantity);
            stmt.setString(3, quantity);
            stmt.executeUpdate();
            loadIngredients(sortBox.getSelectedIndex());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding ingredient.");
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
        String query = "DELETE FROM ingredients WHERE name = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cookingApp", "root", "123");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.executeUpdate();
            model.removeRow(row);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting ingredient.");
            e.printStackTrace();
        }
    }
}
