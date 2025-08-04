package com.example.controller.dashboard;

import com.dao.DashboardDao;
import com.example.utils.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

public class DashboardController {

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
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        setupStatistics();
        setupCategoryChart();
        setupRevenueChart();
        setupBookChart();
        System.out.println("Dashboard đã được làm mới!");
    }

    private void setupStatistics() {
        try {
            totalBooksLabel.setText(String.valueOf(DashboardDao.getTotalBooks()));
            totalUsersLabel.setText(String.valueOf(DashboardDao.getTotalUsers()));
            totalSalesLabel.setText(String.valueOf(DashboardDao.getTotalSales()));
            totalRevenueLabel.setText(CurrencyFormatter.format(DashboardDao.getTotalRevenue()));
            todaySalesLabel.setText(String.valueOf(DashboardDao.getTodaySales()));
            todayRevenueLabel.setText(CurrencyFormatter.format(DashboardDao.getTodayRevenue()));
            todayBooksLabel.setText(String.valueOf(DashboardDao.getTodaySoldBooks()));
            todayUsersLabel.setText(String.valueOf(DashboardDao.getTodayNewUsers()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCategoryChart() {
        try {
            categoryChart.setData(DashboardDao.getCategoryChartData());
            categoryChart.setTitle("Phân bố sách theo thể loại");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRevenueChart() {
        try {
            revenueChart.getData().clear();
            revenueChart.getData().add(DashboardDao.getRevenueChartData());
            revenueChart.setTitle("Doanh thu 7 ngày qua");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBookChart() {
        try {
            bookChart.getData().clear();
            bookChart.getData().add(DashboardDao.getTopSellingBooksData());
            bookChart.setTitle("Top 5 sách bán chạy");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
