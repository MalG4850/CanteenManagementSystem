package canteen;

public final class MenuItem {
    private final int id;
    private final String name;
    private final double price;
    private final String category;
    private final boolean available;

    public MenuItem(int id, String name, double price, String category, boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return name + " - ₹" + String.format("%.2f", price);
    }
}