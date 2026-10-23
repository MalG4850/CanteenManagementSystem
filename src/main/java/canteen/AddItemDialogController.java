package canteen;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.List;

public class AddItemDialogController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private CheckBox availableCheck;
    @FXML private Label errorLabel;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Stage dialogStage;
    private MenuManagementController parentController;

    @FXML
    public void initialize() {
        // Load categories
        List<String> cats = new CategoryService().getAllCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(cats));
        // price validation
        priceField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*\\.?\\d*")) priceField.setText(old);
        });
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setParentController(MenuManagementController parent) {
        this.parentController = parent;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String category = categoryCombo.getValue();
        boolean available = availableCheck.isSelected();

        if (name.isEmpty() || priceText.isEmpty() || category == null) {
            showError("All fields are required.");
            return;
        }
        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException e) {
            showError("Invalid price.");
            return;
        }

        MenuService menuService = new MenuService();
        boolean ok = menuService.addItem(name, price, category);
        if (ok) {
            if (parentController != null) parentController.refreshMenu();
            closeDialog();
        } else {
            showError("Failed to add item (duplicate name?).");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}