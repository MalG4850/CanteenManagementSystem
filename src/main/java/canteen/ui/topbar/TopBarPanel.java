package canteen.ui.topbar;

import canteen.ui.components.ActionButton;
import canteen.ui.sidebar.NavItem;
import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopBarPanel extends JPanel {

    public interface TopBarListener {
        void onSearch(String query);
        void onNotifications();
        void onProfile();
        void onThemeToggle();
        void onNavigate(NavItem.Target target);
    }

    private TopBarListener listener;
    private JLabel titleLabel;
    private ActionButton notificationsBtn;
    private ActionButton profileBtn;
    private ActionButton themeToggleBtn;
    private JPopupMenu profileMenu;
    private boolean isDarkTheme = false;

    public TopBarPanel(TopBarListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 56));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        setMinimumSize(new Dimension(0, 56));
        setBackground(ThemeManager.getBgSecondary());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderLight()),
            new EmptyBorder(0, 20, 0, 20)
        ));

        initComponents();
    }

    private void initComponents() {
        // Center: Title / Breadcrumbs
        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeManager.getTextPrimary());
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel);

        // Right: Actions
        notificationsBtn = new ActionButton("🔔", ActionButton.Variant.GHOST);
        notificationsBtn.setPreferredSize(new Dimension(40, 40));
        notificationsBtn.setToolTipText("Notifications");

        themeToggleBtn = new ActionButton("🌙", ActionButton.Variant.GHOST);
        themeToggleBtn.setPreferredSize(new Dimension(40, 40));
        themeToggleBtn.setToolTipText("Toggle Dark/Light Mode");

        profileBtn = new ActionButton("👤", ActionButton.Variant.GHOST);
        profileBtn.setPreferredSize(new Dimension(40, 40));
        profileBtn.setToolTipText("Profile");

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightPanel.setOpaque(false);
        rightPanel.add(notificationsBtn);
        rightPanel.add(themeToggleBtn);
        rightPanel.add(profileBtn);

        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setupProfileMenu();
        setupEventHandlers();
    }

    private void setupProfileMenu() {
        profileMenu = new JPopupMenu();
        profileMenu.setBorder(BorderFactory.createLineBorder(ThemeManager.getBorderLight()));
        profileMenu.setBackground(ThemeManager.getBgSecondary());

        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profileItem.addActionListener(e -> { if (listener != null) listener.onProfile(); });

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        settingsItem.addActionListener(e -> { if (listener != null) listener.onNavigate(NavItem.Target.SETTINGS); });

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeManager.getBorderLight());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoutItem.setForeground(ThemeManager.getError());
        logoutItem.addActionListener(e -> System.exit(0));

        profileMenu.add(profileItem);
        profileMenu.add(settingsItem);
        profileMenu.add(sep);
        profileMenu.add(logoutItem);
    }

    private void setupEventHandlers() {
        notificationsBtn.addActionListener(e -> {
            if (listener != null) listener.onNotifications();
        });

        themeToggleBtn.addActionListener(e -> {
            isDarkTheme = !isDarkTheme;
            ThemeManager.setTheme(isDarkTheme ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
            themeToggleBtn.setText(isDarkTheme ? "☀️" : "🌙");
            ThemeManager.refreshAllFrames();
        });

        profileBtn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                profileMenu.show(profileBtn, 0, profileBtn.getHeight());
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) profileMenu.show(profileBtn, 0, profileBtn.getHeight());
            }
        });
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public Object getSearchField() { return null; }
    public ActionButton getNotificationsBtn() { return notificationsBtn; }
    public ActionButton getThemeToggleBtn() { return themeToggleBtn; }
}