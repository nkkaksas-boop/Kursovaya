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

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> tableNumberColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> createdAtColumn;
    @FXML private TableColumn<Order, String> totalColumn;

    @FXML private TableView<OrderItem> orderItemsTable;
    @FXML private TableColumn<OrderItem, String> itemDishColumn;
    @FXML private TableColumn<OrderItem, Integer> itemQuantityColumn;
    @FXML private TableColumn<OrderItem, BigDecimal> itemPriceColumn;
    @FXML private TableColumn<OrderItem, String> itemSubtotalColumn;

    @FXML private TextArea orderDetailsArea;
    @FXML private Label orderTotalLabel;
    @FXML private Label orderStatusLabel;

    @FXML private Button acceptOrderButton;
    @FXML private Button completeOrderButton;
    @FXML private Button cancelOrderButton;

    private String userRole;
    private final OrderDAO orderDAO = new OrderDAO();
    private ObservableList<Order> ordersData;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableNumberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("Стол " + cellData.getValue().getTableNumber()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter)));

        totalColumn.setCellValueFactory(cellData -> {
            BigDecimal total = orderDAO.getOrderTotal(cellData.getValue().getId());
            return new SimpleStringProperty(total.toPlainString() + " ₽");
        });

        itemDishColumn.setCellValueFactory(new PropertyValueFactory<>("dishName"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceAtTime"));
        itemSubtotalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubtotal().toPlainString() + " ₽"));

        ordersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        showOrderDetails(selected);
                    } else {
                        clearOrderDetails();
                    }
                });
    }

    public void setMainController(MainController mainController, String userRole) {
        this.userRole = userRole;
        configureButtonsByRole();
        loadOrders();
    }

    private void configureButtonsByRole() {
        boolean isClient = "client".equals(userRole);
        acceptOrderButton.setVisible(!isClient);
        completeOrderButton.setVisible(!isClient);
        cancelOrderButton.setVisible(true);
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
        if (fullOrder == null) {
            showError("Не удалось загрузить детали заказа");
            return;
        }

        orderItemsTable.setItems(FXCollections.observableArrayList(fullOrder.getItems()));

        StringBuilder sb = new StringBuilder();
        sb.append("Заказ №").append(fullOrder.getId()).append("\n");
        sb.append("Стол: ").append(fullOrder.getTableNumber()).append("\n");

        if (fullOrder.getSpecialRequests() != null && !fullOrder.getSpecialRequests().trim().isEmpty()) {
            sb.append("Пожелания: ").append(fullOrder.getSpecialRequests().trim()).append("\n");
        }

        orderDetailsArea.setText(sb.toString());

        BigDecimal total = orderDAO.getOrderTotal(fullOrder.getId());
        orderTotalLabel.setText("Итого: " + total.toPlainString() + " ₽");
        orderStatusLabel.setText("Статус: " + getStatusText(fullOrder.getStatus()));
    }

    private void clearOrderDetails() {
        orderItemsTable.getItems().clear();
        orderDetailsArea.clear();
        orderTotalLabel.setText("Итого: —");
        orderStatusLabel.setText("Статус: —");
    }

    private String getStatusText(String status) {
        switch (status) {
            case "active":    return "Активен";
            case "completed": return "Завершён";
            case "cancelled": return "Отменён";
            default:          return status != null ? status : "—";
        }
    }

    @FXML private void handleAcceptOrder() {
        Order order = getSelectedActiveOrder();
        if (order == null) return;
        if (showConfirmation("Принять заказ №" + order.getId() + "?", "Заказ будет передан на кухню")) {
            showInfo("Заказ №" + order.getId() + " принят (логика принятия пока не реализована)");
            loadOrders();
        }
    }

    @FXML private void handleCompleteOrder() {
        Order order = getSelectedActiveOrder();
        if (order == null) return;
        if (showConfirmation("Завершить заказ №" + order.getId() + "?", "Заказ будет отмечен как выполненный")) {
            if (orderDAO.completeOrder(order.getId())) {
                showInfo("Заказ №" + order.getId() + " завершён");
                loadOrders();
                clearOrderDetails();
            } else {
                showError("Не удалось завершить заказ");
            }
        }
    }

    @FXML private void handleCancelOrder() {
        Order order = getSelectedActiveOrder();
        if (order == null) return;
        if (showConfirmation("Отменить заказ №" + order.getId() + "?", "Действие нельзя отменить")) {
            if (orderDAO.cancelOrder(order.getId())) {
                showInfo("Заказ №" + order.getId() + " отменён");
                loadOrders();
                clearOrderDetails();
            } else {
                showError("Не удалось отменить заказ");
            }
        }
    }

    private Order getSelectedActiveOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите заказ");
            return null;
        }
        if (!"active".equals(selected.getStatus())) {
            showWarning("Действие доступно только для активных заказов");
            return null;
        }
        return selected;
    }

    private boolean showConfirmation(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showWarning(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
    private void showInfo(String msg)    { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void showError(String msg)   { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}