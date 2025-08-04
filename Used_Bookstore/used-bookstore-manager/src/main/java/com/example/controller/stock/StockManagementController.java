package com.example.controller.stock;

import com.dao.BookDao;
import com.dao.StockDao;
import com.example.controller.auth.LoginController;
import com.example.model.StockEntry;
import com.example.utils.BookDetailUtil;
import com.example.utils.DatabaseConnection;
import com.example.utils.StockDialogUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockManagementController {

    @FXML private TableView<StockEntry> stockTable;
    @FXML private TableColumn<StockEntry, Integer> colId;
    @FXML private TableColumn<StockEntry, String> colBookTitle;
    @FXML private TableColumn<StockEntry, Integer> colQuantity;
    @FXML private TableColumn<StockEntry, String> colDate;
    @FXML private TableColumn<StockEntry, String> colCreatedBy;
    @FXML private TableColumn<StockEntry, String> colCondition;
    @FXML private Button addStockBtn;

    private final StockDao stockDao = new StockDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
        colBookTitle.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getBookTitle()));
        colQuantity.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());
        colDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEntryDate().toString()));
        colCreatedBy.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCreatedBy()));

        colCondition.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getCondition()));

        loadStockEntries();

        stockTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && stockTable.getSelectionModel().getSelectedItem() != null) {
                StockEntry selected = stockTable.getSelectionModel().getSelectedItem();
                BookDao bookDao = new BookDao();
                com.example.model.Book book = bookDao.findBookById(selected.getBookId());
                BookDetailUtil.showBookDetail(book);
            }
        });
        // Tránh null ngay từ lần đầu gọi
        addStockBtn.setOnAction(event -> handleAddStockInternal(new ArrayList<>()));
    }

    private void handleAddStockInternal(List<StockEntry> prefillEntries) {
        StockDialogUtil.showMultiStockDialog(prefillEntries).ifPresent(entries -> {
            if (entries == null || entries.isEmpty()) {
                showInfo("Chưa có sách nào được chọn để nhập.");
                return;
            }

            StringBuilder content = new StringBuilder();
            for (StockEntry entry : entries) {
                content.append("• ").append(entry.getBookTitle())
                        .append(" - SL: ").append(entry.getQuantity()).append("\n");
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận nhập kho");
            confirm.setHeaderText("Bạn có chắc muốn nhập các sách sau?");
            confirm.setContentText(content.toString());

            ButtonType okBtn = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Huỷ", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(okBtn, cancelBtn);

            confirm.showAndWait().ifPresent(choice -> {
                if (choice == okBtn) {
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        conn.setAutoCommit(false);

                        int phieuId = stockDao.insertPhieuNhap(conn, LoginController.currentUserId);
                        if (phieuId == -1) {
                            conn.rollback();
                            showError("Không thể tạo phiếu nhập.");
                            return;
                        }

                        boolean success = true;
                        BookDao bookDao = new BookDao();
                        for (StockEntry entry : entries) {
                            double importPrice = bookDao.getImportPrice(entry.getBookId());
                            boolean inserted = stockDao.insertChiTietPhieuNhap(conn, phieuId, entry.getBookId(), entry.getQuantity(), importPrice);
                            if (!inserted) {
                                success = false;
                                break;
                            }
                        }

                        if (success) {
                            conn.commit();
                            showSuccess("Đã nhập kho thành công cho " + entries.size() + " sách.");
                            loadStockEntries();
                        } else {
                            conn.rollback();
                            showError("Lỗi khi nhập chi tiết. Giao dịch đã bị huỷ.");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        showError("Lỗi SQL: " + e.getMessage());
                    }
                } else {
                    showInfo("Bạn đã huỷ nhập kho.");
                    // Gọi lại dialog với dữ liệu cũ
                    Platform.runLater(() -> handleAddStockInternal(entries));
                }
            });
        });
    }

    private void loadStockEntries() {
        ObservableList<StockEntry> entries = FXCollections.observableArrayList(stockDao.getAllStockEntries());
        stockTable.setItems(entries);
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle("Thông báo");
        alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Lỗi");
        alert.show();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setTitle("Thành công");
        alert.show();
    }
}
