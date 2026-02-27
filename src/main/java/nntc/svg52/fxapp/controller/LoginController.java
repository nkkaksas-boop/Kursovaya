package nntc.svg52.fxapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import nntc.svg52.fxapp.DesktopApplication;
import nntc.svg52.fxapp.model.dao.UserDAO;
import nntc.svg52.fxapp.model.entities.User;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private DesktopApplication mainApp;
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        loginButton.setDefaultButton(true);
    }

    public void setMainApp(DesktopApplication mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            try {
                mainApp.showMainWindow(user.getRoleName());
            } catch (Exception e) {
                showError("Ошибка при открытии главного окна: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Неверное имя пользователя или пароль");
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}
