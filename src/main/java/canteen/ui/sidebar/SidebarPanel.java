package canteen.ui.sidebar;

import canteen.ui.theme.ThemeManager;
import canteen.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {

    public interface NavigationListener {
        void onNavigate(NavItem.Target target);
    }

    private NavigationListener listener;
    private JList<NavItem> navList;
    private DefaultListModel<NavItem> listModel;
    private JLabel logoLabel;
    private JLabel canteenNameLabel;
    private JPanel userPanel;
    private JLabel userRoleLabel;
    private JLabel userNameLabel;
    private JButton logoutBtn;

    public SidebarPanel(NavigationListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(240, 0));
        setMinimumSize(new Dimension(240, 0));
        setMaximumSize(new Dimension(240, Integer.MAX_VALUE));
        setBackground(ThemeManager.getBgSecondary());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getBorderLight()));

        initComponents();
        setupEventHandlers();
    }

    private void initComponents() {
        // Logo / Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        logoLabel = new JLabel("☕");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        canteenNameLabel = new JLabel(AppConfig.getInstance().getCanteenName());
        canteenNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        canteenNameLabel.setForeground(ThemeManager.getTextPrimary());
        canteenNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(canteenNameLabel);

        // Navigation List
        listModel = new DefaultListModel<>();
        for (NavItem item : NavItem.defaultItems()) {
            listModel.addElement(item);
        }

        navList = new JList<>(listModel);
        navList.setCellRenderer(new NavItemRenderer());
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setSelectedIndex(0);
        navList.setOpaque(false);
        navList.setBackground(ThemeManager.getBgSecondary());
        navList.setBorder(new EmptyBorder(8, 8, 8, 8));
        navList.setFixedCellHeight(48);
        navList.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(navList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // User Panel (bottom)
        userPanel = new JPanel(new BorderLayout(8, 4));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        userNameLabel = new JLabel("Cashier");
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userNameLabel.setForeground(ThemeManager.getTextPrimary());
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userRoleLabel = new JLabel("Staff");
        userRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRoleLabel.setForeground(ThemeManager.getTextMuted());
        userRoleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfo.add(userNameLabel);
        userInfo.add(Box.createRigidArea(new Dimension(0, 2)));
        userInfo.add(userRoleLabel);

        logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setForeground(ThemeManager.getTextMuted());
        logoutBtn.setBorder(null);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                logoutBtn.setForeground(ThemeManager.getError());
            }
            @Override public void mouseExited(MouseEvent e) {
                logoutBtn.setForeground(ThemeManager.getTextMuted());
            }
        });

        userPanel.add(userInfo, BorderLayout.CENTER);
        userPanel.add(logoutBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(userPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        navList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                NavItem selected = navList.getSelectedValue();
                if (selected != null && listener != null) {
                    listener.onNavigate(selected.getTarget());
                }
            }
        });
    }

    public void setSelectedTarget(NavItem.Target target) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getTarget() == target) {
                navList.setSelectedIndex(i);
                navList.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public JList<NavItem> getNavList() { return navList; }
    public void setUserName(String name) { userNameLabel.setText(name); }
    public void setUserRole(String role) { userRoleLabel.setText(role); }
}