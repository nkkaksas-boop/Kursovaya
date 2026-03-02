package nntc.svg52.fxapp.model.dao;

import nntc.svg52.fxapp.model.DatabaseManager;
import nntc.svg52.fxapp.model.entities.Order;
import nntc.svg52.fxapp.model.entities.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {


    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении статуса заказа #" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public int createOrder(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return -1;
        }

        String insertOrderSql =
                "INSERT INTO orders (user_id, table_number, status, special_requests) " +
                        "VALUES (?, ?, ?, ?) RETURNING id";

        String insertItemSql =
                "INSERT INTO order_items (order_id, dish_id, quantity, price_at_time, notes) " +
                        "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            int orderId;

            // 1. Создание заказа
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {
                pstmt.setInt(1, order.getUserId());
                pstmt.setInt(2, order.getTableNumber());
                pstmt.setString(3, order.getStatus());
                pstmt.setString(4, order.getSpecialRequests() != null ? order.getSpecialRequests() : "");

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Не удалось получить ID нового заказа");
                    }
                    orderId = rs.getInt("id");
                }
            }

            // 2. Добавление позиций заказа
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : order.getItems()) {
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getDishId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getPriceAtTime());
                    itemStmt.setString(5, item.getNotes() != null ? item.getNotes() : "");
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка отката транзакции: " + ex.getMessage());
                }
            }
            return -1;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                    // silent close
                }
            }
        }
    }

    public List<Order> getActiveOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.full_name as user_name " +
                        "FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "WHERE o.status = 'active' AND o.user_id = ? " +
                        "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setUserName(rs.getString("user_name"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении активных заказов пользователя " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }


    public BigDecimal getOrderTotal(int orderId) {
        String sql =
                "SELECT COALESCE(SUM(quantity * price_at_time), 0) as total " +
                        "FROM order_items " +
                        "WHERE order_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка расчёта суммы заказа #" + orderId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }


    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setItems(getOrderItems(id));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql =
                "SELECT oi.*, d.name as dish_name " +
                        "FROM order_items oi " +
                        "JOIN dishes d ON oi.dish_id = d.id " +
                        "WHERE oi.order_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setDishId(rs.getInt("dish_id"));
                    item.setDishName(rs.getString("dish_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPriceAtTime(rs.getBigDecimal("price_at_time"));
                    item.setNotes(rs.getString("notes"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Вспомогательный метод извлечения заказа из ResultSet
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTableNumber(rs.getInt("table_number"));
        order.setStatus(rs.getString("status"));
        order.setSpecialRequests(rs.getString("special_requests"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'active' ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(extractOrderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public boolean completeOrder(int orderId) {
        return updateOrderStatus(orderId, "completed");
    }

    public boolean cancelOrder(int orderId) {
        return updateOrderStatus(orderId, "cancelled");
    }
}