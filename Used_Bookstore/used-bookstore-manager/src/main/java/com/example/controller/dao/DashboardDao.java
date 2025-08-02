package com.example.controller.dao;

import com.example.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.sql.*;

public class DashboardDao {

    public static int getTotalBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sach";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM taikhoan WHERE loai_nguoi_dung = 'khachhang'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTotalSales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donhang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(tong_tien) FROM donhang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTodaySales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donhang WHERE DATE(ngay_tao) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTodayRevenue() throws SQLException {
        String sql = "SELECT SUM(tong_tien) FROM donhang WHERE DATE(ngay_tao) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTodaySoldBooks() throws SQLException {
        String sql = """
                SELECT SUM(so_luong) FROM chitiet_donhang ct
                JOIN donhang dh ON ct.ma_don = dh.ma_don
                WHERE DATE(dh.ngay_tao) = CURRENT_DATE
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static int getTodayNewUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM taikhoan WHERE DATE(ngay_dang_ky) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static ObservableList<PieChart.Data> getCategoryChartData() throws SQLException {
        String sql = "SELECT the_loai, COUNT(*) AS so_luong FROM sach GROUP BY the_loai";
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                data.add(new PieChart.Data(rs.getString("the_loai"), rs.getInt("so_luong")));
            }
        }
        return data;
    }

    public static XYChart.Series<String, Number> getRevenueChartData() throws SQLException {
        String sql = """
            SELECT DATE(ngay_tao) AS ngay, SUM(tong_tien) AS doanh_thu
            FROM donhang
            WHERE ngay_tao >= CURRENT_DATE - INTERVAL 6 DAY
            GROUP BY DATE(ngay_tao)
            ORDER BY ngay ASC
        """;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("ngay"), rs.getInt("doanh_thu")));
            }
        }
        return series;
    }

    public static XYChart.Series<String, Number> getTopSellingBooksData() throws SQLException {
        String sql = """
            SELECT s.ten_sach, SUM(ct.so_luong) AS tong
            FROM chitiet_donhang ct
            JOIN sach s ON ct.ma_sach = s.ma_sach
            GROUP BY s.ten_sach
            ORDER BY tong DESC
            LIMIT 5
        """;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng bán");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("ten_sach"), rs.getInt("tong")));
            }
        }
        return series;
    }
}
