package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.Order;
import com.example.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT ma_don, ten_kh, sdt, email, dia_chi, tong_tien, ngay_tao, loai_don, trang_thai FROM donhang ORDER BY ngay_tao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("ma_don"),
                        rs.getString("ten_kh"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("dia_chi"),
                        rs.getString("loai_don"),
                        rs.getString("trang_thai"),
                        rs.getString("ngay_tao")
                );
                order.setTotal(calculateTotalForOrder(order.getId()));
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Có thể ghi log nếu cần
        }

        return orders;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE donhang SET trang_thai = ? WHERE ma_don = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double calculateTotalForOrder(int orderId) {
        String sql = "SELECT SUM(so_luong * don_gia) AS tong FROM chitiet_donhang WHERE ma_don = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("tong");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
