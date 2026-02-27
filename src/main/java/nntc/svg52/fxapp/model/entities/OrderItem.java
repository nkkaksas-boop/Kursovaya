package nntc.svg52.fxapp.model.entities;

import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int orderId;
    private int dishId;
    private String dishName;
    private int quantity;
    private BigDecimal priceAtTime;
    private String notes;

    public OrderItem() {}

    public OrderItem(int id, int orderId, int dishId, String dishName,
                     int quantity, BigDecimal priceAtTime, String notes) {
        this.id = id;
        this.orderId = orderId;
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.notes = notes;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }

    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtTime() { return priceAtTime; }
    public void setPriceAtTime(BigDecimal priceAtTime) { this.priceAtTime = priceAtTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getSubtotal() {
        return priceAtTime.multiply(BigDecimal.valueOf(quantity));
    }
}