package nntc.svg52.fxapp.model.dao;

import nntc.svg52.fxapp.model.DatabaseManager;
import nntc.svg52.fxapp.model.entities.Order;
import nntc.svg52.fxapp.model.entities.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.full_name as user_name " +
                        "FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setUserName(rs.getString("user_name"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String sql =
                "SELECT o.*, u.full_name as user_name " +
                        "FROM orders o " +
                        "JOIN users u ON o.user_id = u.id " +
                        "WHERE o.status = 'active' " +
                        "ORDER BY o.created_at";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setUserName(rs.getString("user_name"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                // Загружаем позиции заказа
                order.setItems(getOrderItems(id));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, table_number, status, special_requests) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, order.getUserId());
            pstmt.setInt(2, order.getTableNumber());
            pstmt.setString(3, order.getStatus());
            pstmt.setString(4, order.getSpecialRequests());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    // Сохраняем позиции заказа
                    for (OrderItem item : order.getItems()) {
                        item.setOrderId(orderId);
                        addOrderItem(item);
                    }
                    return orderId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET status = ?, special_requests = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, order.getStatus());
            pstmt.setString(2, order.getSpecialRequests());
            pstmt.setInt(3, order.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'cancelled' WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean completeOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'completed' WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, dish_id, quantity, price_at_time, notes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getOrderId());
            pstmt.setInt(2, item.getDishId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getPriceAtTime());
            pstmt.setString(5, item.getNotes());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            ResultSet rs = pstmt.executeQuery();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public BigDecimal getOrderTotal(int orderId) {
        String sql = "SELECT get_order_total(?) as total";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setTableNumber(rs.getInt("table_number"));
        order.setStatus(rs.getString("status"));
        order.setSpecialRequests(rs.getString("special_requests"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return order;
    }
}