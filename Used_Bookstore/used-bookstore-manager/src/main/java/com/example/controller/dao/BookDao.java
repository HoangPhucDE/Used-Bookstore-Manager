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
     * Lấy tất cả sách trong cơ sở dữ liệu
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM sach";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * Thêm sách mới
     */
    public boolean insertBook(Book b) {
        String sql = """
            INSERT INTO sach (ten_sach, tac_gia, the_loai, gia_nhap, gia_ban,
                              so_luong_ton, danh_gia, hinh_anh)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, b.getTitle());
            stmt.setString(2, b.getAuthor());
            stmt.setString(3, b.getCategory());
            stmt.setDouble(4, b.getImportPrice());
            stmt.setDouble(5, b.getPrice());
            stmt.setInt(6, b.getStock());
            stmt.setDouble(7, b.getRating());
            stmt.setString(8, b.getImagePath());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin sách
     */
    public boolean updateBook(Book b) {
        String sql = """
            UPDATE sach SET ten_sach=?, tac_gia=?, the_loai=?, gia_nhap=?, gia_ban=?,
                            so_luong_ton=?, danh_gia=?, hinh_anh=?
            WHERE ma_sach=?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, b.getTitle());
            stmt.setString(2, b.getAuthor());
            stmt.setString(3, b.getCategory());
            stmt.setDouble(4, b.getImportPrice());
            stmt.setDouble(5, b.getPrice());
            stmt.setInt(6, b.getStock());
            stmt.setDouble(7, b.getRating());
            stmt.setString(8, b.getImagePath());
            stmt.setInt(9, b.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xoá sách theo ID
     */
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM sach WHERE ma_sach = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm tiện ích chuyển ResultSet thành Book
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
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
