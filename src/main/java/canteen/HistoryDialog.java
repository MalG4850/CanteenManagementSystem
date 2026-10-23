package canteen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public final class HistoryDialog extends JDialog {
    private final OrderService orderService;
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public HistoryDialog(JFrame parent, OrderService orderService) {
        super(parent, "Order History", true);
        this.orderService = orderService;
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        loadHistory();
    }

    private void initComponents() {
        // Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadHistory());
        toolbar.add(refreshBtn);
        toolbar.addSeparator();
        JLabel filterLabel = new JLabel("Filter:");
        toolbar.add(filterLabel);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "PREPARING", "READY", "SERVED", "CANCELLED"});
        statusFilter.addActionListener(e -> loadHistory((String) statusFilter.getSelectedItem()));
        toolbar.add(statusFilter);
        add(toolbar, BorderLayout.NORTH);

        // Table
        historyModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Mobile", "Total", "Status", "Date/Time"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(28);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(80);
        historyTable.getColumnModel().getColumn(3).setMaxWidth(100);
        historyTable.getColumnModel().getColumn(4).setMaxWidth(120);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Double-click to view details
        historyTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = historyTable.getSelectedRow();
                    if (row >= 0) {
                        int orderId = (int) historyModel.getValueAt(row, 0);
                        showOrderDetails(orderId);
                    }
                }
            }
        });

        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Bottom button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadHistory() {
        loadHistory("All");
    }

    private void loadHistory(String statusFilter) {
        List<Order> orders = orderService.getRecentOrders(100);
        historyModel.setRowCount(0);

        for (Order o : orders) {
            if (!"All".equals(statusFilter) && !o.getStatus().name().equals(statusFilter)) continue;
            historyModel.addRow(new Object[]{
                o.getOrderId(),
                o.getCustomerName(),
                o.getCustomerMobile(),
                String.format("₹%.2f", o.getTotalAmount()),
                o.getStatus().getDisplayName(),
                o.getFormattedOrderDate()
            });
        }
    }

    private void showOrderDetails(int orderId) {
        orderService.findById(orderId).ifPresent(order -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Order #").append(order.getOrderId()).append("\n");
            sb.append("Customer: ").append(order.getCustomerName()).append(" (").append(order.getCustomerMobile()).append(")\n");
            sb.append("Date: ").append(order.getFormattedOrderDate()).append("\n");
            sb.append("Status: ").append(order.getStatus().getDisplayName()).append("\n");
            sb.append("QR Token: ").append(order.getQrToken()).append("\n");
            sb.append("WhatsApp Sent: ").append(order.isWhatsappSent() ? "Yes" : "No").append("\n\n");
            sb.append("Items:\n");
            for (OrderItem item : order.getItems()) {
                sb.append("  ").append(item.getItemName())
                  .append(" x").append(item.getQuantity())
                  .append(" = ₹").append(String.format("%.2f", item.getLineTotal()))
                  .append("\n");
            }
            sb.append("\nSubtotal: ₹").append(String.format("%.2f", order.getBaseAmount()));
            sb.append("\nGST (5%): ₹").append(String.format("%.2f", order.getGstAmount()));
            sb.append("\nTotal: ₹").append(String.format("%.2f", order.getTotalAmount()));

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(400, 400));

            JOptionPane.showMessageDialog(this, scroll, "Order #" + orderId + " Details", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}