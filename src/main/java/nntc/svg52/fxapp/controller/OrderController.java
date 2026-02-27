package nntc.svg52.fxapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import nntc.svg52.fxapp.model.dao.OrderDAO;
import nntc.svg52.fxapp.model.entities.Order;
import nntc.svg52.fxapp.model.entities.OrderItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderController {

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Integer> orderIdColumn;

    @FXML
    private TableColumn<Order, String> tableNumberColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, String> createdAtColumn;

    @FXML
    private TableColumn<Order, String> totalColumn;

    @FXML
    private TableView<OrderItem> orderItemsTable;

    @FXML
    private TableColumn<OrderItem, String> itemDishColumn;

    @FXML
    private TableColumn<OrderItem, Integer> itemQuantityColumn;

    @FXML
    private TableColumn<OrderItem, BigDecimal> itemPriceColumn;

    @FXML
    private TableColumn<OrderItem, String> itemSubtotalColumn;

    @FXML
    private TextArea orderDetailsArea;

    @FXML
    private Label orderTotalLabel;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label customerInfoLabel;

    @FXML
    private Button acceptOrderButton;

    @FXML
    private Button completeOrderButton;

    @FXML
    private Button cancelOrderButton;

    @FXML
    private Button refreshButton;

    private MainController mainController;
    private String userRole;
    private OrderDAO orderDAO;
    private ObservableList<Order> ordersData;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();

        // Настройка таблицы заказов
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableNumberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("Стол " + cellData.getValue().getTableNumber()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter)));
        totalColumn.setCellValueFactory(cellData -> {
            BigDecimal total = orderDAO.getOrderTotal(cellData.getValue().getId());
            return new SimpleStringProperty(total.toString() + " руб.");
        });

        // Настройка таблицы позиций заказа - ИСПРАВЛЕНО!
        itemDishColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTime"));
        itemSubtotalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubtotal().toString() + " руб."));

        // Обработчик выбора заказа - ИСПРАВЛЕНО!
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showOrderDetails(newSelection);
                    }
                });
    }

    public void setMainController(MainController mainController, String userRole) {
        this.mainController = mainController;
        this.userRole = userRole;

        // Настройка кнопок в зависимости от роли - ИСПРАВЛЕНО!
        if ("client".equals(userRole)) {
            acceptOrderButton.setVisible(false);
            completeOrderButton.setVisible(false);
            cancelOrderButton.setVisible(true);       // Клиент может отменять свои заказы
        } else if ("waiter".equals(userRole)) {
            acceptOrderButton.setVisible(true);
            completeOrderButton.setVisible(true);
            cancelOrderButton.setVisible(true);
        } else if ("manager".equals(userRole)) {
            acceptOrderButton.setVisible(true);
            completeOrderButton.setVisible(true);
            cancelOrderButton.setVisible(true);
        }

        loadOrders();
    }

    private void loadOrders() {
        if ("client".equals(userRole)) {
            ordersData = FXCollections.observableArrayList(orderDAO.getActiveOrders());
        } else {
            ordersData = FXCollections.observableArrayList(orderDAO.getAllOrders());
        }
        ordersTable.setItems(ordersData);
    }

    private void showOrderDetails(Order order) {
        Order fullOrder = orderDAO.getOrderById(order.getId());
        if (fullOrder != null) {
            orderItemsTable.setItems(FXCollections.observableArrayList(fullOrder.getItems()));

            StringBuilder details = new StringBuilder();
            details.append("Заказ №").append(fullOrder.getId()).append("\n");
            details.append("Стол: ").append(fullOrder.getTableNumber()).append("\n");
            if (fullOrder.getSpecialRequests() != null && !fullOrder.getSpecialRequests().isEmpty()) {
                details.append("Пожелания: ").append(fullOrder.getSpecialRequests()).append("\n");
            }
            orderDetailsArea.setText(details.toString());

            orderTotalLabel.setText("Итого: " + orderDAO.getOrderTotal(order.getId()) + " руб.");
            orderStatusLabel.setText("Статус: " + getStatusText(fullOrder.getStatus()));
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "active": return "Активен";
            case "completed": return "Завершен";
            case "cancelled": return "Отменен";
            default: return status;
        }
    }

    @FXML
    private void handleAcceptOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showWarning("Выберите заказ");
            return;
        }

        if (!"active".equals(selectedOrder.getStatus())) {
            showWarning("Можно принять только активный заказ");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Принять заказ №" + selectedOrder.getId() + "?");
        alert.setContentText("Заказ будет передан на кухню");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showInfo("Заказ принят");
        }
    }

    @FXML
    private void handleCompleteOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showWarning("Выберите заказ");
            return;
        }

        if (!"active".equals(selectedOrder.getStatus())) {
            showWarning("Можно завершить только активный заказ");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Завершить заказ №" + selectedOrder.getId() + "?");
        alert.setContentText("Заказ будет отмечен как выполненный");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (orderDAO.completeOrder(selectedOrder.getId())) {
                showInfo("Заказ №" + selectedOrder.getId() + " завершен");
                loadOrders();
                orderItemsTable.getItems().clear();
                orderDetailsArea.clear();
            } else {
                showError("Ошибка при завершении заказа");
            }
        }
    }

    @FXML
    private void handleCancelOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showWarning("Выберите заказ");
            return;
        }

        if (!"active".equals(selectedOrder.getStatus())) {
            showWarning("Можно отменить только активный заказ");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение отмены");
        alert.setHeaderText("Отменить заказ №" + selectedOrder.getId() + "?");
        alert.setContentText("Это действие нельзя отменить");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (orderDAO.cancelOrder(selectedOrder.getId())) {
                showInfo("Заказ №" + selectedOrder.getId() + " отменен");
                loadOrders();
                orderItemsTable.getItems().clear();
                orderDetailsArea.clear();
            } else {
                showError("Ошибка при отмене заказа");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
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