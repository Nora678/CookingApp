import javax.swing.*;
import java.util.Stack;

public class Navigator {
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
