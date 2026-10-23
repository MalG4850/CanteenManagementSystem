package canteen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CashierApp extends Application {
    private static Scene mainScene;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashier.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        mainScene = scene;
        String theme = AppConfig.getInstance().getUiTheme();
        String cssPath = "/css/canteen-" + theme + ".css";
        scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        stage.setTitle(AppConfig.getInstance().getCanteenName() + " - Cashier");
        stage.setScene(scene);
        stage.show();
    }

    public static void applyTheme(String theme) {
        if (mainScene != null) {
            mainScene.getStylesheets().clear();
            String cssPath = "/css/canteen-" + theme + ".css";
            mainScene.getStylesheets().add(CashierApp.class.getResource(cssPath).toExternalForm());
        }
        AppConfig.getInstance().setUiTheme(theme);
    }

    public static void main(String[] args) {
        launch(args);
    }
}