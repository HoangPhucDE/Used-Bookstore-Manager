package com.example.utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConnected {
    public static void main(String[] args) {
        String username = "admin_quyen";
        String password = "123456@Admin";  // Giả sử chưa mã hóa

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM taikhoan WHERE username = ? AND mat_khau = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Đăng nhập thành công: " + rs.getString("vai_tro"));
            } else {
                System.out.println("❌ Sai tài khoản hoặc mật khẩu");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
        }
    }
}