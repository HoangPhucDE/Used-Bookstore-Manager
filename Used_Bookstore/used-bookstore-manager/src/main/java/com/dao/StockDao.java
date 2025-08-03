package com.dao;

import com.example.model.StockEntry;
import com.example.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockDao {
    private static final Logger LOGGER = Logger.getLogger(StockDao.class.getName());

    public boolean addStockEntry(Connection conn, int bookId, int quantity, int userId) throws SQLException {
        // 1. Tạo phiếu nhập
        String insertPhieu = "INSERT INTO phieu_nhap (ngay_nhap, nguoi_tao_id, ghi_chu) VALUES (?, ?, ?)";
        try (PreparedStatement stmt1 = conn.prepareStatement(insertPhieu, Statement.RETURN_GENERATED_KEYS)) {
            stmt1.setDate(1, Date.valueOf(LocalDate.now()));
            stmt1.setInt(2, userId);
            stmt1.setString(3, "Nhập kho từ giao diện");
            stmt1.executeUpdate();

            ResultSet rs = stmt1.getGeneratedKeys();
            if (!rs.next()) throw new SQLException("Không tạo được phiếu nhập");
            int maPhieu = rs.getInt(1);

            // 2. Lấy giá nhập từ sách
            double donGia = getBookImportPrice(conn, bookId);
            if (donGia < 0) throw new SQLException("Không tìm thấy sách hoặc giá nhập không hợp lệ");

            // 3. Ghi chi tiết phiếu nhập
            String insertDetail = """
            INSERT INTO chitiet_phieunhap (ma_phieu, ma_sach, so_luong, don_gia)
            VALUES (?, ?, ?, ?)
        """;
            try (PreparedStatement stmt2 = conn.prepareStatement(insertDetail)) {
                stmt2.setInt(1, maPhieu);
                stmt2.setInt(2, bookId);
                stmt2.setInt(3, quantity);
                stmt2.setDouble(4, donGia);
                stmt2.executeUpdate();
            }

            // 4. Cập nhật tồn kho
            String updateStock = "UPDATE sach SET so_luong_ton = so_luong_ton + ? WHERE ma_sach = ?";
            try (PreparedStatement stmt3 = conn.prepareStatement(updateStock)) {
                stmt3.setInt(1, quantity);
                stmt3.setInt(2, bookId);
                stmt3.executeUpdate();
            }

            return true;
        }
    }
    public List<StockEntry> getAllStockEntries() {
        List<StockEntry> entries = new ArrayList<>();
        String sql = """
        SELECT pn.id AS phieu_id, s.ma_sach AS book_id, s.ten_sach,
               ctpn.so_luong, pn.ngay_nhap, tk.username AS nguoi_nhap,
               s.tinh_trang
        FROM phieu_nhap pn
        JOIN chitiet_phieunhap ctpn ON pn.id = ctpn.ma_phieu
        JOIN sach s ON ctpn.ma_sach = s.ma_sach
        JOIN taikhoan tk ON pn.nguoi_tao_id = tk.id
        ORDER BY pn.ngay_nhap DESC
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int phieuId = rs.getInt("phieu_id");
                int bookId = rs.getInt("book_id");
                String tenSach = rs.getString("ten_sach");
                int soLuong = rs.getInt("so_luong");
                LocalDate ngayNhap = rs.getDate("ngay_nhap").toLocalDate();
                String nguoiNhap = rs.getString("nguoi_nhap");
                String tinhTrangRaw = rs.getString("tinh_trang");

                // Ánh xạ tình trạng mã -> tên hiển thị
                String tinhTrang = mapCondition(tinhTrangRaw);

                StockEntry entry = new StockEntry(
                        phieuId,
                        bookId,
                        tenSach,
                        soLuong,
                        ngayNhap,
                        nguoiNhap,
                        tinhTrang
                );
                entries.add(entry);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi truy vấn danh sách phiếu nhập", e);
        }

        return entries;
    }


    public boolean insertStockEntry(int bookId, int quantity, int userId) {
        String sql = """
            INSERT INTO phieu_nhap (ma_sach, so_luong, ngay_nhap, nguoi_tao_id)
            VALUES (?, ?, CURRENT_DATE, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, quantity);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm phiếu nhập cho sách ID " + bookId, e);
            return false;
        }
    }

    private double getBookImportPrice(Connection conn, int bookId) throws SQLException {
        String sql = "SELECT gia_nhap FROM sach WHERE ma_sach = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("gia_nhap");
        }
        return -1;
    }

    public int getBookStock(int bookId) {
        String sql = "SELECT so_luong_ton FROM sach WHERE ma_sach = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("so_luong_ton");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertPhieuNhap(Connection conn, int userId) throws SQLException {
        String sql = "INSERT INTO phieu_nhap (nguoi_tao_id, ngay_nhap) VALUES (?, CURRENT_DATE)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();
            if (affected == 0) return -1;

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean insertChiTietPhieuNhap(Connection conn, int phieuId, int bookId, int quantity, double importPrice) throws SQLException {
        String sql = "INSERT INTO chitiet_phieunhap (ma_phieu, ma_sach, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, phieuId);
            stmt.setInt(2, bookId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, importPrice);
            return stmt.executeUpdate() > 0;
        }
    }
    public double getImportPrice(int bookId) {
        String sql = "SELECT gia_nhap FROM sach WHERE ma_sach = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("gia_nhap");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String mapCondition(String code) {
        return switch (code) {
            case "moi" -> "Mới";
            case "tot" -> "Tốt";
            case "cu" -> "Cũ";
            default -> "Không rõ";
        };
    }

    private static String toCode(String display) {
        return switch (display) {
            case "Mới" -> "moi";
            case "Tốt" -> "tot";
            case "Cũ" -> "cu";
            default -> "cu"; // mặc định nếu không rõ
        };
    }
}