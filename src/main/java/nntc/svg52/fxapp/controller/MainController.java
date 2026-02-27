package nntc.svg52.fxapp.controller;

// ПРАВИЛЬНЫЕ ИМПОРТЫ - ВСЕ ДОЛЖНЫ БЫТЬ javafx!
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;  // Stage из javafx, не javax.swing!

import nntc.svg52.fxapp.DesktopApplication;  // Исправлено: DeskTopApplication -> DesktopApplication
import nntc.svg52.fxapp.model.dao.DishDAO;
import nntc.svg52.fxapp.model.dao.OrderDAO;

import java.util.Optional;

public class MainController {

    @FXML private MenuBar menuBar;
    @FXML private Menu fileMenu;
    @FXML private Menu editMenu;
    @FXML private Menu helpMenu;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private VBox contentArea;
    @FXML private Label userRoleLabel;

    private DesktopApplication mainApp;
    private String userRole;
    private DishDAO dishDAO;
    private OrderDAO orderDAO;

    @FXML
    public void initialize() {
        dishDAO = new DishDAO();
        orderDAO = new OrderDAO();
    }

    public void setMainApp(DesktopApplication mainApp, String userRole) {
        this.mainApp = mainApp;
        this.userRole = userRole;
        userRoleLabel.setText("Роль: " + userRole);
        loadDefaultContent();
    }

    private void loadDefaultContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
            Parent menuView = loader.load();

            MenuController controller = loader.getController();
            controller.setMainController(this, userRole);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(menuView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settings.fxml"));
            Parent settingsView = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Настройки");
            settingsStage.setScene(new Scene(settingsView));
            settingsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/about.fxml"));
            Parent aboutView = loader.load();

            Stage aboutStage = new Stage();
            aboutStage.setTitle("О программе");
            aboutStage.setScene(new Scene(aboutView));
            aboutStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение выхода");
        alert.setHeaderText("Вы действительно хотите выйти?");
        alert.setContentText("Все несохраненные данные будут потеряны.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    public void showMenuManagement() {
        if (!"manager".equals(userRole)) {
            showError("У вас нет прав для управления меню");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu_management.fxml"));
            Parent managementView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(managementView);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка загрузки управления меню");
        }
    }

    public void showOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/orders.fxml"));
            Parent ordersView = loader.load();

            OrderController controller = loader.getController();
            controller.setMainController(this, userRole);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(ordersView);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка загрузки заказов");
        }
    }

    public String getUserRole() {
        return userRole;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
