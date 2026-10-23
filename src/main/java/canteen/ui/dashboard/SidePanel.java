package canteen.ui.dashboard;

import canteen.ui.theme.ThemeManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SidePanel extends JPanel {

    public SidePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 16, 0, 0));
        setPreferredSize(new Dimension(350, 0));
        // No components (Order Status Summary removed)
    }

    public void refresh() {
        // No-op: nothing to refresh
    }
}