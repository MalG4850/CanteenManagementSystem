package canteen;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Order {
    private final int orderId;
    private final String customerName;
    private final String customerMobile;
    private final double baseAmount;
    private final double gstAmount;
    private final double totalAmount;
    private final OrderStatus status;
    private final String qrToken;
    private final boolean whatsappSent;
    private final Timestamp orderDate;
    private final Timestamp updatedAt;
    private final String shortCode;
    private final LocalDate codeDate;
    private final List<OrderItem> items;

    public Order(int orderId, String customerName, String customerMobile,
                 double baseAmount, double gstAmount, double totalAmount,
                 OrderStatus status, String qrToken, boolean whatsappSent,
                 Timestamp orderDate, Timestamp updatedAt,
                 String shortCode, LocalDate codeDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.baseAmount = baseAmount;
        this.gstAmount = gstAmount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.qrToken = qrToken;
        this.whatsappSent = whatsappSent;
        this.orderDate = orderDate;
        this.updatedAt = updatedAt;
        this.shortCode = shortCode;
        this.codeDate = codeDate;
        this.items = new ArrayList<>();
    }

    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public double getBaseAmount() { return baseAmount; }
    public double getGstAmount() { return gstAmount; }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getQrToken() { return qrToken; }
    public boolean isWhatsappSent() { return whatsappSent; }
    public Timestamp getOrderDate() { return orderDate; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }

    public void addItem(OrderItem item) { items.add(item); }

    public String getFormattedOrderDate() {
        return orderDate != null ? orderDate.toString().replace("T", " ").substring(0, 19) : "";
    }

    public String getShortCode() { return shortCode; }
    public LocalDate getCodeDate() { return codeDate; }
}