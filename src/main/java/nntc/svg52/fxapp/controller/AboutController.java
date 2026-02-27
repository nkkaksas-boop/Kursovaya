package nntc.svg52.fxapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutController {

    @FXML private Label appNameLabel;
    @FXML private Label versionLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label authorLabel;
    @FXML private Label groupLabel;
    @FXML private Label yearLabel;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        appNameLabel.setText("Электронное меню ресторана");
        versionLabel.setText("Версия: 1.0.0");
        descriptionLabel.setText("Курсовой проект по МДК 11.01");
        authorLabel.setText("Автор: Хозяинов Андрей Сергеевич");
        groupLabel.setText("Группа: ЗИСиП-23-1");
        yearLabel.setText("2025");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
