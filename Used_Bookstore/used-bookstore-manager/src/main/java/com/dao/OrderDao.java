package com.dao;

import com.example.utils.DatabaseConnection;
import com.example.model.Order;

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
            e.printStackTrace();
        }

        return orders;
    }

    public int insertOrder(Connection conn, String tenKH, String sdt, String email, String diaChi, int nguoiTaoId, String loaiDon) throws SQLException {
        String sql = """
        INSERT INTO donhang (ten_kh, sdt, email, dia_chi, nguoi_tao_id, ngay_tao, loai_don, tong_tien)
        VALUES (?, ?, ?, ?, ?, NOW(), ?, 0)
    """;
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tenKH);
            stmt.setString(2, sdt);
            stmt.setString(3, email);
            stmt.setString(4, diaChi);
            stmt.setInt(5, nguoiTaoId);
            stmt.setString(6, loaiDon);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("Không lấy được ID đơn hàng.");
        }
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
