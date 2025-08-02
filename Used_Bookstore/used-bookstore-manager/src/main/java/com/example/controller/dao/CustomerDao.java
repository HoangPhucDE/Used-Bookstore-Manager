package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.Customer;

import java.sql.*;

public class CustomerDao {

    /**
     * ✅ Kiểm tra khách hàng đã tồn tại theo ID tài khoản
     */
    public boolean customerExistsByAccountId(int accountId) {
        String sql = "SELECT ma_kh FROM khachhang WHERE id_taikhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ Thêm khách hàng mới gắn với tài khoản (dùng Connection bên ngoài)
     */
    public void insertCustomer(Connection conn, String name, String email, String phone, String address, int accountId) throws SQLException {
        String sql = """
            INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setInt(5, accountId);
            stmt.executeUpdate();
        }
    }

    /**
     * ✅ Tìm thông tin khách hàng theo SĐT
     */
    public Customer findCustomerByPhone(String sdt) {
        String sql = "SELECT ho_ten, email, dia_chi FROM khachhang WHERE sdt = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sdt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        0,
                        rs.getString("ho_ten"),
                        rs.getString("email"),
                        sdt,
                        rs.getString("dia_chi"),
                        "đang hoạt động"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
