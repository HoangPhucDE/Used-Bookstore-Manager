package com.dao;

import com.example.utils.DatabaseConnection;
import com.example.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDao {

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
        SELECT s.ma_sach, s.ten_sach, c.so_luong, c.don_gia
        FROM chitiet_donhang c
        JOIN sach s ON c.ma_sach = s.ma_sach
        WHERE c.ma_don = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("ma_sach");
                String tenSach = rs.getString("ten_sach");
                int soLuong = rs.getInt("so_luong");
                double donGia = rs.getDouble("don_gia");

                OrderItem item = new OrderItem(bookId, tenSach, soLuong, donGia);
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace(); //
        }
        return items;
    }
    public void insertOrderItems(Connection conn, int orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO chitiet_donhang (ma_don, ma_sach, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getBookId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getUnitPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
