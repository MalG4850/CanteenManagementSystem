package canteen.ui.dashboard;

import canteen.ui.theme.ThemeManager;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class FilterTabGroup extends JPanel {

    public interface FilterListener {
        void onFilterChange(OrderStatus status);
    }

    private final FilterListener listener;
    private final List<TabButton> tabs = new ArrayList<>();
    private OrderStatus currentFilter = null; // null = All

    public FilterTabGroup(FilterListener listener) {
        this.listener = listener;
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 0, 8, 0));

        createTabs();
    }

    private void createTabs() {
        OrderStatus[] statuses = { null, OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY };
        String[] labels = { "All", "Pending", "Preparing", "Ready" };

        for (int i = 0; i < statuses.length; i++) {
            final OrderStatus status = statuses[i];
            final String label = labels[i];
            TabButton tab = new TabButton(label, status, i == 0);
            tab.addActionListener(e -> {
                setSelectedFilter(status);
                if (listener != null) listener.onFilterChange(status);
            });
            tabs.add(tab);
            add(tab);
        }
    }

    public void setSelectedFilter(OrderStatus status) {
        currentFilter = status;
        for (TabButton tab : tabs) {
            tab.setSelected(tab.getStatus() == status);
        }
        repaint();
    }

    public OrderStatus getCurrentFilter() { return currentFilter; }

    private class TabButton extends JButton {
        private final OrderStatus status;
        private boolean selected = false;

        public TabButton(String text, OrderStatus status, boolean selected) {
            super(text);
            this.status = status;
            this.selected = selected;
            init();
        }

        private void init() {
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 14, 6, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
            updateAppearance();
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateAppearance();
        }

        public OrderStatus getStatus() { return status; }

        private void updateAppearance() {
            if (selected) {
                setForeground(ThemeManager.getAccent());
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(ThemeManager.getTextSecondary());
                setFont(getFont().deriveFont(Font.PLAIN));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 16;

            if (selected) {
                g2.setColor(ThemeManager.getAccentBg());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            } else if (getModel().isRollover()) {
                g2.setColor(ThemeManager.getBgHover());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}