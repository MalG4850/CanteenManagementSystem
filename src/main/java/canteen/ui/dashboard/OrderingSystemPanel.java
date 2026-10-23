package canteen.ui.dashboard;

import canteen.ui.components.RoundedPanel;
import canteen.ui.components.ActionButton;
import canteen.ui.components.StatusBadge;
import canteen.ui.components.WrapLayout;
import canteen.ui.theme.ThemeManager;
import canteen.*;
import canteen.OrderService.OrderResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderingSystemPanel extends JPanel {

    private final MenuService menuService = new MenuService();
    private final OrderService orderService = new OrderService();
    private final WhatsAppService whatsAppService = new WhatsAppService(AppConfig.getInstance());
    private final List<CartItem> cart = new ArrayList<>();

    private JComboBox<String> categoryCombo;
    private JPanel menuGrid;
    private JScrollPane menuScrollPane;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel subtotalLabel;
    private JLabel gstLabel;
    private JLabel totalLabel;
    private JTextField customerNameField;
    private JTextField customerPhoneField;

    public OrderingSystemPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        loadMenu();
        updateCartDisplay();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("🛒 Ordering System (POS)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getTextPrimary());

        // Category filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterPanel.setOpaque(false);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        catLabel.setForeground(ThemeManager.getTextSecondary());

        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("All");
        for (String cat : menuService.getCategories()) {
            categoryCombo.addItem(cat);
        }
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryCombo.setPreferredSize(new Dimension(180, 36));
        categoryCombo.addActionListener(e -> loadMenu());

        filterPanel.add(catLabel);
        filterPanel.add(categoryCombo);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        // Main content: Split pane with menu (left) and cart (right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Left: Menu Grid
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderLight()),
            "Menu",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            ThemeManager.getTextPrimary()
        ));

        menuGrid = new JPanel(new WrapLayout(16, 16));
        menuGrid.setOpaque(false);

        menuScrollPane = new JScrollPane(menuGrid);
        menuScrollPane.setOpaque(false);
        menuScrollPane.getViewport().setOpaque(false);
        menuScrollPane.setBorder(null);
        menuScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        menuPanel.add(menuScrollPane, BorderLayout.CENTER);

        // Right: Cart + Customer
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(400, 0));
        rightPanel.setBorder(new EmptyBorder(0, 16, 0, 0));

        // Cart Table
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setOpaque(false);
        cartPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderLight()),
            "Cart",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            ThemeManager.getTextPrimary()
        ));

        cartModel = new DefaultTableModel(new String[]{"Item", "Qty", "Price", "Total", ""}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 1 || col == 4; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 1 ? Integer.class : (col == 4 ? JButton.class : Object.class);
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(36);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.getColumnModel().getColumn(1).setMaxWidth(60);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(80);
        cartTable.getColumnModel().getColumn(3).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(40);
        cartTable.getColumn("").setCellRenderer(new RemoveButtonRenderer());
        cartTable.getColumn("").setCellEditor(new RemoveButtonEditor(new JCheckBox()));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        cartTable.getTableHeader().setBackground(ThemeManager.getBgTertiary());

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);
        cartScroll.setBorder(null);
        cartPanel.add(cartScroll, BorderLayout.CENTER);

        // Totals
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 10, 6));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        totalsPanel.add(new JLabel("Subtotal:")); subtotalLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("GST (5%):")); gstLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(gstLabel);
        totalsPanel.add(new JLabel("Total:")); totalLabel = new JLabel("₹0.00", SwingConstants.RIGHT); totalsPanel.add(totalLabel);
        Font totalFont = totalLabel.getFont().deriveFont(Font.BOLD, 18f);
        totalLabel.setFont(totalFont);
        totalLabel.setForeground(ThemeManager.getAccent());
        cartPanel.add(totalsPanel, BorderLayout.SOUTH);

        rightPanel.add(cartPanel, BorderLayout.CENTER);

        // Customer info panel
        JPanel customerPanel = new JPanel(new GridBagLayout());
        customerPanel.setOpaque(false);
        customerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderLight()),
            "Customer Details",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            ThemeManager.getTextPrimary()
        ));
        customerPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; customerPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; customerNameField = new JTextField(15); customerNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13)); customerPanel.add(customerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; customerPanel.add(new JLabel("Mobile:"), gbc);
        gbc.gridx = 1; customerPhoneField = new JTextField(15); customerPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13)); customerPanel.add(customerPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        ActionButton checkoutBtn = new ActionButton("💳 Checkout", ActionButton.Variant.PRIMARY);
        checkoutBtn.setPreferredSize(new Dimension(0, 50));
        checkoutBtn.setFont(checkoutBtn.getFont().deriveFont(Font.BOLD, 16f));
        checkoutBtn.addActionListener(e -> processCheckout());
        customerPanel.add(checkoutBtn, gbc);

        rightPanel.add(customerPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(rightPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadMenu() {
        String selectedCat = (String) categoryCombo.getSelectedItem();
        new SwingWorker<List<canteen.MenuItem>, Void>() {
            @Override
            protected List<canteen.MenuItem> doInBackground() {
                return "All".equals(selectedCat) ? menuService.getAllItems() : menuService.getItemsByCategory(selectedCat);
            }

            @Override
            protected void done() {
                try {
                    List<canteen.MenuItem> items = get();
                    menuGrid.removeAll();
                    for (canteen.MenuItem item : items) {
                        if (!item.isAvailable()) continue;
                        MenuItemCard card = new MenuItemCard(item, (id, name) -> addToCart(id, name, item.getPrice()));
                        menuGrid.add(card);
                    }
                    menuGrid.revalidate();
                    menuGrid.repaint();
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void addToCart(int itemId, String itemName, double price) {
        for (CartItem ci : cart) {
            if (ci.getItemId() == itemId) {
                ci.setQuantity(ci.getQuantity() + 1);
                updateCartDisplay();
                return;
            }
        }
        cart.add(new CartItem(itemId, itemName, 1, price));
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        double subtotal = 0;
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        for (int i = 0; i < cart.size(); i++) {
            CartItem ci = cart.get(i);
            cartModel.addRow(new Object[]{
                ci.getItemName(),
                ci.getQuantity(),
                fmt.format(ci.getPricePerUnit()),
                fmt.format(ci.getTotalPrice()),
                "✕"
            });
            subtotal += ci.getTotalPrice();
        }
        double gst = subtotal * 0.05;
        double total = subtotal + gst;
        subtotalLabel.setText(fmt.format(subtotal));
        gstLabel.setText(fmt.format(gst));
        totalLabel.setText(fmt.format(total));
    }

    private void processCheckout() {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer name", "Missing Info", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return;
        }
        if (!phone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Enter valid 10-digit mobile number", "Invalid Mobile", JOptionPane.WARNING_MESSAGE);
            customerPhoneField.requestFocus();
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subtotal = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        double gst = subtotal * 0.05;
        double total = subtotal + gst;
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        String msg = String.format(
            "Confirm Order:\nCustomer: %s (%s)\nItems: %d\nSubtotal: %s\nGST: %s\nTotal: %s",
            name, phone, cart.size(), fmt.format(subtotal), fmt.format(gst), fmt.format(total)
        );
        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        List<CartItem> cartCopy = new ArrayList<>(cart);
        new SwingWorker<OrderResult, Void>() {
            @Override
            protected OrderResult doInBackground() {
                return orderService.placeOrder(name, phone, cartCopy);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    OrderResult result = get();
                    if (result.isSuccess()) {
                        Order order = result.getOrder();
                        showQrDialog(order);
                        cart.clear();
                        customerNameField.setText("");
                        customerPhoneField.setText("");
                        updateCartDisplay();
                    } else {
                        JOptionPane.showMessageDialog(OrderingSystemPanel.this, "Order failed: " + result.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(OrderingSystemPanel.this, "Order error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showQrDialog(Order order) {
        QrDialog dialog = new QrDialog((JFrame) SwingUtilities.getWindowAncestor(this), order, whatsAppService);
        dialog.setVisible(true);
    }

    // Menu Item Card
    private static class MenuItemCard extends RoundedPanel {
        private final canteen.MenuItem item;
        private final java.util.function.BiConsumer<Integer, String> onAddToCart; // itemId, itemName, price

        public MenuItemCard(canteen.MenuItem item, java.util.function.BiConsumer<Integer, String> onAddToCart) {
            super(new BorderLayout(0, 0));
            this.item = item;
            this.onAddToCart = (id, name) -> onAddToCart.accept(id, name);

            setPanelBackgroundColor(ThemeManager.getBgSecondary());
            setArc(12);
            setShadowOffset(2);
            setDrawShadow(true);
            setDrawBorder(true);
            setBorderColor(ThemeManager.getBorderLight());
            setBorder(new EmptyBorder(12, 12, 12, 12));
            setPreferredSize(new Dimension(200, 160));
            addHoverEffect();

            initComponents();
        }

        private void initComponents() {
            // Item Name
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            nameLabel.setForeground(ThemeManager.getTextPrimary());

            // Category
            JLabel catLabel = new JLabel(item.getCategory());
            catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            catLabel.setForeground(ThemeManager.getTextMuted());

            // Price
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            JLabel priceLabel = new JLabel(fmt.format(item.getPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            priceLabel.setForeground(ThemeManager.getAccent());

            // Add Button
            ActionButton addBtn = new ActionButton("Add to Cart", ActionButton.Variant.PRIMARY);
            addBtn.setPreferredSize(new Dimension(0, 36));
            addBtn.addActionListener(e -> {
                if (onAddToCart != null) onAddToCart.accept(item.getId(), item.getName());
            });

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.setOpaque(false);
            topPanel.add(nameLabel);
            topPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            topPanel.add(catLabel);
            topPanel.add(Box.createVerticalGlue());
            topPanel.add(priceLabel);

            add(topPanel, BorderLayout.CENTER);
            add(addBtn, BorderLayout.SOUTH);
        }
    }

    // Remove button renderer/editor for cart
    private static class RemoveButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        RemoveButtonRenderer() { setText("✕"); setMargin(new Insets(0, 4, 0, 4)); setForeground(ThemeManager.getError()); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setFont(new Font("Segoe UI", Font.BOLD, 14)); }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) { return this; }
    }

    private class RemoveButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        RemoveButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("✕");
            button.setForeground(ThemeManager.getError());
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            clickedRow = row;
            return button;
        }

        @Override public Object getCellEditorValue() {
            if (clickedRow >= 0 && clickedRow < cart.size()) {
                cart.remove(clickedRow);
                updateCartDisplay();
            }
            return "✕";
        }

        @Override public boolean stopCellEditing() { return super.stopCellEditing(); }
    }
}