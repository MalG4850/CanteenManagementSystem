package canteen;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class QrDialogController {
    @FXML private ImageView qrImageView;
    @FXML private ImageView billImageView;
    @FXML private Label titleLabel;
    @FXML private Label shortCodeLabel;
    @FXML private Label orderInfoLabel;
    @FXML private Button sendWhatsAppBtn;
    @FXML private Button closeBtn;

    private Order order;
    private Stage dialogStage;
    private final WhatsAppService whatsAppService = new WhatsAppService(AppConfig.getInstance());

    public void setOrder(Order order) {
        this.order = order;
        updateUI();
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    private void updateUI() {
        if (order == null) return;
        titleLabel.setText("Order #" + order.getOrderId() + " - QR Code");
        shortCodeLabel.setText("Short Code: " + order.getShortCode());
        shortCodeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        orderInfoLabel.setText("Customer: " + order.getCustomerName() + "\nMobile: " + order.getCustomerMobile() +
                "\nTotal: ₹" + String.format("%.2f", order.getTotalAmount()));

        // Load bill image from server
        String mediaUrl = AppConfig.getInstance().getBaseUrl() + "/bill/" + order.getOrderId() + ".png";
        try {
            Image billImg = new Image(new URL(mediaUrl).openStream());
            billImageView.setImage(billImg);
        } catch (Exception e) {
            System.err.println("Failed to load bill image: " + e.getMessage());
        }

        // Generate QR code for the tracking link
        String qrLink = AppConfig.getInstance().getBaseUrl() + "/kitchen?token=" + order.getQrToken();
        BufferedImage qrBuffered = QrGenerator.generate(qrLink, 300);
        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(qrBuffered, null);
        qrImageView.setImage(fxImage);
    }

    @FXML
    private void sendWhatsApp() {
        if (order == null) return;
        sendWhatsAppBtn.setDisable(true);
        sendWhatsAppBtn.setText("Sending...");
        new Thread(() -> {
            boolean sent = whatsAppService.sendInvoice(order, order.getCustomerMobile());
            javafx.application.Platform.runLater(() -> {
                sendWhatsAppBtn.setDisable(false);
                sendWhatsAppBtn.setText("Send WhatsApp");
                if (sent) {
                    showInfo("WhatsApp Sent", "Invoice sent to " + order.getCustomerMobile());
                } else {
                    showAlert("Failed", "Could not send WhatsApp. Check Twilio configuration.");
                }
            });
        }).start();
    }

    @FXML
    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }

    private void showAlert(String title, String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, msg, javafx.scene.control.ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, msg, javafx.scene.control.ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}