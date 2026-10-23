package canteen.ui.dashboard;

import canteen.OrderService;
import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class SplitContentPanel extends JSplitPane {

    private ActiveOrdersPanel leftPanel;
    private SidePanel rightPanel;

    public SplitContentPanel(OrderService orderService, OrderCard.OrderCardListener cardListener) {
        super(JSplitPane.HORIZONTAL_SPLIT);
        setDividerLocation(700);
        setResizeWeight(0.7);
        setBorder(null);
        setOpaque(false);

        leftPanel = new ActiveOrdersPanel(orderService, cardListener);
        rightPanel = new SidePanel();

        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 8));
        leftWrapper.add(leftPanel, BorderLayout.CENTER);

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.setBorder(new javax.swing.border.EmptyBorder(0, 8, 0, 0));
        rightWrapper.add(rightPanel, BorderLayout.CENTER);

        setLeftComponent(leftWrapper);
        setRightComponent(rightWrapper);

        // Style the divider
        setDividerSize(8);
        UIManager.put("SplitPane.dividerFocusColor", ThemeManager.getAccent());
    }

    public ActiveOrdersPanel getLeftPanel() { return leftPanel; }
    public SidePanel getRightPanel() { return rightPanel; }

    public void refresh() {
        leftPanel.refresh();
        rightPanel.refresh();
    }
}