package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Book;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.utils.BookDialogUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderStatusController {

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, String> colPhone;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colCreatedDate;
    @FXML private TableColumn<Order, String> colOrderType;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Order> colAction;

    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private TextField searchField;
    @FXML private Button updateStatusBtn;

    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private Order selectedOrder;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterControls();
        loadOrders();
        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Order clickedOrder = row.getItem();
                    showOrderDetails(clickedOrder);
                }
            });
            return row;
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colCustomerName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerName()));
        colPhone.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPhone()));
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotal()));
        colCreatedDate.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCreatedDate()));
        colOrderType.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getOrderType()));
        colStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));

        colAction.setCellFactory(getDetailButtonCellFactory());

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedOrder = newVal;
            if (newVal != null) statusCombo.setValue(newVal.getStatus());
        });
    }

    private Callback<TableColumn<Order, Order>, TableCell<Order, Order>> getDetailButtonCellFactory() {
        return col -> new TableCell<>() {
            private final Button detailBtn = new Button("👁️");

            {
                detailBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                detailBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    showOrderDetails(order);
                });
            }

            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : detailBtn);
            }
        };
    }

    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng #" + order.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<OrderItem> table = new TableView<>();
        table.setPrefHeight(250);

        TableColumn<OrderItem, String> colTitle = new TableColumn<>("Tên sách");
        colTitle.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBookTitle()));
        colTitle.setPrefWidth(200);

        TableColumn<OrderItem, Integer> colQuantity = new TableColumn<>("Số lượng");
        colQuantity.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getQuantity()));
        colQuantity.setPrefWidth(80);

        TableColumn<OrderItem, Double> colUnitPrice = new TableColumn<>("Đơn giá");
        colUnitPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUnitPrice()));
        colUnitPrice.setPrefWidth(100);

        TableColumn<OrderItem, Double> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalPrice()));
        colTotal.setPrefWidth(120);

        table.getColumns().addAll(colTitle, colQuantity, colUnitPrice, colTotal);

        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        double total = 0;

        String sql = "SELECT s.ma_sach, s.ten_sach, c.so_luong, c.don_gia FROM chitiet_donhang c JOIN sach s ON c.ma_sach = s.ma_sach WHERE c.ma_don = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("ma_sach");
                String tenSach = rs.getString("ten_sach");
                int soLuong = rs.getInt("so_luong");
                double donGia = rs.getDouble("don_gia");

                OrderItem item = new OrderItem(bookId, tenSach, soLuong, donGia);
                items.add(item);
                total += item.getTotalPrice();
            }

            table.setItems(items);
        } catch (SQLException e) {
            showError("Lỗi tải chi tiết đơn hàng: " + e.getMessage());
            return;
        }

        table.setRowFactory(tv -> {
            TableRow<OrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    OrderItem item = row.getItem();
                    showBookDetailById(item.getBookId());
                }
            });
            return row;
        });

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        Label info = new Label(
                "Khách hàng: " + order.getCustomerName() +
                        "\nSĐT: " + order.getPhone() +
                        "\nEmail: " + order.getEmail() +
                        "\nĐịa chỉ: " + order.getAddress() +
                        "\nLoại đơn: " + order.getOrderType() +
                        "\nNgày tạo: " + order.getCreatedDate()
        );
        Label totalLabel = new Label("Tổng tiền: " + String.format("%.0f", total) + " VNĐ");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        content.getChildren().addAll(info, table, totalLabel);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showBookDetailById(int bookId) {
        String sql = "SELECT * FROM sach WHERE ma_sach = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
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
                BookDialogUtil.showBookDetails(book);
            }
        } catch (SQLException e) {
            showError("Không thể hiển thị chi tiết sách: " + e.getMessage());
        }
    }

    private void setupFilterControls() {
        filterStatusCombo.getItems().addAll("Tất cả", "cho_duyet", "dang_giao", "hoan_thanh", "huy");
        filterStatusCombo.setValue("Tất cả");
        filterStatusCombo.setOnAction(e -> filterOrders());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOrders());

        statusCombo.getItems().addAll("cho_duyet", "dang_giao", "hoan_thanh", "huy");
    }

    private void filterOrders() {
        String keyword = searchField.getText().toLowerCase().trim();
        String selectedStatus = filterStatusCombo.getValue();

        orderTable.setItems(orderList.filtered(order -> {
            boolean matchesSearch = order.getCustomerName().toLowerCase().contains(keyword) ||
                    order.getPhone().contains(keyword);
            boolean matchesStatus = selectedStatus.equals("Tất cả") || order.getStatus().equals(selectedStatus);
            return matchesSearch && matchesStatus;
        }));
    }

    private void loadOrders() {
        orderList.clear();
        String sql = "SELECT ma_don, ten_kh, sdt, email, dia_chi, tong_tien, ngay_tao, loai_don, trang_thai FROM donhang ORDER BY ngay_tao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("ma_don"),
                        rs.getString("ten_kh"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("dia_chi"),
                        rs.getString("loai_don"),
                        rs.getString("trang_thai"),
                        rs.getString("ngay_tao")
                );
                order.setTotal(rs.getDouble("tong_tien"));
                orderList.add(order);
            }

            orderTable.setItems(orderList);

        } catch (SQLException e) {
            showError("Không thể tải đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedOrder == null) {
            showError("Vui lòng chọn đơn hàng.");
            return;
        }

        String newStatus = statusCombo.getValue();
        if (newStatus == null || newStatus.isEmpty()) {
            showError("Vui lòng chọn trạng thái mới.");
            return;
        }

        String sql = "UPDATE donhang SET trang_thai = ? WHERE ma_don = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, selectedOrder.getId());
            stmt.executeUpdate();

            selectedOrder.setStatus(newStatus);
            orderTable.refresh();
            showInfo("Trạng thái đã được cập nhật!");

        } catch (SQLException e) {
            showError("Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
