package com.example.controller.statistcs;

import com.dao.StatisticsDao;
import com.example.model.RevenueByDate;
import com.example.model.RevenueByBook;
import com.example.model.RevenueByEmployee;
import com.example.utils.PdfExportUtils;
import com.example.utils.ExcelExportUtils;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class StatisticsController {

    // === 📅 Theo thời gian ===
    @FXML private TableView<RevenueByDate> timeTable;
    @FXML private TableColumn<RevenueByDate, LocalDate> dateCol;
    @FXML private TableColumn<RevenueByDate, Integer> invoiceCountCol;
    @FXML private TableColumn<RevenueByDate, Double> totalRevenueCol;

    // === 📚 Theo sản phẩm ===
    @FXML private TableView<RevenueByBook> bookTable;
    @FXML private TableColumn<RevenueByBook, String> bookNameCol;
    @FXML private TableColumn<RevenueByBook, Integer> quantityCol;

    // === 👥 Theo nhân viên ===
    @FXML private TableView<RevenueByEmployee> employeeTable;
    @FXML private TableColumn<RevenueByEmployee, String> employeeNameCol;
    @FXML private TableColumn<RevenueByEmployee, Double> employeeRevenueCol;
    @FXML private TableColumn<RevenueByEmployee, Integer> employeeInvoiceCol;

    // Dữ liệu
    private final ObservableList<RevenueByDate> revenueByDateList = FXCollections.observableArrayList();
    private final ObservableList<RevenueByBook> revenueByBookList = FXCollections.observableArrayList();
    private final ObservableList<RevenueByEmployee> revenueByEmployeeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // === Setup cột bảng "Theo thời gian"
        dateCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));
        invoiceCountCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInvoiceCount()).asObject());
        totalRevenueCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalRevenue()).asObject());

        // === Setup cột bảng "Theo sản phẩm"
        bookNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookName()));
        quantityCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        // === Setup cột bảng "Theo nhân viên"
        employeeNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmployeeName()));
        employeeRevenueCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getRevenue()).asObject());
        employeeInvoiceCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInvoiceCount()).asObject());

        loadRevenueByDate();
        loadRevenueByBook();
        loadRevenueByEmployee();
    }

    private void loadRevenueByDate() {
        revenueByDateList.clear();
        try {
            revenueByDateList.addAll(StatisticsDao.getRevenueByDateList());
            timeTable.setItems(revenueByDateList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải doanh thu theo ngày.");
        }
    }

    private void loadRevenueByBook() {
        revenueByBookList.clear();
        try {
            revenueByBookList.addAll(StatisticsDao.getRevenueByBookList());
            bookTable.setItems(revenueByBookList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải thống kê sách bán chạy.");
        }
    }

    private void loadRevenueByEmployee() {
        revenueByEmployeeList.clear();
        try {
            revenueByEmployeeList.addAll(StatisticsDao.getRevenueByEmployeeList());
            employeeTable.setItems(revenueByEmployeeList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải thống kê theo nhân viên.");
        }
    }

    @FXML
    public void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel thống kê");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("ThongKeDoanhThu.xlsx");

        File file = fileChooser.showSaveDialog(timeTable.getScene().getWindow());
        if (file != null) {
            try {
                ExcelExportUtils.exportStatisticsToExcel(
                        file,
                        timeTable.getItems(),
                        bookTable.getItems(),
                        employeeTable.getItems()
                );
                showSuccess("Đã xuất file Excel thành công.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi khi xuất file Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file PDF thống kê");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("ThongKeDoanhThu.pdf");

        File file = fileChooser.showSaveDialog(timeTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportUtils.exportStatistics(
                        file,
                        timeTable.getItems(),
                        bookTable.getItems(),
                        employeeTable.getItems()
                );
                showAlert("✅ Thành công", "Đã xuất file PDF thống kê.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("❌ Lỗi", "Xuất file PDF thất bại: " + e.getMessage());
            }
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("❌ Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
