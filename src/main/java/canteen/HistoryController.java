package canteen;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class HistoryController {
    @FXML private ComboBox<OrderStatus> statusFilter;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, String> colMobile;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, Void> colActions;
    @FXML private Label summaryLabel;

    private final OrderService orderService = new OrderService();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private Stage dialogStage;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colMobile.setCellValueFactory(new PropertyValueFactory<>("customerMobile"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().getDisplayName()));
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFormattedOrderDate()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View QR");
            private final Button deleteBtn = new Button("Delete");
            private final ComboBox<OrderStatus> statusBox = new ComboBox<>();
            {
                viewBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    openQrDialog(order);
                });
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
                statusBox.setItems(FXCollections.observableArrayList(OrderStatus.values()));
                statusBox.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    OrderStatus newStatus = statusBox.getValue();
                    if (newStatus != null && newStatus != order.getStatus()) {
                        boolean ok = orderService.setOrderStatus(order.getOrderId(), newStatus);
                        if (ok) {
                            search(); // refresh
                            showInfo("Updated", "Order #" + order.getOrderId() + " status changed to " + newStatus.getDisplayName());
                        } else {
                            // revert selection
                            statusBox.setValue(order.getStatus());
                            showAlert("Error", "Invalid status transition");
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    statusBox.setValue(order.getStatus());
                    HBox box = new HBox(5, viewBtn, deleteBtn, statusBox);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        // Populate status filter
        statusFilter.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        statusFilter.getItems().add(0, null); // All
        statusFilter.getSelectionModel().selectFirst();

        search();
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void search() {
        OrderStatus status = statusFilter.getValue();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        List<Order> result = orderService.getRecentOrders(200); // broad fetch, then filter client-side
        // Filter
        if (status != null) {
            result.removeIf(o -> o.getStatus() != status);
        }
        if (from != null) {
            result.removeIf(o -> toLocalDate(o.getOrderDate()).isBefore(from));
        }
        if (to != null) {
            result.removeIf(o -> toLocalDate(o.getOrderDate()).isAfter(to));
        }

        orders.setAll(result);
        ordersTable.setItems(orders);
        summaryLabel.setText("Showing " + result.size() + " orders");
    }

    @FXML
    private void clearFilters() {
        statusFilter.getSelectionModel().selectFirst();
        fromDate.setValue(null);
        toDate.setValue(null);
        search();
    }

    @FXML
    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }

    private void openQrDialog(Order order) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/qr_dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            QrDialogController controller = loader.getController();
            controller.setOrder(order);
            Stage stage = new Stage();
            stage.setTitle("QR Code - Order #" + order.getOrderId());
            stage.setScene(new javafx.scene.Scene(root));
            controller.setDialogStage(stage);
            stage.initOwner(dialogStage);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open QR dialog: " + e.getMessage());
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return LocalDate.MIN;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void deleteOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel order #" + order.getOrderId() + "? This will set status to CANCELLED.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Cancel");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean ok = orderService.cancelOrder(order.getOrderId());
            if (ok) {
                search(); // refresh table
                showInfo("Cancelled", "Order #" + order.getOrderId() + " cancelled.");
            } else {
                showAlert("Error", "Failed to cancel order.");
            }
        }
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