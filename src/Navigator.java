// Inventory.java already provided above
// BrowseRecipes.java, RecipeDetails.java, Favourites.java
// Welcome.java Menu Implementation with Navigation History

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Stack;

class Navigator {
    private static final Stack<JFrame> history = new Stack<>();

    public static void push(JFrame frame) {
        history.push(frame);
    }

    public static void goBack(JFrame current) {
        if (!history.isEmpty()) {
            JFrame previous = history.pop();
            current.dispose();
            previous.setVisible(true);
        }
    }

    public static void clear() {
        history.clear();
    }
}

// In all other pages like Inventory.java, Favourites.java, etc.,
// Replace `new Welcome(user);` in the Go Back button with: `Navigator.goBack(this);`
// Example:
// backButton.addActionListener(e -> Navigator.goBack(this));
