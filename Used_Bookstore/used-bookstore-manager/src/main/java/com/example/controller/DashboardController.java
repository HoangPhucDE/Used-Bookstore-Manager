package com.example.controller;

import com.example.DatabaseConnection;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;

public class DashboardController {

    @FXML private VBox rootVBox;

    @FXML private Label totalBooksLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label todayBooksLabel;
    @FXML private Label todayUsersLabel;
    @FXML private Label todaySalesLabel;
    @FXML private Label todayRevenueLabel;
    @FXML private PieChart categoryChart;
    @FXML private AreaChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> bookChart;

    @FXML
    public void initialize() {
        fadeIn(rootVBox); // hiệu ứng fade-in toàn dashboard
        setupStatistics();
        setupCategoryChart();
        setupRevenueChart();
        setupBookChart();
    }

    /* ================= HIỆU ỨNG ================= */
    private void fadeIn(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(800), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.15);
        st.setToY(1.15);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    @FXML
    private void handleCardHoverEnter(javafx.scene.input.MouseEvent event) {
        Node card = (Node) event.getSource();
        card.setScaleX(1.05);
        card.setScaleY(1.05);
    }

    @FXML
    private void handleCardHoverExit(javafx.scene.input.MouseEvent event) {
        Node card = (Node) event.getSource();
        card.setScaleX(1.0);
        card.setScaleY(1.0);
    }

   

    private void setupStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Tổng sách
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM sach");
            if (rs.next()) totalBooksLabel.setText(String.valueOf(rs.getInt(1)));

            // Tổng người dùng
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM taikhoan WHERE loai_nguoi_dung = 'khachhang'");
            if (rs.next()) totalUsersLabel.setText(String.valueOf(rs.getInt(1)));

            // Tổng đơn hàng
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM donhang");
            if (rs.next()) totalSalesLabel.setText(String.valueOf(rs.getInt(1)));

            // Tổng doanh thu
            rs = conn.createStatement().executeQuery("SELECT SUM(tong_tien) FROM donhang");
            if (rs.next()) totalRevenueLabel.setText(String.format("%,d ₫", rs.getInt(1)));

            // Đơn hôm nay
            rs = conn.createStatement().executeQuery("SELECT COUNT(*), SUM(tong_tien) FROM donhang WHERE DATE(ngay_tao) = CURRENT_DATE");
            if (rs.next()) {
                todaySalesLabel.setText(String.valueOf(rs.getInt(1)));
                todayRevenueLabel.setText(String.format("%,d ₫", rs.getInt(2)));
            }

            // Sách bán hôm nay
            rs = conn.createStatement().executeQuery("""
                SELECT SUM(so_luong) FROM chitiet_donhang ct
                JOIN donhang dh ON ct.ma_don = dh.ma_don
                WHERE DATE(dh.ngay_tao) = CURRENT_DATE
            """);
            if (rs.next()) todayBooksLabel.setText(String.valueOf(rs.getInt(1)));

            // Người dùng mới hôm nay
            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM taikhoan WHERE DATE(ngay_dang_ky) = CURRENT_DATE");
            if (rs.next()) todayUsersLabel.setText(String.valueOf(rs.getInt(1)));

            // Hiệu ứng pulse cho label số liệu
            pulse(totalBooksLabel);
            pulse(totalUsersLabel);
            pulse(totalSalesLabel);
            pulse(totalRevenueLabel);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCategoryChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String sql = "SELECT the_loai, COUNT(*) AS so_luong FROM sach GROUP BY the_loai";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pieChartData.add(new PieChart.Data(rs.getString("the_loai"), rs.getInt("so_luong")));
            }

            categoryChart.setData(pieChartData);
            categoryChart.setTitle("Phân bố sách theo thể loại");
            fadeIn(categoryChart);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupRevenueChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        String sql = """
            SELECT DATE(ngay_tao) AS ngay, SUM(tong_tien) AS doanh_thu
            FROM donhang
            WHERE ngay_tao >= CURRENT_DATE - INTERVAL 6 DAY
            GROUP BY DATE(ngay_tao)
            ORDER BY ngay ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String date = rs.getString("ngay");
                int revenue = rs.getInt("doanh_thu");
                series.getData().add(new XYChart.Data<>(date, revenue));
            }

            revenueChart.getData().clear();
            revenueChart.getData().add(series);
            revenueChart.setTitle("Doanh thu 7 ngày qua");
            fadeIn(revenueChart);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupBookChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số lượng bán");

        String sql = """
            SELECT s.ten_sach, SUM(ct.so_luong) AS tong
            FROM chitiet_donhang ct
            JOIN sach s ON ct.ma_sach = s.ma_sach
            GROUP BY s.ten_sach
            ORDER BY tong DESC
            LIMIT 5
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("ten_sach"), rs.getInt("tong")));
            }

            bookChart.getData().clear();
            bookChart.getData().add(series);
            bookChart.setTitle("Top 5 sách bán chạy");
            fadeIn(bookChart);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshDashboard() {
        setupStatistics();
        setupCategoryChart();
        setupRevenueChart();
        setupBookChart();
        System.out.println("Dashboard đã được làm mới!");
    }

    @FXML
    private void exportReport() {
        System.out.println("Xuất báo cáo thống kê...");
    }

    @FXML
    private void viewDetailedStats() {
        System.out.println("Chuyển đến trang thống kê chi tiết...");
    }
}
