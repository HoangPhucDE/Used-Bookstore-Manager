package com.dao;

import com.example.utils.DatabaseConnection;
import com.example.model.RevenueByBook;
import com.example.model.RevenueByDate;
import com.example.model.RevenueByEmployee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDao {

    public static List<RevenueByDate> getRevenueByDateList() throws SQLException {
        List<RevenueByDate> list = new ArrayList<>();
        String sql = """
            SELECT DATE(ngay_tao) AS ngay, COUNT(*) AS so_hoa_don, SUM(tong_tien) AS tong_tien
            FROM donhang
            WHERE trang_thai = 'hoan_thanh'
            GROUP BY DATE(ngay_tao)
            ORDER BY ngay DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new RevenueByDate(
                        rs.getDate("ngay").toLocalDate(),
                        rs.getInt("so_hoa_don"),
                        rs.getDouble("tong_tien")
                ));
            }
        }
        return list;
    }

    public static List<RevenueByBook> getRevenueByBookList() throws SQLException {
        List<RevenueByBook> list = new ArrayList<>();
        String sql = """
            SELECT s.ten_sach, SUM(ct.so_luong) AS so_luong
            FROM chitiet_donhang ct
            JOIN sach s ON ct.ma_sach = s.ma_sach
            JOIN donhang d ON ct.ma_don = d.ma_don
            WHERE d.trang_thai = 'hoan_thanh'
            GROUP BY s.ten_sach
            ORDER BY so_luong DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new RevenueByBook(
                        rs.getString("ten_sach"),
                        rs.getInt("so_luong")
                ));
            }
        }
        return list;
    }

    public static List<RevenueByEmployee> getRevenueByEmployeeList() throws SQLException {
        List<RevenueByEmployee> list = new ArrayList<>();
        String sql = """
            SELECT nv.ho_ten, COUNT(d.ma_don) AS so_hoa_don, SUM(d.tong_tien) AS tong_tien
            FROM donhang d
            JOIN taikhoan tk ON d.nguoi_tao_id = tk.id
            JOIN nhanvien nv ON tk.id = nv.id_taikhoan
            WHERE d.trang_thai = 'hoan_thanh'
            GROUP BY nv.ho_ten
            ORDER BY tong_tien DESC;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new RevenueByEmployee(
                        rs.getString("ho_ten"),
                        rs.getDouble("tong_tien"),
                        rs.getInt("so_hoa_don")
                ));
            }
        }
        return list;
    }
}
