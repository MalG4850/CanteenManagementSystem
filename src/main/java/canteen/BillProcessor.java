package canteen;

import java.util.List;

public abstract class BillProcessor {
    protected String customerName;
    protected String customerMobile;
    protected List<CartItem> cart;

    public BillProcessor(String customerName, String customerMobile, List<CartItem> cart) {
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.cart = cart;
    }

    public abstract void PrintInvoice(int orderId, double base, double gst, double total);
}
