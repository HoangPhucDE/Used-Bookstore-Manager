package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookDao {
    public Book findBookById(int bookId) {
        String sql = "SELECT * FROM sach WHERE ma_sach = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Book(
                        rs.getInt("ma_sach"),
                        rs.getString("ten_sach"),
                        rs.getString("tac_gia"),
                        rs.getString("the_loai"),
                        rs.getString("nxb"),
                        rs.getInt("nam_xb"),
                        rs.getDouble("gia_nhap"),
                        rs.getDouble("gia_ban"),
                        rs.getString("tinh_trang"),
                        rs.getInt("so_luong_ton"),
                        rs.getDouble("danh_gia"),
                        rs.getString("hinh_anh")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Có thể log lỗi nếu cần
        }
        return null;
    }
}
