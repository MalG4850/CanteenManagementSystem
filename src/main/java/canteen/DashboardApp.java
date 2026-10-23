package canteen;

import canteen.ui.theme.ThemeManager;
import javax.swing.*;

public class DashboardApp {

    public static void main(String[] args) {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);

        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
        });
    }
}