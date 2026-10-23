package canteen;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class SystemSanityTest {

    @Test
    void testTokenGeneration() {
        TokenService tokenService = new TokenService("test-secret-key-12345");
        String token = tokenService.generateToken(101);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testQrCodeGeneration() {
        BufferedImage qrImage = QrGenerator.generate("http://localhost:8080/kitchen?token=test-token", 200);
        assertNotNull(qrImage);
        assertEquals(200, qrImage.getWidth());
        assertEquals(200, qrImage.getHeight());
    }

    @Test
    void testOrderStatusTransitions() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.READY));
        assertTrue(OrderStatus.READY.canTransitionTo(OrderStatus.SERVED));
        assertFalse(OrderStatus.SERVED.canTransitionTo(OrderStatus.PENDING));
    }
}
