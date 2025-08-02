package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.Book;
import com.example.model.OrderItem;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookDao {

    public List<Book> getAvailableBooks() {
        List<Book> books = new ArrayList<>();
        String sql = """
            SELECT ma_sach, ten_sach, tac_gia, the_loai, nxb, nam_xb,
                   gia_nhap, gia_ban, tinh_trang, so_luong_ton, danh_gia, hinh_anh
            FROM sach
            WHERE so_luong_ton > 0
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Book book = new Book(
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
                books.add(book);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Hoặc log lỗi
        }

        return books;
    }

    /**
     * Tìm sách theo ID (dùng khi cần hiện chi tiết sách)
     */
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
            e.printStackTrace(); // log lỗi
        }
        return null;
    }

    /**
     * Cập nhật tồn kho sau khi bán sách
     */
    public boolean updateStock(int bookId, int quantityToSubtract) {
        String sql = "UPDATE sach SET so_luong_ton = so_luong_ton - ? WHERE ma_sach = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantityToSubtract);
            stmt.setInt(2, bookId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStockAfterOrder(Connection conn, List<OrderItem> items) throws SQLException {
        String sql = "UPDATE sach SET so_luong_ton = so_luong_ton - ? WHERE ma_sach = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                stmt.setInt(1, item.getQuantity());
                stmt.setInt(2, item.getBookId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

}
