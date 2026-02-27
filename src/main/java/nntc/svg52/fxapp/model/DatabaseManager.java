package nntc.svg52.fxapp.model;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private ConfigManager configManager;

    private DatabaseManager() {
        configManager = new ConfigManager();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = configManager.getConnectionUrl();
            connection = DriverManager.getConnection(
                    url,
                    configManager.getUsername(),
                    configManager.getPassword()
            );
        }
        return connection;
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(5);
            if (isValid) {
                System.out.println("Подключение к БД успешно!");
            }
            return isValid;
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}