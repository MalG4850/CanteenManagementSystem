package canteen;

import java.util.List;

public class CanteenBillPrinter extends BillProcessor {

    public CanteenBillPrinter(String customerName, String customerMobile, List<CartItem> cart) {
        super(customerName, customerMobile, cart);
    }

    @Override
    public void PrintInvoice(int orderId, double base, double gst, double total) {
        System.out.println("\n=========================================");
        System.out.println("          CANTEEN TAX INVOICE            ");
        System.out.println("=========================================");
        System.out.printf("Order ID: #%-10d\n", orderId);
        System.out.println("Customer: " + customerName + " (" + customerMobile + ")");
        System.out.println("-----------------------------------------");
        System.out.printf("%-18s %-5s %-8s %-8s\n", "Item", "Qty", "Rate", "Total");
        System.out.println("-----------------------------------------");
        
        for (CartItem item : cart) {
            System.out.printf("%-18s %-5d ₹%-7.2f ₹%-7.2f\n", 
                item.getItemName(), item.getQuantity(), item.getPricePerUnit(), item.getTotalPrice());
        }
        
        System.out.println("-----------------------------------------");
        System.out.printf("Sub-Total:                     ₹%.2f\n", base);
        System.out.printf("CGST + SGST (5%%):              ₹%.2f\n", gst);
        System.out.println("-----------------------------------------");
        System.out.printf("GRAND TOTAL PAYABLE:           ₹%.2f\n", total);
        System.out.println("=========================================\n");
    }
}
