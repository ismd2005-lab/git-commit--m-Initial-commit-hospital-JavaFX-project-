package config;

public final class DatabaseConfig {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/medvision_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private DatabaseConfig() {
    }

    public static String url() {
        return System.getenv().getOrDefault("MEDVISION_DB_URL", DEFAULT_URL);
    }

    public static String user() {
        return System.getenv().getOrDefault("MEDVISION_DB_USER", "root");
    }

    public static String password() {
        return System.getenv().getOrDefault("MEDVISION_DB_PASSWORD", "");
    }

    public static String modeLabel() {
        return "In-memory mode | DB ready: " + url();
    }
}
