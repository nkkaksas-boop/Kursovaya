package nntc.svg52.fxapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import nntc.svg52.fxapp.model.dao.DishDAO;
import nntc.svg52.fxapp.model.dao.OrderDAO;
import nntc.svg52.fxapp.model.entities.Dish;
import nntc.svg52.fxapp.model.entities.Order;
import nntc.svg52.fxapp.model.entities.OrderItem;
import nntc.svg52.fxapp.model.entities.User;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MenuController {

    @FXML private TableView<Dish> dishesTable;
    @FXML private TableColumn<Dish, String> nameColumn;
    @FXML private TableColumn<Dish, String> categoryColumn;
    @FXML private TableColumn<Dish, BigDecimal> priceColumn;
    @FXML private TableColumn<Dish, String> availabilityColumn;

    @FXML private TextArea dishDescriptionArea;
    @FXML private TextField quantityField;
    @FXML private TextArea specialRequestsField;
    @FXML private TextField tableNumberField;

    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> cartDishColumn;
    @FXML private TableColumn<OrderItem, Integer> cartQuantityColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> cartPriceColumn;
    @FXML private TableColumn<OrderItem, String> cartSubtotalColumn;

    @FXML private Label totalAmountLabel;
    @FXML private Button addToCartButton;
    @FXML private Button removeFromCartButton;
    @FXML private Button createOrderButton;
    @FXML private Button manageMenuButton;

    private MainController mainController;
    private String userRole;
    private DishDAO dishDAO;
    private OrderDAO orderDAO;
    private ObservableList<Dish> dishData;
    private ObservableList<OrderItem> cartData;
    private Map<Integer, BigDecimal> dishPrices;
    private User currentUser;

    @FXML
    public void initialize() {
        dishDAO = new DishDAO();
        orderDAO = new OrderDAO();
        dishPrices = new HashMap<>();

        // Инициализация таблицы блюд
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // ИСПРАВЛЕНО: правильная реализация для availabilityColumn
        availabilityColumn.setCellValueFactory(cellData -> {
            boolean available = cellData.getValue().isAvailable();
            return new SimpleStringProperty(available ? "Доступно" : "Недоступно");
        });

        // ИСПРАВЛЕНО: правильная инициализация корзины
        cartDishColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTime"));
        cartSubtotalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubtotal().toString() + " руб."));

        // ИСПРАВЛЕНО: правильный listener для выбора блюда
        dishesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        dishDescriptionArea.setText(newSelection.getDescription());
                    }
                });

        loadDishes();
    }

    public void setMainController(MainController mainController, String userRole) {
        this.mainController = mainController;
        this.userRole = userRole;

        if ("client".equals(userRole)) {
            manageMenuButton.setVisible(false);
        } else if ("waiter".equals(userRole)) {
            manageMenuButton.setVisible(false);
        } else if ("manager".equals(userRole)) {
            manageMenuButton.setVisible(true);
        }

        cartData = FXCollections.observableArrayList();
        cartTable.setItems(cartData);
        updateTotalAmount();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadDishes() {
        dishData = FXCollections.observableArrayList(dishDAO.getAvailableDishes());
        dishesTable.setItems(dishData);

        for (Dish dish : dishData) {
            dishPrices.put(dish.getId(), dish.getPrice());
        }
    }

    @FXML
    private void handleAddToCart() {
        Dish selectedDish = dishesTable.getSelectionModel().getSelectedItem();
        if (selectedDish == null) {
            showWarning("Выберите блюдо");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showWarning("Количество должно быть положительным числом");
                return;
            }
        } catch (NumberFormatException e) {
            showWarning("Введите корректное количество");
            return;
        }

        Optional<OrderItem> existingItem = cartData.stream()
                .filter(item -> item.getDishId() == selectedDish.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartTable.refresh();
        } else {
            OrderItem item = new OrderItem();
            item.setDishId(selectedDish.getId());
            item.setDishName(selectedDish.getName());
            item.setQuantity(quantity);
            item.setPriceAtTime(selectedDish.getPrice());
            item.setNotes(specialRequestsField.getText());
            cartData.add(item);
        }

        updateTotalAmount();
        quantityField.clear();
        specialRequestsField.clear();
    }

    @FXML
    private void handleRemoveFromCart() {
        OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удалить блюдо из корзины?");
            alert.setContentText(selectedItem.getDishName());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cartData.remove(selectedItem);
                updateTotalAmount();
            }
        }
    }

    @FXML
    private void handleCreateOrder() {
        if (cartData.isEmpty()) {
            showWarning("Корзина пуста");
            return;
        }

        int tableNumber;
        try {
            tableNumber = Integer.parseInt(tableNumberField.getText().trim());
            if (tableNumber <= 0) {
                showWarning("Номер стола должен быть положительным числом");
                return;
            }
        } catch (NumberFormatException e) {
            showWarning("Введите номер стола");
            return;
        }

        Order order = new Order();
        order.setUserId(currentUser != null ? currentUser.getId() : 1);
        order.setTableNumber(tableNumber);
        order.setStatus("active");
        order.setSpecialRequests("");
        order.setItems(cartData);

        int orderId = orderDAO.createOrder(order);
        if (orderId > 0) {
            showInfo("Заказ №" + orderId + " успешно создан");
            cartData.clear();
            tableNumberField.clear();
            updateTotalAmount();
        } else {
            showError("Ошибка при создании заказа");
        }
    }

    @FXML
    private void handleManageMenu() {
        if ("manager".equals(userRole)) {
            mainController.showMenuManagement();
        } else {
            showWarning("У вас нет прав для управления меню");
        }
    }

    private void updateTotalAmount() {
        BigDecimal total = cartData.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAmountLabel.setText("Итого: " + total.toString() + " руб.");
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
