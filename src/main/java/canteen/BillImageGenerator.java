package canteen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class BillImageGenerator {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 1100;
    private static final int MARGIN = 40;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 14);
    private static final Font FOOTER_FONT = new Font("SansSerif", Font.ITALIC, 12);
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private BillImageGenerator() {}

    public static BufferedImage createBillImage(Order order, List<OrderItem> items) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);

        int y = MARGIN;

        // Title
        g.setFont(TITLE_FONT);
        String title = AppConfig.getInstance().getCanteenName();
        drawCenteredString(g, title, WIDTH / 2, y);
        y += 40;

        // Order meta
        g.setFont(HEADER_FONT);
        drawLeft(g, "Order ID: " + order.getOrderId(), MARGIN, y); y += 22;
        drawLeft(g, "Date: " + order.getFormattedOrderDate(), MARGIN, y); y += 22;
        drawLeft(g, "Customer: " + order.getCustomerName(), MARGIN, y); y += 22;
        drawLeft(g, "Mobile: " + order.getCustomerMobile(), MARGIN, y); y += 22;
        drawLeft(g, "Short Code: " + order.getShortCode(), MARGIN, y); y += 30;

        // Table header
        g.setFont(HEADER_FONT);
        int col1 = MARGIN;
        int col2 = 420;
        int col3 = 560;
        int col4 = 680;
        drawLeft(g, "Item", col1, y);
        drawLeft(g, "Qty", col2, y);
        drawLeft(g, "Unit", col3, y);
        drawRight(g, "Total", col4 + 80, y);
        y += 4;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        y += 20;

        // Items
        g.setFont(MONO_FONT);
        for (OrderItem it : items) {
            drawLeft(g, truncate(it.getItemName(), 30), col1, y);
            drawCenteredString(g, String.valueOf(it.getQuantity()), col2 + 30, y);
            drawRight(g, CURRENCY.format(it.getItemPrice()), col3 + 80, y);
            drawRight(g, CURRENCY.format(it.getLineTotal()), col4 + 80, y);
            y += 22;
        }

        y += 10;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        y += 20;

        // Totals
        g.setFont(HEADER_FONT);
        drawRight(g, "Subtotal:", col4, y);
        drawRight(g, CURRENCY.format(order.getBaseAmount()), col4 + 80, y); y += 22;
        drawRight(g, "GST (5%):", col4, y);
        drawRight(g, CURRENCY.format(order.getGstAmount()), col4 + 80, y); y += 22;
        g.setFont(TITLE_FONT);
        drawRight(g, "Total:", col4, y);
        drawRight(g, CURRENCY.format(order.getTotalAmount()), col4 + 80, y);
        y += 40;

        // QR code at bottom left
        String trackingLink = AppConfig.getInstance().getBaseUrl() + "/kitchen?token=" + order.getQrToken();
        BufferedImage qr = QrGenerator.generate(trackingLink, 200);
        g.drawImage(qr, MARGIN, HEIGHT - MARGIN - 200, 200, 200, null);

        // Footer
        g.setFont(FOOTER_FONT);
        drawCenteredString(g, "Thank you for your order!", WIDTH / 2, HEIGHT - MARGIN - 10);

        g.dispose();
        return img;
    }

    private static void drawLeft(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    private static void drawRight(Graphics2D g, String text, int rightX, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, rightX - w, y);
    }

    private static void drawCenteredString(Graphics2D g, String text, int centerX, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - w / 2, y);
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}