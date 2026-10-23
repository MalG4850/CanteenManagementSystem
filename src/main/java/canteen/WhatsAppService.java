package canteen;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public final class WhatsAppService {
    private final HttpClient httpClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String baseUrl;

    public WhatsAppService(AppConfig config) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.accountSid = config.getTwilioSid();
        this.authToken = config.getTwilioToken();
        this.fromNumber = config.getTwilioFrom();
        this.baseUrl = config.getBaseUrl();
    }

    public boolean sendInvoice(Order order, String customerPhone) {
        if (accountSid == null || accountSid.startsWith("YOUR_")) {
            System.err.println("⚠ Twilio credentials not configured, skipping WhatsApp");
            return false;
        }

        String toNumber = formatPhone(customerPhone);
        String body = buildInvoiceMessage(order);

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String form = "To=" + URLEncoder.encode(toNumber, StandardCharsets.UTF_8)
            + "&From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
            + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);

        String auth = accountSid + ":" + authToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + encodedAuth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✔ WhatsApp invoice sent to " + toNumber);
                return true;
            } else {
                System.err.println("❌ Twilio API error (" + response.statusCode() + "): " + response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ WhatsApp send failed: " + e.getMessage());
            Thread.currentThread().interrupt(); // restore interrupt
            return false;
        }
    }

    public boolean sendQrNotification(Order order, String customerPhone) {
        if (accountSid == null || accountSid.startsWith("YOUR_")) {
            return false;
        }

        String toNumber = formatPhone(customerPhone);
        String qrLink = baseUrl + "/kitchen?token=" + order.getQrToken();
        String body = "Your order #" + order.getOrderId() + " is confirmed!\n" +
            "Track status: " + qrLink + "\n" +
            "Show this QR code at the counter for pickup.";

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String form = "To=" + URLEncoder.encode(toNumber, StandardCharsets.UTF_8)
            + "&From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
            + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);

        String auth = accountSid + ":" + authToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + encodedAuth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ WhatsApp QR notification failed: " + e.getMessage());
            return false;
        }
    }

    private String formatPhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("0")) digits = digits.substring(1);
        if (digits.startsWith("91") && digits.length() == 12) return "whatsapp:+" + digits;
        if (digits.length() == 10) return "whatsapp:+91" + digits;
        return "whatsapp:+" + digits;
    }

    public boolean sendInvoiceMedia(Order order, String customerPhone, String mediaUrl) {
        if (accountSid == null || accountSid.startsWith("YOUR_")) {
            System.err.println("⚠ Twilio credentials not configured, MOCK WhatsApp media send");
            System.err.println("   To: " + formatPhone(customerPhone));
            System.err.println("   Media URL: " + mediaUrl);
            // Optionally save image locally for inspection
            try {
                java.net.URL url = new java.net.URL(mediaUrl);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                java.io.File out = new java.io.File("target/bill-" + order.getOrderId() + ".png");
                out.getParentFile().mkdirs();
                javax.imageio.ImageIO.write(img, "png", out);
                System.err.println("   Saved mock image to " + out.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("   Could not fetch image for mock: " + e.getMessage());
            }
            return false;
        }

        String toNumber = formatPhone(customerPhone);
        String body = buildInvoiceMessage(order) + "\n\nShort Code: " + order.getShortCode();

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String form = "To=" + URLEncoder.encode(toNumber, StandardCharsets.UTF_8)
            + "&From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
            + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8)
            + "&MediaUrl=" + URLEncoder.encode(mediaUrl, StandardCharsets.UTF_8);

        String auth = accountSid + ":" + authToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + encodedAuth)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✔ WhatsApp media invoice sent to " + toNumber);
                return true;
            } else {
                System.err.println("❌ Twilio API error (" + response.statusCode() + "): " + response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ WhatsApp media send failed: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String buildInvoiceMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧾 *").append(AppConfig.getInstance().get("canteen.name", "Canteen")).append(" Tax Invoice* \n");
        sb.append("Order ID: #").append(order.getOrderId()).append("\n");
        sb.append("Customer: ").append(order.getCustomerName()).append("\n");
        sb.append("Date: ").append(order.getFormattedOrderDate()).append("\n");
        sb.append("───────────────\n");

        for (OrderItem item : order.getItems()) {
            sb.append(item.getItemName())
                .append(" x").append(item.getQuantity())
                .append(" = ₹").append(String.format("%.2f", item.getLineTotal()))
                .append("\n");
        }

        sb.append("───────────────\n");
        sb.append("Subtotal: ₹").append(String.format("%.2f", order.getBaseAmount())).append("\n");
        sb.append("GST (5%): ₹").append(String.format("%.2f", order.getGstAmount())).append("\n");
        sb.append("*Total: ₹").append(String.format("%.2f", order.getTotalAmount())).append("*\n");
        sb.append("\nThank you for your order! 🙏");

        return sb.toString();
    }
}