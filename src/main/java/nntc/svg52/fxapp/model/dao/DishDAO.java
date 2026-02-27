package nntc.svg52.fxapp.model.dao;

import nntc.svg52.fxapp.model.DatabaseManager;
import nntc.svg52.fxapp.model.entities.Dish;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {

    public List<Dish> getAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dishes ORDER BY category, name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dishes.add(extractDishFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dishes;
    }

    public List<Dish> getAvailableDishes() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dishes WHERE is_available = true ORDER BY category, name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dishes.add(extractDishFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dishes;
    }

    public Dish getDishById(int id) {
        String sql = "SELECT * FROM dishes WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractDishFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createDish(Dish dish) {
        String sql = "INSERT INTO dishes (name, description, price, category, is_available) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, dish.getName());
            pstmt.setString(2, dish.getDescription());
            pstmt.setBigDecimal(3, dish.getPrice());
            pstmt.setString(4, dish.getCategory());
            pstmt.setBoolean(5, dish.isAvailable());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    dish.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDish(Dish dish) {
        String sql = "UPDATE dishes SET name = ?, description = ?, price = ?, category = ?, is_available = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dish.getName());
            pstmt.setString(2, dish.getDescription());
            pstmt.setBigDecimal(3, dish.getPrice());
            pstmt.setString(4, dish.getCategory());
            pstmt.setBoolean(5, dish.isAvailable());
            pstmt.setInt(6, dish.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDish(int dishId) {
        // Запрос подтверждения на уровне UI
        String sql = "DELETE FROM dishes WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, dishId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleAvailability(int dishId, boolean isAvailable) {
        String sql = "UPDATE dishes SET is_available = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, dishId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Dish extractDishFromResultSet(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDescription(rs.getString("description"));
        dish.setPrice(rs.getBigDecimal("price"));
        dish.setCategory(rs.getString("category"));
        dish.setAvailable(rs.getBoolean("is_available"));
        dish.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return dish;
    }
}