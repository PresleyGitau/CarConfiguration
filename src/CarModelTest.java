import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class CarModelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Car Models — inset windows");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1050, 700);
            frame.add(new CarModels());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
