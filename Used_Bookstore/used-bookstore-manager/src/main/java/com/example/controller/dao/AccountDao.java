package com.example.controller.dao;

import java.sql.*;

public class AccountDao {

    // ✅ Tìm ID tài khoản theo username (dùng kết nối bên ngoài)
    public Integer findAccountIdByUsername(String username) {
        String sql = "SELECT id FROM taikhoan WHERE username = ?";
        try (Connection conn = com.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Kiểm tra email tồn tại (dùng kết nối bên ngoài)
    public boolean isEmailExists(String email) {
        String sql = "SELECT id FROM taikhoan WHERE email = ?";
        try (Connection conn = com.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Tạo tài khoản mới — dùng `Connection conn` truyền từ ngoài
    public int createNewCustomerAccount(Connection conn, String username, String password, String email) throws SQLException {
        String sql = """
            INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai, ngay_dang_ky)
            VALUES (?, ?, 'khach', 'khachhang', ?, TRUE, NOW())
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy ID tài khoản vừa tạo.");
            }
        }
    }
}
