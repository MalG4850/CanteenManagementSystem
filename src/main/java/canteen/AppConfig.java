package canteen;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final Properties props = new Properties();
    private final AtomicReference<HikariDataSource> dataSourceRef = new AtomicReference<>();

    private AppConfig() {
        loadConfig();
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    private void loadConfig() {
        Path configPath = Paths.get("config.properties");
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                System.out.println("✔ Loaded config from: " + configPath.toAbsolutePath());
                return;
            } catch (IOException e) {
                System.err.println("⚠ Failed to load config.properties: " + e.getMessage());
            }
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("✔ Loaded config from classpath");
            } else {
                System.err.println("⚠ config.properties not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("⚠ Failed to load classpath config: " + e.getMessage());
        }
    }

    public String get(String key) { return props.getProperty(key); }
    public String get(String key, String defaultValue) { return props.getProperty(key, defaultValue); }
    public int getInt(String key, int defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        try { return Integer.parseInt(val.trim()); } catch (NumberFormatException e) { return defaultValue; }
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.trim());
    }

    public HikariDataSource getDataSource() {
        HikariDataSource ds = dataSourceRef.get();
        if (ds == null || ds.isClosed()) {
            synchronized (dataSourceRef) {
                ds = dataSourceRef.get();
                if (ds == null || ds.isClosed()) {
                    HikariConfig config = new HikariConfig();
                    String host = get("db.host", "localhost");
                    int port = getInt("db.port", 3306);
                    String dbName = get("db.name", "canteen_db");
                    config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC");
                    config.setUsername(get("db.user", "root"));
                    config.setPassword(get("db.password", "root"));
                    config.setDriverClassName("org.mariadb.jdbc.Driver");

                    // Performance & Pooling tuning
                    config.setMaximumPoolSize(10);
                    config.setMinimumIdle(2);
                    config.setIdleTimeout(30000);
                    config.setConnectionTimeout(10000);
                    config.setMaxLifetime(1800000);
                    config.setPoolName("CanteenHikariPool");

                    ds = new HikariDataSource(config);
                    dataSourceRef.set(ds);
                }
            }
        }
        return ds;
    }

    public void closeDataSource() {
        HikariDataSource ds = dataSourceRef.get();
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    public String getTwilioSid() { return get("twilio.account.sid"); }
    public String getTwilioToken() { return get("twilio.auth.token"); }
    public String getTwilioFrom() { return get("twilio.from.number", "whatsapp:+14155238886"); }
    public String getCanteenSecret() { return get("canteen.secret", "default-secret-change-me"); }
    public String getBaseUrl() { return get("canteen.base.url", "http://localhost:8080"); }
    public String getCanteenName() { return get("canteen.name", "College Canteen"); }

    public String getUiTheme() { return get("ui.theme", "light"); }

    public void setUiTheme(String theme) {
        if (!"dark".equalsIgnoreCase(theme) && !"light".equalsIgnoreCase(theme)) {
            theme = "light";
        }
        props.setProperty("ui.theme", theme);
        // Persist to file
        try {
            Path configPath = Paths.get("config.properties");
            try (var out = Files.newOutputStream(configPath)) {
                props.store(out, "Canteen Management System Config");
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}
