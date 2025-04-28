import org.mindrot.jbcrypt.BCrypt;
import java.io.InputStream;
import java.sql.*;

public class connect {
    private static final String URL = "jdbc:mysql://localhost:3306/cookingApp";
    private static final String USER = "root";
    private static final String PASSWORD = "123";

    public static void addUser(String name, String username, String hashedPassword, InputStream img) {
        String query = "INSERT INTO users (name, username, password_hash, image) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            conn.setAutoCommit(false);

            pstmt.setString(1, name);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedPassword);
            pstmt.setBinaryStream(4, img);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                conn.commit();
                System.out.println("User added successfully.");
            } else {
                conn.rollback();
                System.out.println("User registration failed.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static User Login(String username, String password) {
        String query = "SELECT name, password_hash, image FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
            e.printStackTrace();
        }
        return null;
    }
}
