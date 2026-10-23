package canteen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import canteen.MenuModel;
import canteen.MenuService;
import canteen.OrderService;
import canteen.WhatsAppService;
import canteen.AppConfig;
import canteen.CartItem;
import canteen.MenuItem;
import canteen.Order;
import canteen.OrderService.OrderResult;
import canteen.QrDialogController;
import canteen.HistoryController;
import canteen.MenuManagementController;
import canteen.CategoryManagementController;
import canteen.SettingsController;

public class CashierController {
    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> colItem;
    @FXML private TableColumn<MenuItem, String> colCategory;
    @FXML private TableColumn<MenuItem, Double> colPrice;
    @FXML private TableColumn<MenuItem, Void> colAdd;

    @FXML private ListView<CartItem> cartList;
    @FXML private TextField customerMobileField;
    @FXML private Button placeOrderBtn;
    @FXML private Button clearCartBtn;
    @FXML private Label statusLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label todaysRevenueLabel;
    @FXML private TilePane dashboardTilePane;

    private final MenuService menuService = new MenuService();
    private final OrderService orderService = new OrderService();
    private final WhatsAppService whatsAppService = new WhatsAppService(AppConfig.getInstance());
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAdd.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Add");
            {
                btn.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    showQuantityDialog(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });

        cartList.setItems(cart);
        cartList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CartItem ci, boolean empty) {
                super.updateItem(ci, empty);
                if (empty || ci == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(ci.getQuantity() + " x " + ci.getItemName() + " = " + String.format("%.2f", ci.getTotalPrice()));
                    Button removeBtn = new Button("Remove");
                    removeBtn.setOnAction(ev -> {
                        cart.remove(ci);
                        updateStatus();
                    });
                    HBox hbox = new HBox(10, lbl, removeBtn);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(hbox);
                }
            }
        });

        // Bind menu table to shared observable list
        menuTable.setItems(MenuModel.getInstance().getItems());

        refreshDashboard();
    }

    private void refreshDashboard() {
        // Total Sales (monthly revenue)
        double monthlyRev = orderService.getMonthlyRevenue();
        totalSalesLabel.setText(String.format("₹%.2f", monthlyRev));

        // Pending Orders count
        int pendingCount = orderService.getPendingOrders().size();
        pendingOrdersLabel.setText(String.valueOf(pendingCount));

        // Today's Revenue
        double dailyRev = orderService.getDailyRevenue();
        todaysRevenueLabel.setText(String.format("₹%.2f", dailyRev));
    }

    private void loadMenu() {
        List<MenuItem> items = menuService.getAllItems();
        menuTable.setItems(FXCollections.observableArrayList(items));
    }

    private void addToCart(MenuItem item, int quantity) {
        if (quantity <= 0) return;
        for (CartItem ci : cart) {
            if (ci.getItemId() == item.getId()) {
                ci.setQuantity(ci.getQuantity() + quantity);
                cartList.refresh();
                updateStatus();
                return;
            }
        }
        cart.add(new CartItem(item.getId(), item.getName(), quantity, item.getPrice()));
        updateStatus();
    }

    private void showQuantityDialog(MenuItem item) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add " + item.getName());
        dialog.setHeaderText("Enter quantity for " + item.getName());

        TextField qtyField = new TextField("1");
        qtyField.setPromptText("Quantity");
        qtyField.selectAll();

        // Only allow digits
        qtyField.textProperty().addListener((obs, old, nw) -> {
            if (!nw.matches("\\d*")) qtyField.setText(old);
        });

        dialog.getDialogPane().setContent(qtyField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String txt = qtyField.getText().trim();
                return txt.isEmpty() ? 0 : Integer.parseInt(txt);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(q -> {
            if (q > 0) addToCart(item, q);
        });
    }

    @FXML
    private void clearCart() {
        cart.clear();
        updateStatus();
    }

    private void updateStatus() {
        double total = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
        statusLabel.setText("Cart total: " + String.format("%.2f", total) + " | Items: " + cart.size());
    }

    @FXML
    private void placeOrder() {
        String mobile = customerMobileField.getText().trim();
        if (mobile.isEmpty()) {
            showAlert("Error", "Customer mobile is required.");
            return;
        }
        if (cart.isEmpty()) {
            showAlert("Error", "Cart is empty.");
            return;
        }
        String name = "Walk-in"; // default name
        try {
            OrderService.OrderResult result = orderService.placeOrder(name, mobile, new java.util.ArrayList<>(cart));
            if (result.isSuccess()) {
                Order order = result.getOrder();
                // Send WhatsApp media invoice
                String mediaUrl = AppConfig.getInstance().getBaseUrl() + "/bill/" + order.getOrderId() + ".png";
                whatsAppService.sendInvoiceMedia(order, mobile, mediaUrl);
                openQrDialog(order);
                clearCart();
                customerMobileField.clear();
                refreshDashboard();
            } else {
                showAlert("Error", result.getError());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to place order: " + ex.getMessage());
        }
    }

    private void openQrDialog(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/qr_dialog.fxml"));
            Parent root = loader.load();
            QrDialogController controller = loader.getController();
            controller.setOrder(order);
            Stage stage = new Stage();
            stage.setTitle("QR Code - Order #" + order.getOrderId());
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open QR dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleHistory() {
        openHistoryDialog();
    }

    @FXML
    private void handleManageMenu() {
        openManageMenuDialog();
    }

    private void openManageMenuDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu_mgmt.fxml"));
            Parent root = loader.load();
            MenuManagementController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Manage Menu");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Manage Menu dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageCategories() {
        openManageCategoriesDialog();
    }

    @FXML
    private void handleSettings() {
        openSettingsDialog();
    }

    private void openManageCategoriesDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/category_mgmt.fxml"));
            Parent root = loader.load();
            CategoryManagementController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Manage Categories");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Manage Categories dialog: " + e.getMessage());
        }
    }

    private void openSettingsDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings_dialog.fxml"));
            Parent root = loader.load();
            SettingsController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Settings dialog: " + e.getMessage());
        }
    }

    private void openHistoryDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/history_dialog.fxml"));
            Parent root = loader.load();
            HistoryController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Order History");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open History dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}