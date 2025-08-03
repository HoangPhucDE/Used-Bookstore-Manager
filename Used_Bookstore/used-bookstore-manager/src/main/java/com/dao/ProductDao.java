package com.dao;

import com.example.utils.DatabaseConnection;
import com.example.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    // Lấy tất cả sách còn tồn kho
    public List<Product> getAllAvailableProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM sach WHERE so_luong_ton > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productList.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productList;
    }

    // Lọc sách theo thể loại
    public List<Product> getProductsByCategory(String category) {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM sach WHERE the_loai = ? AND so_luong_ton > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                productList.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productList;
    }

    // Tìm sách theo ID
    public Product findById(int id) {
        String sql = "SELECT * FROM sach WHERE ma_sach = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Cập nhật tồn kho sau khi bán sách
    public void updateStockAfterOrder(int productId, int quantitySold) {
        String sql = "UPDATE sach SET so_luong_ton = so_luong_ton - ? WHERE ma_sach = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantitySold);
            stmt.setInt(2, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hàm ánh xạ ResultSet -> Product object
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("ma_sach"),
                rs.getString("ten_sach"),
                rs.getString("tac_gia"),
                rs.getString("the_loai"),
                rs.getString("nxb"),
                rs.getInt("nam_xb"),
                rs.getDouble("gia_nhap"),
                rs.getDouble("gia_ban"),
                rs.getString("tinh_trang"),
                rs.getString("hinh_anh"),
                rs.getInt("so_luong_ton"),
                rs.getDouble("danh_gia")
        );
    }
}
