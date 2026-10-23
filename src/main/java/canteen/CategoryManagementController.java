package canteen;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.List;
import java.util.Optional;

public class CategoryManagementController {

    @FXML private TableView<String> categoryTable;
    @FXML private TableColumn<String, String> colName;
    @FXML private TableColumn<String, Void> colActions;
    @FXML private TextField newCategoryField;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<String> categories = FXCollections.observableArrayList();
    private Stage dialogStage;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    String name = getTableView().getItems().get(getIndex());
                    deleteCategory(name);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        List<String> list = categoryService.getAllCategories();
        categories.setAll(list);
        categoryTable.setItems(categories);
    }

    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Category name cannot be empty.");
            return;
        }
        boolean ok = categoryService.addCategory(name);
        if (ok) {
            newCategoryField.clear();
            loadCategories();
            showInfo("Added", "Category \"" + name + "\" added.");
        } else {
            showAlert("Error", "Failed to add category (maybe already exists).");
        }
    }

    private void deleteCategory(String name) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete category \"" + name + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean ok = categoryService.deleteCategory(name);
            if (ok) {
                loadCategories();
                showInfo("Deleted", "Category removed.");
            } else {
                showAlert("Error", "Failed to delete category.");
            }
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