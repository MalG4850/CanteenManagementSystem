package canteen;

import canteen.ui.theme.ThemeManager;
import javax.swing.SwingUtilities;
import java.io.IOException;

public final class Main {
    private static KitchenServer kitchenServer;
    private static final int KITCHEN_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║   CANTEEN MANAGEMENT SYSTEM v2.0          ║");
        System.out.println("║   College Canteen Order Management        ║");
        System.out.println("╚═══════════════════════════════════════════╝");

        // Initialize config (loads config.properties)
        AppConfig config = AppConfig.getInstance();
        System.out.println("✔ Configuration loaded");

        // Register shutdown hook early
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

        // Test database connection pool
        try (var conn = config.getDataSource().getConnection()) {
            System.out.println("✔ Database connected: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("   Check config.properties and ensure MariaDB is running");
            System.exit(1);
        }

        // Start embedded HTTP server for kitchen
        startKitchenServer(config);

        // Launch modern Swing Dashboard
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
        });
    }

    private static void startKitchenServer(AppConfig config) {
        try {
            kitchenServer = new KitchenServer(KITCHEN_PORT);
        } catch (IOException e) {
            System.err.println("❌ Failed to start kitchen server: " + e.getMessage());
        }
    }

    private static void shutdown() {
        System.out.println("\nShutting down...");
        if (kitchenServer != null) {
            kitchenServer.stop();
        }
        AppConfig.getInstance().closeDataSource();
        System.out.println("✔ Shutdown complete");
    }
}