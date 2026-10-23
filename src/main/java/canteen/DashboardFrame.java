package canteen;

import canteen.ui.dashboard.*;
import canteen.ui.sidebar.NavItem;
import canteen.ui.sidebar.SidebarPanel;
import canteen.ui.topbar.TopBarPanel;
import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

public class DashboardFrame extends JFrame {

    private SidebarPanel sidebar;
    private TopBarPanel topBar;
    private DashboardPanel dashboardPanel;
    private OrderingSystemPanel orderingSystemPanel;
    private MenuManagementPanel menuManagementPanel;
    private OrdersPanel ordersPanel;
    private KitchenDisplayPanel kitchenDisplayPanel;
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private String currentCard = "DASHBOARD";

    public DashboardFrame() {
        super(AppConfig.getInstance().getCanteenName() + " - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 900);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Apply theme early
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);

        initComponents();
        setupWindowListener();
    }

    private void initComponents() {
        // Sidebar
        sidebar = new SidebarPanel(target -> handleNavigation(target));

        // Top Bar
        topBar = new TopBarPanel(new TopBarPanel.TopBarListener() {
            @Override public void onSearch(String query) { handleSearch(query); }
            @Override public void onNotifications() { showNotifications(); }
            @Override public void onProfile() { showProfile(); }
            @Override public void onThemeToggle() { /* handled in TopBarPanel */ }
            @Override public void onNavigate(NavItem.Target target) { handleNavigation(target); }
        });

        // Center content with CardLayout for page switching
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBackground(ThemeManager.getBgPrimary());

        // Create all panels
        OrderService orderService = new OrderService();
        dashboardPanel = new DashboardPanel(orderService);
        orderingSystemPanel = new OrderingSystemPanel();
        menuManagementPanel = new MenuManagementPanel();
        ordersPanel = new OrdersPanel(orderService);
        kitchenDisplayPanel = new KitchenDisplayPanel(orderService);

        centerPanel.add(dashboardPanel, "DASHBOARD");
        centerPanel.add(orderingSystemPanel, "ORDERING_SYSTEM");
        centerPanel.add(menuManagementPanel, "MENU_MANAGEMENT");
        centerPanel.add(ordersPanel, "ORDERS");
        centerPanel.add(kitchenDisplayPanel, "KITCHEN_DISPLAY");

        // Main layout
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(ThemeManager.getBgPrimary());
        mainContent.add(topBar, BorderLayout.NORTH);
        mainContent.add(centerPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void handleNavigation(NavItem.Target target) {
        String cardName = target.name();
        cardLayout.show(centerPanel, cardName);
        currentCard = cardName;

        switch (target) {
            case DASHBOARD -> topBar.setTitle("Dashboard");
            case ORDERING_SYSTEM -> topBar.setTitle("Ordering System");
            case MENU_MANAGEMENT -> {
                topBar.setTitle("Menu Management");
                menuManagementPanel.loadMenu();
            }
            case ORDERS -> {
                topBar.setTitle("Orders History");
                ordersPanel.refresh();
            }
            case KITCHEN_DISPLAY -> {
                topBar.setTitle("Kitchen Display");
                kitchenDisplayPanel.refresh();
            }
            case REPORTS -> showReportsDialog();
            case SETTINGS -> showSettingsDialog();
        }
        sidebar.setSelectedTarget(target);
    }

    private void handleSearch(String query) {
        // Filter dashboard content based on search
        System.out.println("Search: " + query);
    }

    private void showNotifications() {
        JOptionPane.showMessageDialog(this, "Notifications feature coming soon", "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showProfile() {
        // Profile popup is handled in TopBarPanel
    }

    private void showReportsDialog() {
        OrderService orderService = new OrderService();
        String msg = String.format(
            "Daily Revenue: %s\nDaily Orders: %d\nMonthly Revenue: %s\nMonthly Orders: %d",
            java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN")).format(orderService.getDailyRevenue()),
            orderService.getDailyOrderCount(),
            java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN")).format(orderService.getMonthlyRevenue()),
            orderService.getMonthlyOrderCount()
        );
        JOptionPane.showMessageDialog(this, msg, "Reports", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettingsDialog() {
        JOptionPane.showMessageDialog(this, "Settings feature coming soon", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupKeyboardShortcuts() {
        int[] keys = { java.awt.event.KeyEvent.VK_1, java.awt.event.KeyEvent.VK_2, java.awt.event.KeyEvent.VK_3,
                       java.awt.event.KeyEvent.VK_4, java.awt.event.KeyEvent.VK_5, java.awt.event.KeyEvent.VK_6,
                       java.awt.event.KeyEvent.VK_7 };
        NavItem.Target[] targets = NavItem.Target.values();

        for (int i = 0; i < keys.length && i < targets.length; i++) {
            final int idx = i;
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(keys[i], java.awt.event.InputEvent.CTRL_DOWN_MASK), "nav" + i);
            getRootPane().getActionMap().put("nav" + i, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    handleNavigation(targets[idx]);
                }
            });
        }

        // F5 for refresh
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                refreshCurrentPanel();
            }
        });
    }

    private void refreshCurrentPanel() {
        String currentCard = getCurrentCardName();
        switch (currentCard) {
            case "DASHBOARD" -> dashboardPanel.refreshAll();
            case "ORDERS" -> ordersPanel.refresh();
            case "KITCHEN_DISPLAY" -> kitchenDisplayPanel.refresh();
            case "MENU_MANAGEMENT" -> menuManagementPanel.loadMenu();
        }
    }

    private String getCurrentCardName() {
        // CardLayout doesn't expose current card, so we track it via sidebar selection
        NavItem selected = sidebar.getNavList().getSelectedValue();
        if (selected != null) {
            return selected.getTarget().name();
        }
        return "DASHBOARD";
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                dashboardPanel.stopAutoRefresh();
                kitchenDisplayPanel.stopAutoRefresh();
                ordersPanel.stopAutoRefresh();
            }
        });
    }
}