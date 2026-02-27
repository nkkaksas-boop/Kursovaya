package nntc.svg52.fxapp.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Dish {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean isAvailable;
    private LocalDateTime createdAt;

    // Конструкторы
    public Dish() {}

    public Dish(int id, String name, String description, BigDecimal price,
                String category, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = isAvailable;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " - " + price + " руб.";
    }
}