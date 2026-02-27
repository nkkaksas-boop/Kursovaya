package nntc.svg52.fxapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import nntc.svg52.fxapp.model.ConfigManager;
import nntc.svg52.fxapp.model.DatabaseManager;
import nntc.svg52.fxapp.controller.LoginController;
import nntc.svg52.fxapp.controller.MainController;

import java.util.Optional;

public class DesktopApplication extends Application {

    private Stage primaryStage;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.configManager = new ConfigManager();

        if (!testDatabaseConnection()) {
            showSettingsWindow();
        } else {
            showLoginWindow();
        }

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            closeConfirmation();
        });
    }

    private boolean testDatabaseConnection() {
        try {
            databaseManager = DatabaseManager.getInstance();
            return databaseManager.testConnection();
        } catch (Exception e) {
            return false;
        }
    }

    private void showLoginWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setTitle("Ресторан - Вход в систему");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    private void showSettingsWindow() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settings.fxml"));
        Parent root = loader.load();

        Stage settingsStage = new Stage();
        settingsStage.setTitle("Настройки подключения");
        settingsStage.setScene(new Scene(root));
        settingsStage.show();
    }

    // ДОБАВЛЯЕМ ЭТОТ МЕТОД!
    public void showMainWindow(String userRole) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setMainApp(this, userRole);

        primaryStage.setTitle("Электронное меню ресторана");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
    }

    private void closeConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Все несохраненные данные будут потеряны.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
