package canteen;

public final class CartItem {
    private final int itemId;
    private final String itemName;
    private int quantity;
    private final double pricePerUnit;

    public CartItem(int itemId, String itemName, int quantity, double pricePerUnit) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (pricePerUnit < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = quantity;
    }
    public double getPricePerUnit() { return pricePerUnit; }
    public double getTotalPrice() { return pricePerUnit * quantity; }

    @Override
    public String toString() {
        return itemName + " x" + quantity + " = ₹" + String.format("%.2f", getTotalPrice());
    }
}