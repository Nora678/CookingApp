import javax.swing.*;
import java.awt.*;


public class UIGlobal {
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 600;

    public static final Color BACKGROUND_COLOR = new Color(255, 253, 208); // Light pastel yellow
    public static final Color BUTTON_COLOR = new Color(245, 222, 179); // Soft wheat/beige
    public static final Color BUTTON_HOVER_COLOR = new Color(255, 228, 181); // Slightly lighter on hover
    public static final Color TEXT_COLOR = new Color(60, 60, 60); // Dark grey for text

    public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 30);
    public static final Font SUBTITLE_FONT = new Font("Serif", Font.PLAIN, 22);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);

    public static final ImageIcon LOGO_ICON = loadLogo();

    private static ImageIcon loadLogo() {
        try {
            ImageIcon icon = new ImageIcon(UIGlobal.class.getResource("/cookingAppLogo.png"));
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("Logo loading failed: " + e.getMessage());
            return null;
        }
    }
}


