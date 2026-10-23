package canteen;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class TokenService {
    private static final String ALGORITHM = "HmacSHA256";
    private final String secret;

    public TokenService(String secret) {
        this.secret = secret;
    }

    public String generateToken(int orderId, long timestamp) {
        String data = orderId + ":" + timestamp;
        return hmac(data);
    }

    public String generateToken(int orderId) {
        return generateToken(orderId, System.currentTimeMillis());
    }

    public boolean verifyToken(int orderId, String token) {
        if (token == null || token.trim().isEmpty()) return false;
        return new OrderDao().findByQrToken(token).map(o -> o.getOrderId() == orderId).orElse(false);
    }

    public boolean verifyToken(int orderId, String token, long maxAgeMillis) {
        return verifyToken(orderId, token);
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Return URL-safe base64 without padding
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC generation failed", e);
        }
    }
}