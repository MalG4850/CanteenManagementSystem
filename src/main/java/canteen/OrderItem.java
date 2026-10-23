package canteen;

public final class OrderItem {
    private final int id;
    private final int orderId;
    private final int itemId;
    private final String itemName;
    private final int quantity;
    private final double itemPrice;

    public OrderItem(int id, int orderId, int itemId, String itemName, int quantity, double itemPrice) {
        this.id = id;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getItemPrice() { return itemPrice; }
    public double getLineTotal() { return quantity * itemPrice; }
}