package canteen.ui.components;

import canteen.ui.theme.ThemeManager;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatusBadge extends JLabel {

    private final OrderStatus status;
    private final boolean small;

    public StatusBadge(OrderStatus status) {
        this(status, false);
    }

    public StatusBadge(OrderStatus status, boolean small) {
        this.status = status;
        this.small = small;
        init();
    }

    private void init() {
        setText(status.getDisplayName());
        setFont(new Font("Segoe UI", Font.BOLD, small ? 10 : 11));
        setBorder(new EmptyBorder(small ? 3 : 4, small ? 8 : 10, small ? 3 : 4, small ? 8 : 10));
        setHorizontalAlignment(CENTER);
        setOpaque(false);
        setForeground(getTextColor());
    }

    private Color getTextColor() {
        return switch (status) {
            case PENDING -> ThemeManager.getWarning();
            case PREPARING -> ThemeManager.getInfo();
            case READY -> ThemeManager.getSuccess();
            case SERVED -> ThemeManager.getTextSecondary();
            case CANCELLED -> ThemeManager.getError();
        };
    }

    private Color getBgColor() {
        return switch (status) {
            case PENDING -> ThemeManager.getWarningBg();
            case PREPARING -> ThemeManager.getInfoBg();
            case READY -> ThemeManager.getSuccessBg();
            case SERVED -> ThemeManager.getBgTertiary();
            case CANCELLED -> ThemeManager.getErrorBg();
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = small ? 10 : 12;

        g2.setColor(getBgColor());
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }

    public OrderStatus getStatus() { return status; }
}