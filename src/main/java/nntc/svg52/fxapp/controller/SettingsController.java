package nntc.svg52.fxapp.controller;

// ПРАВИЛЬНЫЕ ИМПОРТЫ - используем javafx, а не javax!
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import nntc.svg52.fxapp.DesktopApplication;
import nntc.svg52.fxapp.model.ConfigManager;
import nntc.svg52.fxapp.model.DatabaseManager;

public class SettingsController {

    @FXML
    private TextField hostTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private TextField databaseTextField;

    @FXML
    private TextField schemaTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;  // Исправлено: PasswordField, а не PasswordTextField

    @FXML
    private Button testButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label statusLabel;

    private DesktopApplication mainApp;
    private ConfigManager configManager;
    private Stage dialogStage;

    @FXML
    public void initialize() {
        configManager = new ConfigManager();
        loadCurrentConfig();
    }

    public void setMainApp(DesktopApplication mainApp) {
        this.mainApp = mainApp;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void loadCurrentConfig() {
        hostTextField.setText(configManager.getHost());
        portTextField.setText(configManager.getPort());
        databaseTextField.setText(configManager.getDatabase());
        schemaTextField.setText(configManager.getSchema());
        usernameTextField.setText(configManager.getUsername());
        passwordTextField.setText(configManager.getPassword());
    }

    @FXML
    private void handleTestConnection() {
        String host = hostTextField.getText().trim();
        String port = portTextField.getText().trim();
        String database = databaseTextField.getText().trim();
        String schema = schemaTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText();

        if (host.isEmpty() || port.isEmpty() || database.isEmpty() || username.isEmpty()) {
            showStatus("Заполните все обязательные поля", "red");
            return;
        }

        try {
            // Создаем временную конфигурацию для теста
            ConfigManager testConfig = new ConfigManager();
            testConfig.saveConfig(host, port, database, schema, username, password);

            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager.testConnection()) {
                showStatus("Подключение успешно!", "green");
            } else {
                showStatus("Ошибка подключения к БД", "red");
            }
        } catch (Exception e) {
            showStatus("Ошибка: " + e.getMessage(), "red");
        }
    }

    @FXML
    private void handleSave() {
        String host = hostTextField.getText().trim();
        String port = portTextField.getText().trim();
        String database = databaseTextField.getText().trim();
        String schema = schemaTextField.getText().trim();
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText();

        if (host.isEmpty() || port.isEmpty() || database.isEmpty() || username.isEmpty()) {
            showStatus("Заполните все обязательные поля", "red");
            return;
        }

        configManager.saveConfig(host, port, database, schema, username, password);
        showStatus("Настройки сохранены", "green");

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}