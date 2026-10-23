package canteen.ui.dashboard;

import canteen.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class MetricCardsPanel extends JPanel {

    private final MetricCard totalSalesCard;
    private final MetricCard pendingOrdersCard;
    private final MetricCard todayRevenueCard;

    public MetricCardsPanel() {
        setLayout(new GridLayout(1, 3, 16, 0));
        setOpaque(false);
        setBorder(new javax.swing.border.EmptyBorder(0, 0, 16, 0));

        totalSalesCard = new MetricCard("Total Sales", "💰", "₹0.00", "+0%", ThemeManager.getSuccess());
        pendingOrdersCard = new MetricCard("Pending Orders", "⏳", "0", "0 new", ThemeManager.getWarning());
        todayRevenueCard = new MetricCard("Today's Revenue", "📈", "₹0.00", "+0%", ThemeManager.getSuccess());

        add(totalSalesCard);
        add(pendingOrdersCard);
        add(todayRevenueCard);
    }

    public void updateTotalSales(String value, String trend) {
        totalSalesCard.setValue(value);
        totalSalesCard.setTrend(trend, ThemeManager.getSuccess());
    }

    public void updatePendingOrders(String value, String trend) {
        pendingOrdersCard.setValue(value);
        pendingOrdersCard.setTrend(trend, ThemeManager.getWarning());
    }

    public void updateTodayRevenue(String value, String trend) {
        todayRevenueCard.setValue(value);
        todayRevenueCard.setTrend(trend, ThemeManager.getSuccess());
    }

    public MetricCard getTotalSalesCard() { return totalSalesCard; }
    public MetricCard getPendingOrdersCard() { return pendingOrdersCard; }
    public MetricCard getTodayRevenueCard() { return todayRevenueCard; }
}