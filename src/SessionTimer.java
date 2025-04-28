import javax.swing.*;

public class SessionTimer {
    private static Timer timer;
    private static final int TIMEOUT_MINUTES = 5;

    public static void startSessionTimer(JFrame frame) {
        timer = new Timer(TIMEOUT_MINUTES * 60 * 1000, e -> {
            JOptionPane.showMessageDialog(frame, "Session expired. Please login again.");
            Session.logout();
            frame.dispose();
            new Login();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void resetTimer() {
        if (timer != null) {
            timer.restart();
        }
    }
}
