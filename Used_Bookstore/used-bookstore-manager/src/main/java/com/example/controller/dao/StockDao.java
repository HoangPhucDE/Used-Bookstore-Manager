package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.StockEntry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockDao {

    public List<StockEntry> getAllStockEntries() {
        List<StockEntry> entries = new ArrayList<>();
        String sql = """
            SELECT pn.id, pn.ma_sach, s.ten_sach, pn.so_luong, pn.ngay_nhap
            FROM phieu_nhap pn
            JOIN sach s ON pn.ma_sach = s.ma_sach
            ORDER BY pn.ngay_nhap DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StockEntry entry = new StockEntry(
                        rs.getInt("id"),
                        rs.getInt("ma_sach"),
                        rs.getString("ten_sach"),
                        rs.getInt("so_luong"),
                        rs.getDate("ngay_nhap").toLocalDate()
                );
                entries.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public boolean insertStockEntry(int bookId, int quantity) {
        String sql = "INSERT INTO phieu_nhap (ma_sach, so_luong, ngay_nhap) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, quantity);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
