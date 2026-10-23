package canteen;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class MainApp {
    public static void main(String[] args) {
        CanteenAdminService adminService = new CanteenAdminService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=========================================");
            System.out.println("    CANTEEN MANAGEMENT SYSTEM (ADMIN)    ");
            System.out.println("=========================================");
            System.out.println("0. Generate New Bill (Multi-Item)");
            System.out.println("1. Add Food Item to Menu");
            System.out.println("2. Delete Food Item from Menu");
            System.out.println("3. Update Item Price");
            System.out.println("4. View Complete Menu");
            System.out.println("5. Find Order Details by ID");
            System.out.println("6. Total Daily Revenue Report");
            System.out.println("7. Total Monthly Revenue Report");
            System.out.println("8. Delete/Cancel Order Record");
            System.out.println("9. Exit Application Portal");
            System.out.print("\nSelect an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.print("Enter Customer Name: ");
                    String customerName = scanner.nextLine();
                    System.out.print("Enter Mobile No: ");
                    String customerMobile = scanner.nextLine();

                    List<CartItem> cart = new ArrayList<>();
                    adminService.viewMenu();

                    while (true) {
                        System.out.print("\nEnter Item ID to add (or enter -1 to close bill): ");
                        int itemId = scanner.nextInt();
                        if (itemId == -1) break;

                        System.out.print("Enter Quantity: ");
                        int qty = scanner.nextInt();

                        CartItem item = adminService.fetchItemForCart(itemId, qty);
                        if (item != null) {
                            cart.add(item);
                            System.out.println("✔ Added: " + item.getItemName() + " x" + qty);
                        } else {
                            System.out.println("⚠️ Invalid Item ID. Please reference menu list.");
                        }
                    }

                    if (!cart.isEmpty()) {
                        System.out.println("\n⏳ Processing multi-item checkout background processing...");
                        Thread billingThread = new Thread(new BillingTask(customerName, customerMobile, cart));
                        billingThread.start();

                        try { billingThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                    } else {
                        System.out.println("⚠️ Cart empty. Transaction cancelled.");
                    }
                    break;
                case 1:
                    System.out.print("Enter item name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter price: ");
                    double price = scanner.nextDouble();
                    adminService.addItem(name, price);
                    break;
                case 2:
                    System.out.print("Enter item ID to delete: ");
                    int delId = scanner.nextInt();
                    adminService.deleteItem(delId);
                    break;
                case 3:
                    System.out.print("Enter item ID to update: ");
                    int upId = scanner.nextInt();
                    System.out.print("Enter new price: ");
                    double newPrice = scanner.nextDouble();
                    adminService.updatePrice(upId, newPrice);
                    break;
                case 4:
                    adminService.viewMenu();
                    break;
                case 5:
                    System.out.print("Enter Order ID: ");
                    int ordId = scanner.nextInt();
                    adminService.findOrder(ordId);
                    break;
                case 6:
                    adminService.displayTotalDailyRevenue();
                    break;
                case 7:
                    adminService.displayTotalMonthlyRevenue();
                    break;
                case 8:
                    System.out.print("Enter Order ID to delete: ");
                    int delOrderId = scanner.nextInt();
                    adminService.deleteOrder(delOrderId);
                    break;
                case 9:
                    System.out.println("Exiting Admin Portal. System termination complete.");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("⚠️ Invalid selection. Please re-enter.");
            }
        }
    }
}
