package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.RevenueByDate;
import com.example.model.RevenueByBook;
import com.example.model.RevenueByEmployee;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.FileOutputStream;
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
                LocalDate date = rs.getDate("ngay").toLocalDate();
                int count = rs.getInt("so_hoa_don");
                double total = rs.getDouble("tong_tien");
                revenueByDateList.add(new RevenueByDate(date, count, total));
            }

            timeTable.setItems(revenueByDateList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải doanh thu theo ngày.");
        }
    }

    private void loadRevenueByBook() {
        revenueByBookList.clear();
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
                String bookName = rs.getString("ten_sach");
                int quantity = rs.getInt("so_luong");
                revenueByBookList.add(new RevenueByBook(bookName, quantity));
            }

            bookTable.setItems(revenueByBookList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải thống kê sách bán chạy.");
        }
    }

    private void loadRevenueByEmployee() {
        revenueByEmployeeList.clear();
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
                String name = rs.getString("ho_ten");
                int invoiceCount = rs.getInt("so_hoa_don");
                double revenue = rs.getDouble("tong_tien");
                revenueByEmployeeList.add(new RevenueByEmployee(name, revenue, invoiceCount));
            }

            employeeTable.setItems(revenueByEmployeeList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải thống kê theo nhân viên.");
        }
    }

    @FXML
    public void handleExportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu file PDF");
            fileChooser.setInitialFileName("bao_cao_doanh_thu.pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(null);
            if (file == null) return;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            document.add(new Paragraph("📅 BÁO CÁO DOANH THU THEO THỜI GIAN\n\n"));

            PdfPTable table = new PdfPTable(3);
            table.addCell("Ngày");
            table.addCell("Số hóa đơn");
            table.addCell("Tổng doanh thu");

            for (RevenueByDate entry : revenueByDateList) {
                table.addCell(entry.getDate().toString());
                table.addCell(String.valueOf(entry.getInvoiceCount()));
                table.addCell(String.format("%.0f", entry.getTotalRevenue()));
            }

            document.close();
            showAlert("Thành công", "Xuất PDF thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Xuất PDF thất bại.");
        }
    }

    @FXML
    public void handleExportExcel() {
        showAlert("Thông báo", "Chức năng xuất Excel đang được phát triển.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
