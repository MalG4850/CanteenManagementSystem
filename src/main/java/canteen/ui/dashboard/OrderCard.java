package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.StatusBadge;
import canteen.ui.components.ActionButton;
import canteen.ui.theme.ThemeManager;
import canteen.Order;
import canteen.OrderStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderCard extends RoundedPanel {

    private final Order order;
    private final OrderCardListener listener;

    public interface OrderCardListener {
        void onAction(Order order, OrderStatus nextStatus);
    }

    public OrderCard(Order order, OrderCardListener listener) {
        super(new BorderLayout(0, 0));
        this.order = order;
        this.listener = listener;

        setPanelBackgroundColor(ThemeManager.getBgSecondary());
        setArc(12);
        setShadowOffset(2);
        setDrawShadow(true);
        setDrawBorder(true);
        setBorderColor(ThemeManager.getBorderLight());
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(320, 0));

        initComponents();
    }

    private void initComponents() {
        // Header: Order ID, Status Badge, Time
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel orderIdLabel = new JLabel("#" + order.getOrderId());
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        orderIdLabel.setForeground(ThemeManager.getTextPrimary());

        StatusBadge statusBadge = new StatusBadge(order.getStatus());
        statusBadge.setBorder(new EmptyBorder(0, 0, 0, 8));

        JLabel timeLabel = new JLabel(order.getFormattedOrderDate());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(ThemeManager.getTextMuted());

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(orderIdLabel);
        leftHeader.add(statusBadge);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);

        // Customer Info
        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.setOpaque(false);
        customerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel customerLabel = new JLabel(order.getCustomerName());
        customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerLabel.setForeground(ThemeManager.getTextPrimary());

        JLabel mobileLabel = new JLabel(order.getCustomerMobile());
        mobileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        mobileLabel.setForeground(ThemeManager.getTextMuted());

        JPanel customerInfo = new JPanel();
        customerInfo.setLayout(new BoxLayout(customerInfo, BoxLayout.Y_AXIS));
        customerInfo.setOpaque(false);
        customerInfo.add(customerLabel);
        customerInfo.add(mobileLabel);

        customerPanel.add(customerInfo, BorderLayout.WEST);

        // Items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

        for (canteen.OrderItem item : order.getItems()) {
            JPanel itemRow = new JPanel(new BorderLayout());
            itemRow.setOpaque(false);
            itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

            JLabel itemName = new JLabel(item.getItemName() + " x" + item.getQuantity());
            itemName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemName.setForeground(ThemeManager.getTextSecondary());

            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            JLabel itemTotal = new JLabel(fmt.format(item.getLineTotal()));
            itemTotal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemTotal.setForeground(ThemeManager.getTextSecondary());

            itemRow.add(itemName, BorderLayout.WEST);
            itemRow.add(itemTotal, BorderLayout.EAST);
            itemsPanel.add(itemRow);
        }

        // Footer: Total + Action Button
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        JLabel totalLabel = new JLabel("Total: " + currencyFmt.format(order.getTotalAmount()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(ThemeManager.getTextPrimary());

        OrderStatus nextStatus = getNextStatus(order.getStatus());
        ActionButton actionBtn = null;
        if (nextStatus != null) {
            actionBtn = new ActionButton(getActionButtonText(nextStatus), ActionButton.Variant.PRIMARY);
            actionBtn.setPreferredSize(new Dimension(140, 36));
            actionBtn.addActionListener(e -> {
                if (listener != null) listener.onAction(order, nextStatus);
            });
        } else {
            JLabel servedLabel = new JLabel("✓ Served");
            servedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            servedLabel.setForeground(ThemeManager.getSuccess());
            footerPanel.add(servedLabel, BorderLayout.EAST);
        }

        footerPanel.add(totalLabel, BorderLayout.WEST);
        if (actionBtn != null) footerPanel.add(actionBtn, BorderLayout.EAST);

        // Assemble
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);
        centerContent.add(headerPanel);
        centerContent.add(customerPanel);
        centerContent.add(itemsPanel);
        centerContent.add(Box.createVerticalGlue());
        centerContent.add(footerPanel);

        add(centerContent, BorderLayout.CENTER);
    }

    private OrderStatus getNextStatus(OrderStatus current) {
        return switch (current) {
            case PENDING -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.READY;
            case READY -> OrderStatus.SERVED;
            default -> null;
        };
    }

    private String getActionButtonText(OrderStatus nextStatus) {
        return switch (nextStatus) {
            case PREPARING -> "Start Preparing";
            case READY -> "Mark Ready";
            case SERVED -> "Mark Served";
            default -> "Next";
        };
    }

    public Order getOrder() { return order; }
}