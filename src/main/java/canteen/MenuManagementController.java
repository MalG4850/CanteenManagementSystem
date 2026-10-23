package canteen;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MenuManagementController {

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> colItem;
    @FXML private TableColumn<MenuItem, String> colCategory;
    @FXML private TableColumn<MenuItem, Double> colPrice;
    @FXML private TableColumn<MenuItem, Boolean> colAvailable;
    @FXML private TableColumn<MenuItem, Void> colActions;

    private final MenuService menuService = new MenuService();
    private final ObservableList<MenuItem> items = FXCollections.observableArrayList();
    private Stage dialogStage;

    @FXML
    public void initialize() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAvailable.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isAvailable()));
        colAvailable.setCellFactory(col -> new TableCell<MenuItem, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty) setText(null);
                else setText(available ? "Yes" : "No");
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    deleteItem(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        loadMenu();
    }

    private void loadMenu() {
        List<MenuItem> list = menuService.getAllItems();
        items.setAll(list);
        menuTable.setItems(items);
    }

    public void refreshMenu() {
        loadMenu();
    }

    private void deleteItem(MenuItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete menu item \"" + item.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean ok = menuService.deleteItem(item.getId());
            if (ok) {
                loadMenu();
                showInfo("Deleted", "Menu item removed.");
            } else {
                showAlert("Error", "Failed to delete item.");
            }
        }
    }

    @FXML
    private void handleAddItem() {
        openAddItemDialog();
    }

    private void openAddItemDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_item_dialog.fxml"));
            Parent root = loader.load();
            AddItemDialogController controller = loader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add Menu Item");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Item dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCategory() {
        openCategoryManagementDialog();
    }

    private void openCategoryManagementDialog() {
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

    @FXML
    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
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