package nntc.svg52.fxapp.controller;

// ИМПОРТЫ JAVAFX - ИХ НЕ ХВАТАЛО!
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// ИМПОРТЫ ПРОЕКТА
import nntc.svg52.fxapp.model.dao.DishDAO;
import nntc.svg52.fxapp.model.entities.Dish;

import java.math.BigDecimal;
import java.util.Optional;

public class MenuManagementController {

    @FXML private TableView<Dish> dishesTable;
    @FXML private TableColumn<Dish, Integer> idColumn;
    @FXML private TableColumn<Dish, String> nameColumn;
    @FXML private TableColumn<Dish, String> categoryColumn;
    @FXML private TableColumn<Dish, BigDecimal> priceColumn;
    @FXML private TableColumn<Dish, Boolean> availableColumn;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionField;
    @FXML private CheckBox availableCheckbox;

    private DishDAO dishDAO;
    private ObservableList<Dish> dishData;  // ObservableList из javafx.collections
    private Dish selectedDish;

    @FXML
    public void initialize() {
        dishDAO = new DishDAO();

        // ИСПРАВЛЕНО: PropertyValueFactory работает!
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));

        // ИСПРАВЛЕНО: категории
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Супы", "Салаты", "Горячее", "Гарниры", "Напитки", "Десерты"  // Исправлено: "Сумь" -> "Супы"
        ));

        // ИСПРАВЛЕНО: listener для выбора в таблице
        dishesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {  // Убраны сложные типы
                    if (newSelection != null) {
                        selectedDish = newSelection;
                        showDishDetails(newSelection);
                    }
                });

        loadDishes();
    }

    private void loadDishes() {
        dishData = FXCollections.observableArrayList(dishDAO.getAllDishes());
        dishesTable.setItems(dishData);  // ИСПРАВЛЕНО: setItems, а не settings
    }

    private void showDishDetails(Dish dish) {
        nameField.setText(dish.getName());
        categoryCombo.setValue(dish.getCategory());
        priceField.setText(dish.getPrice().toString());
        descriptionField.setText(dish.getDescription());
        availableCheckbox.setSelected(dish.isAvailable());
    }

    private void clearFields() {
        nameField.clear();
        categoryCombo.setValue(null);
        priceField.clear();
        descriptionField.clear();
        availableCheckbox.setSelected(true);
        selectedDish = null;
        dishesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) return;

        Dish dish = new Dish();
        dish.setName(nameField.getText().trim());
        dish.setCategory(categoryCombo.getValue());
        dish.setPrice(new BigDecimal(priceField.getText().trim()));
        dish.setDescription(descriptionField.getText().trim());
        dish.setAvailable(availableCheckbox.isSelected());

        if (dishDAO.createDish(dish)) {
            showInfo("Блюдо добавлено");
            loadDishes();
            clearFields();
        } else {
            showError("Ошибка при добавлении блюда");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedDish == null) {
            showWarning("Выберите блюдо для обновления");
            return;
        }

        if (!validateInputs()) return;

        selectedDish.setName(nameField.getText().trim());
        selectedDish.setCategory(categoryCombo.getValue());
        selectedDish.setPrice(new BigDecimal(priceField.getText().trim()));
        selectedDish.setDescription(descriptionField.getText().trim());
        selectedDish.setAvailable(availableCheckbox.isSelected());

        if (dishDAO.updateDish(selectedDish)) {
            showInfo("Блюдо обновлено");
            loadDishes();
            clearFields();
        } else {
            showError("Ошибка при обновлении блюда");
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedDish == null) {
            showWarning("Выберите блюдо для удаления");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить блюдо?");
        alert.setContentText(selectedDish.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dishDAO.deleteDish(selectedDish.getId())) {
                showInfo("Блюдо удалено");
                loadDishes();
                clearFields();
            } else {
                showError("Ошибка при удалении блюда");
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu.fxml"));
            Parent menuView = loader.load();

            Scene scene = nameField.getScene();
            if (scene != null) {
                VBox contentArea = (VBox) scene.lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(menuView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // Позже заменить на логирование
        }
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showWarning("Введите название блюда");
            return false;
        }
        if (categoryCombo.getValue() == null || categoryCombo.getValue().trim().isEmpty()) {
            showWarning("Выберите категорию");
            return false;
        }
        if (priceField.getText().trim().isEmpty()) {
            showWarning("Введите цену");
            return false;
        }
        try {
            new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Цена должна быть числом");
            return false;
        }
        return true;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}