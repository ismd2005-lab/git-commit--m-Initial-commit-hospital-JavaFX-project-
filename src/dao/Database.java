package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/medvision_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String URL = System.getenv().getOrDefault("MEDVISION_DB_URL", DEFAULT_URL);
    private static final String USER = System.getenv().getOrDefault("MEDVISION_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("MEDVISION_DB_PASSWORD", "");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean isReachable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }
}
