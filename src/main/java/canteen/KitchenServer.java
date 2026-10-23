package canteen;

import fi.iki.elonen.NanoHTTPD;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public final class KitchenServer extends NanoHTTPD {
    private final OrderService orderService = new OrderService();
    private final TokenService tokenService = new TokenService(AppConfig.getInstance().getCanteenSecret());

    public KitchenServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("✔ Kitchen server started on http://localhost:" + port + "/kitchen");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        try {
            if ("/kitchen".equals(uri) && Method.GET == method) {
                return newFixedLengthResponse(Response.Status.OK, MIME_HTML, KitchenHtml.generatePage(orderService));
            } else if ("/kitchen/api/orders".equals(uri) && Method.GET == method) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", getOrdersJson());
            } else if ("/kitchen/scan".equals(uri) && Method.POST == method) {
                return handleScan(session);
            } else if (method == Method.GET && uri.startsWith("/bill/") && uri.endsWith(".png")) {
                return handleBillImage(uri);
            } else {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
            }
        } catch (Exception e) {
            System.err.println("Kitchen server error: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Server Error");
        }
    }

    private Response handleBillImage(String uri) {
        // uri like "/bill/123.png"
        String idPart = uri.substring("/bill/".length(), uri.length() - ".png".length());
        int orderId;
        try {
            orderId = Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid order ID");
        }
        Order order = orderService.findById(orderId).orElse(null);
        if (order == null) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Order not found");
        }
        List<OrderItem> items = order.getItems();
        BufferedImage img = BillImageGenerator.createBillImage(order, items);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);
        } catch (IOException e) {
            System.err.println("Failed to encode bill image: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Image generation failed");
        }
        byte[] pngBytes = baos.toByteArray();
        return new PngResponse(pngBytes);
    }

    private String getOrdersJson() {
        List<Order> pending = orderService.getPendingOrders();
        List<Order> preparing = orderService.getPreparingOrders();
        List<Order> ready = orderService.getReadyOrders();

        StringBuilder json = new StringBuilder("{");
        json.append("\"pending\":").append(ordersToJson(pending)).append(",");
        json.append("\"preparing\":").append(ordersToJson(preparing)).append(",");
        json.append("\"ready\":").append(ordersToJson(ready));
        json.append("}");
        return json.toString();
    }

    private Response handleScan(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        Map<String, String> params = session.getParms();
        String token = params.get("token");
        String action = params.get("action");

        if (token == null || token.trim().isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Missing token\"}");
        }

        // Handle full tracking URLs if scanned (e.g. http://.../kitchen?token=xxx)
        token = token.trim();
        if (token.contains("token=")) {
            token = token.substring(token.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }
        }

        int orderId = orderService.getOrderIdByToken(token);
        if (orderId == -1) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Invalid token\"}");
        }

        OrderStatus currentStatus = orderService.getOrderStatus(orderId);
        if (currentStatus == null) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Order not found\"}");
        }

        OrderStatus nextStatus = (action == null || "next".equalsIgnoreCase(action))
            ? getNextStatus(currentStatus)
            : OrderStatus.fromString(action);

        if (!currentStatus.canTransitionTo(nextStatus)) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"error\":\"Invalid transition from " + currentStatus + " to " + nextStatus + "\"}");
        }

        boolean success = orderService.advanceStatus(orderId, currentStatus, nextStatus);
        if (success) {
            String json = String.format("{\"success\":true,\"orderId\":%d,\"newStatus\":\"%s\",\"display\":\"%s\"}",
                orderId, nextStatus, nextStatus.getDisplayName());
            return newFixedLengthResponse(Response.Status.OK, "application/json", json);
        }
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Failed to update\"}");
    }

    private OrderStatus getNextStatus(OrderStatus current) {
        return switch (current) {
            case PENDING -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.READY;
            case READY -> OrderStatus.SERVED;
            default -> current;
        };
    }

    private String ordersToJson(List<Order> orders) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"orderId\":").append(o.getOrderId()).append(",");
            sb.append("\"customer\":\"").append(escapeJson(o.getCustomerName())).append("\",");
            sb.append("\"mobile\":\"").append(o.getCustomerMobile()).append("\",");
            sb.append("\"total\":").append(String.format("%.2f", o.getTotalAmount())).append(",");
            sb.append("\"status\":\"").append(o.getStatus().name()).append("\",");
            sb.append("\"display\":\"").append(escapeJson(o.getStatus().getDisplayName())).append("\",");
            sb.append("\"token\":\"").append(o.getQrToken()).append("\",");
            sb.append("\"time\":\"").append(o.getFormattedOrderDate()).append("\",");
            sb.append("\"items\":[");
            for (int j = 0; j < o.getItems().size(); j++) {
                OrderItem item = o.getItems().get(j);
                if (j > 0) sb.append(",");
                sb.append("{");
                sb.append("\"name\":\"").append(escapeJson(item.getItemName())).append("\",");
                sb.append("\"qty\":").append(item.getQuantity()).append(",");
                sb.append("\"price\":").append(String.format("%.2f", item.getItemPrice())).append(",");
                sb.append("\"total\":").append(String.format("%.2f", item.getLineTotal()));
                sb.append("}");
            }
            sb.append("]");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static class PngResponse extends Response {
        public PngResponse(byte[] data) {
            super(Status.OK, "image/png", new ByteArrayInputStream(data), data.length);
        }
    }
}
