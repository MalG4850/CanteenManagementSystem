package canteen;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private CheckBox darkModeCheck;

    private Stage dialogStage;

    @FXML
    public void initialize() {
        // Load current theme setting
        String theme = AppConfig.getInstance().getUiTheme();
        darkModeCheck.setSelected("dark".equalsIgnoreCase(theme));
        darkModeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            String newTheme = newVal ? "dark" : "light";
            CashierApp.applyTheme(newTheme);
        });
    }

    @FXML
    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}