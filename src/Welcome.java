// Inventory.java already provided above
// BrowseRecipes.java, RecipeDetails.java, Favourites.java
// Welcome.java Menu Implementation

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

class Welcome extends JFrame {
    private JLabel welcomeLabel, img;
    private JButton logoutButton, inventoryButton, browseButton, favouritesButton;
    private JPanel mainPanel, topPanel, menuPanel;

    public Welcome(User user) {
        setTitle("Welcome");
        setSize(UIGlobal.WINDOW_WIDTH, UIGlobal.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
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

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIGlobal.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        welcomeLabel = new JLabel("Welcome, " + user.getFirst_name());
        welcomeLabel.setFont(UIGlobal.TITLE_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(welcomeLabel, gbc);

        ImageIcon icon = user.getImageIcon();
        if (icon != null) {
            Image imgResized = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            img = new JLabel(new ImageIcon(imgResized));
        } else {
            img = new JLabel("No Image Available");
        }
        gbc.gridy = 1;
        mainPanel.add(img, gbc);

        menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setBackground(UIGlobal.BACKGROUND_COLOR);

        inventoryButton = new JButton("My Inventory");
        browseButton = new JButton("Browse Recipes");
        favouritesButton = new JButton("Favourites");

        for (JButton btn : new JButton[]{inventoryButton, browseButton, favouritesButton}) {
            btn.setFont(UIGlobal.BUTTON_FONT);
            btn.setBackground(UIGlobal.BUTTON_COLOR);
            btn.setFocusPainted(false);
            menuPanel.add(btn);
        }

        inventoryButton.addActionListener(e -> {
            dispose();
            Navigator.push(this);
            new Inventory(user);
        });

        browseButton.addActionListener(e -> {
            dispose();
            Navigator.push(this);
            new BrowseRecipes(user);
        });

        favouritesButton.addActionListener(e -> {
            dispose();
            Navigator.push(this);
            new Favourites(user);
        });

        gbc.gridy = 2;
        mainPanel.add(menuPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}

// Rest of the code remains unchanged below