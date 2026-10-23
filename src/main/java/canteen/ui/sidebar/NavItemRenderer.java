package canteen.ui.sidebar;

import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.SwingConstants;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class NavItemRenderer extends JPanel implements ListCellRenderer<NavItem> {

    private final JLabel iconLabel;
    private final JLabel textLabel;
    private boolean isSelected = false;

    public NavItemRenderer() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(new javax.swing.border.EmptyBorder(10, 14, 10, 14));

        iconLabel = new JLabel();
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setPreferredSize(new Dimension(28, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        textLabel = new JLabel();
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends NavItem> list, NavItem value, int index, boolean isSelected, boolean cellHasFocus) {
        this.isSelected = isSelected;

        if (value != null) {
            iconLabel.setText(value.getIcon());
            textLabel.setText(value.getLabel());
        }

        if (isSelected) {
            iconLabel.setForeground(ThemeManager.getAccent());
            textLabel.setForeground(ThemeManager.getAccent());
            textLabel.setFont(textLabel.getFont().deriveFont(Font.BOLD));
        } else {
            iconLabel.setForeground(ThemeManager.getTextSecondary());
            textLabel.setForeground(ThemeManager.getTextPrimary());
            textLabel.setFont(textLabel.getFont().deriveFont(Font.PLAIN));
        }

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
            int w = getWidth();
            int h = getHeight();
            int arc = 10;

            g2.setColor(ThemeManager.getBgSelected());
            g2.fill(new RoundRectangle2D.Float(4, 2, w - 8, h - 4, arc, arc));

            g2.setColor(ThemeManager.getAccent());
            g2.fill(new RoundRectangle2D.Float(4, h / 2 - 8, 3, 16, 2, 2));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}