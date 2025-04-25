/*import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class form1 extends JFrame {
    private JPanel panel1;
    private JTable table1;
    private JComboBox<String> comboBox1;
    private JButton deleteButton, addColumnButton, deleteColumnButton;
    private JTextField titleSearchField, releaseYearSearchField;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private ArrayList<String[]> movies;

    public form1() {
        setTitle("Movie Manager");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel1 = new JPanel(new BorderLayout());
        setContentPane(panel1);

        JPanel searchPanel = new JPanel(new FlowLayout());
        titleSearchField = new JTextField(10);
        releaseYearSearchField = new JTextField(10);
        searchPanel.add(new JLabel("Title:"));
        searchPanel.add(titleSearchField);
        searchPanel.add(new JLabel("Year:"));
        searchPanel.add(releaseYearSearchField);
        panel1.add(searchPanel, BorderLayout.NORTH);

        // Default columns for the table
        model = new DefaultTableModel(new String[]{"film_id", "title", "release_year", "length", "rating"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };

        table1 = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table1.setRowSorter(sorter);
        panel1.add(new JScrollPane(table1), BorderLayout.CENTER);

        comboBox1 = new JComboBox<>(new String[]{"Sort by Title (A-Z)", "Sort by Length (Descending)"});
        panel1.add(comboBox1, BorderLayout.SOUTH);

        // Changed buttonPanel layout to BoxLayout for vertical arrangement
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));  // BoxLayout to stack buttons vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Add padding around the button panel

        // Create buttons and add space between them
        deleteButton = new JButton("Delete");
        addColumnButton = new JButton("Add Column");
        deleteColumnButton = new JButton("Delete Column");

        // Styling buttons
        deleteButton.setPreferredSize(new Dimension(150, 40));
        addColumnButton.setPreferredSize(new Dimension(150, 40));
        deleteColumnButton.setPreferredSize(new Dimension(150, 40));

        // Set a consistent size and margin to make buttons visually appealing
        deleteButton.setMaximumSize(deleteButton.getPreferredSize());
        addColumnButton.setMaximumSize(addColumnButton.getPreferredSize());
        deleteColumnButton.setMaximumSize(deleteColumnButton.getPreferredSize());

        // Add buttons with vertical space in between
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalStrut(10));  // Space between buttons
        buttonPanel.add(addColumnButton);
        buttonPanel.add(Box.createVerticalStrut(10));  // Space between buttons
        buttonPanel.add(deleteColumnButton);
        panel1.add(buttonPanel, BorderLayout.WEST);

        loadMovies();

        // Search functionality for title and release year
        titleSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMovies(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMovies(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMovies(); }
        });

        releaseYearSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMovies(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMovies(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMovies(); }
        });

        comboBox1.addActionListener(e -> sortMovies());

        // Delete movie functionality
        deleteButton.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                String movieTitle = table1.getValueAt(row, 1).toString().trim();
                if (JOptionPane.showConfirmDialog(null, "Delete '" + movieTitle + "'?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    connect.deleteRow(movieTitle);
                    loadMovies();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Select a row to delete.");
            }
        });

        // Add column functionality
        addColumnButton.addActionListener(e -> {
            String columnName = JOptionPane.showInputDialog("Enter column name:");
            String[] columnTypes = {"VARCHAR(255)", "INT", "DOUBLE", "CHAR(10)", "BOOLEAN", "TEXT"};
            String columnType = (String) JOptionPane.showInputDialog(null, "Select column type:", "Column Type", JOptionPane.QUESTION_MESSAGE, null, columnTypes, columnTypes[0]);
            if (columnName != null && columnType != null) {
                connect.addColumn(columnName, columnType);
                loadMovies();
            }
        });

        // Delete column functionality
        deleteColumnButton.addActionListener(e -> {
            String columnName = JOptionPane.showInputDialog("Enter column name to delete:");
            if (columnName != null && !columnName.trim().isEmpty()) {
                int confirmDelete = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the column '" + columnName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirmDelete == JOptionPane.YES_OPTION) {
                    connect.deleteColumn(columnName);
                    loadMovies();  // Reload after deleting the column
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a valid column name.");
            }
        });

        // Save changes in the table
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row != -1 && column != -1) {
                    String movieTitle = (String) model.getValueAt(row, 1);
                    String columnName = model.getColumnName(column);
                    String newValue = (String) model.getValueAt(row, column);
                    connect.updateRow(movieTitle, columnName, newValue);
                }
            }
        });

        setVisible(true);
    }

    // Load movie data into the table
    private void loadMovies() {
        movies = connect.executeQuery("", "", "Title");

        // Get column names dynamically
        String[] columnNames = {"film_id", "title", "release_year", "length", "rating"};
        try (Connection connection = DriverManager.getConnection(connect.getURL(), connect.getUser(), connect.getPassword());
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE sakila.film")) {

            ArrayList<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString("Field"));
            }
            columnNames = columns.toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.setColumnCount(0);
        for (String columnName : columnNames) {
            model.addColumn(columnName);
        }

        model.setRowCount(0);

        for (String[] movie : movies) {
            model.addRow(movie);
        }
    }

    // Filter movies based on title
    private void filterMovies() {
        RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + titleSearchField.getText(), 1);
        sorter.setRowFilter(rf);
    }

    // Sort movies based on selected criteria
    private void sortMovies() {
        String orderBy = (String) comboBox1.getSelectedItem();
        movies = connect.executeQuery("", "", orderBy);
        model.setRowCount(0);
        for (String[] movie : movies) {
            model.addRow(movie);
        }
    }
}
*/