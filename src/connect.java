import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class connect {
    private static final String URL = "jdbc:mysql://localhost:3306/cookingApp";
    private static final String USER = "root";
    private static final String PASSWORD = "123";

    public static void addUser(String name, String username, String hashedPassword, InputStream img) {
        String query = "INSERT INTO users (name, username, password_hash, image) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);

            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedPassword);
            pstmt.setBinaryStream(4, img);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("User added successfully.");
            } else {
                connection.rollback();
                System.out.println("User registration failed.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static User Login(String username, String password) {
        String query = "SELECT name, password_hash, image FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password_hash");

                    if (BCrypt.checkpw(password, hashedPassword)) {
                        InputStream imgStream = rs.getBinaryStream("image");
                        return new User(username, rs.getString("name"), imgStream);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Initiating rollback");
            e.printStackTrace();
        }
        return null;
    }
}
