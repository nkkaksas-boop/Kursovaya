package nntc.svg52.fxapp.model;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "database.properties";
    private Properties properties;

    public ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setDefaultConfig();
        }
    }

    public void saveConfig(String host, String port, String database, String schema,
                           String username, String password) {
        properties.setProperty("db.host", host);
        properties.setProperty("db.port", port);
        properties.setProperty("db.name", database);
        properties.setProperty("db.schema", schema);
        properties.setProperty("db.username", username);
        properties.setProperty("db.password", password);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Database Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultConfig() {
        properties.setProperty("db.host", "localhost");
        properties.setProperty("db.port", "5432");
        properties.setProperty("db.name", "restaurant_menu");
        properties.setProperty("db.schema", "restaurant_schema");
        properties.setProperty("db.username", "postgres");
        properties.setProperty("db.password", "postgres");
    }

    public String getHost() {
        return properties.getProperty("db.host", "localhost");
    }

    public String getPort() {
        return properties.getProperty("db.port", "5432");
    }

    public String getDatabase() {
        return properties.getProperty("db.name", "restaurant_menu");
    }

    public String getSchema() {
        return properties.getProperty("db.schema", "restaurant_schema");
    }

    public String getUsername() {
        return properties.getProperty("db.username", "postgres");
    }

    public String getPassword() {
        return properties.getProperty("db.password", "postgres");
    }

    public String getConnectionUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s?currentSchema=%s",
                getHost(), getPort(), getDatabase(), getSchema());
    }
}