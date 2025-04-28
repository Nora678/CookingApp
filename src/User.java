import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class User {
    private String username;
    private String fullName;
    private InputStream imgStream;

    public User(String username, String fullName, InputStream imgStream) {
        this.username = username;
        this.fullName = fullName;
        this.imgStream = imgStream;
    }

    public String getUsername() {
        return username;
    }

    public String getFirst_name() {
        return fullName;
    }

    public ImageIcon getImageIcon() {
        if (imgStream == null) {
            // Return a default placeholder image if no profile image exists
            return new ImageIcon(new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB));
        }
        try {
            BufferedImage img = ImageIO.read(imgStream);
            if (img == null) {
                return new ImageIcon(new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB));
            }
            return new ImageIcon(img);
        } catch (IOException e) {
            System.err.println("Failed to load user image: " + e.getMessage());
            return new ImageIcon(new BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB));
        }
    }
}
