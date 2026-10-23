package canteen.ui.sidebar;

public final class NavItem {

    public enum Target { DASHBOARD, ORDERING_SYSTEM, MENU_MANAGEMENT, ORDERS, KITCHEN_DISPLAY, REPORTS, SETTINGS }

    private final Target target;
    private final String icon;
    private final String label;

    public NavItem(Target target, String icon, String label) {
        this.target = target;
        this.icon = icon;
        this.label = label;
    }

    public Target getTarget() { return target; }
    public String getIcon() { return icon; }
    public String getLabel() { return label; }

    public static NavItem[] defaultItems() {
        return new NavItem[] {
            new NavItem(Target.DASHBOARD, "🏠", "Dashboard"),
            new NavItem(Target.ORDERING_SYSTEM, "🛒", "Ordering System"),
            new NavItem(Target.MENU_MANAGEMENT, "📋", "Menu Management"),
            new NavItem(Target.ORDERS, "🧾", "Orders"),
            new NavItem(Target.KITCHEN_DISPLAY, "🍽️", "Kitchen Display"),
            new NavItem(Target.REPORTS, "📊", "Reports"),
            new NavItem(Target.SETTINGS, "⚙️", "Settings")
        };
    }
}