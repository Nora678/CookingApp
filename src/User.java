import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class User {
    private String username;
    private String fullName;
    private InputStream imgStream;

    public User(String username, String fullName, InputStream imgStream) {
        this.username = username;
        this.fullName = fullName;
        this.imgStream = imgStream;
    }

    public String getFirst_name() {
        return fullName;
    }

    public ImageIcon getImageIcon() {
        try {
            BufferedImage image = ImageIO.read(imgStream);
            return new ImageIcon(image);
        } catch (IOException e) {
            System.err.println("Image load failed: " + e.getMessage());
            return null;
        }
    }
}
