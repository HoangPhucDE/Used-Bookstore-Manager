package com.example.controller.order;

import com.example.controller.dao.BookDao;
import com.example.controller.dao.OrderDao;
import com.example.controller.dao.OrderItemDao;
import com.example.model.Book;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.utils.BookDialogUtil;
import com.example.utils.OrderItemTableBuilder;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class OrderStatusController {

    private final OrderDao orderDao = new OrderDao();
    private final OrderItemDao orderItemDao = new OrderItemDao();
    private final BookDao bookDao = new BookDao();

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, String> colPhone;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colCreatedDate;
    @FXML private TableColumn<Order, String> colOrderType;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private TextField searchField;
    @FXML private Button updateStatusBtn;

    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private Order selectedOrder;

    private final Map<String, String> statusDisplayMap = new LinkedHashMap<>() {{
        put("Chờ duyệt", "cho_duyet");
        put("Đang giao", "dang_giao");
        put("Hoàn thành", "hoan_thanh");
        put("Hủy", "huy");
    }};

    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterControls();
        loadOrders();
        setupRowClickHandler();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colCustomerName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerName()));
        colPhone.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPhone()));
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotal()));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? "" : currencyFormat.format(amount) + " VNĐ");
            }
        });

        colCreatedDate.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCreatedDate()));
        colOrderType.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getOrderType()));
        colStatus.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getDisplayStatus(data.getValue().getStatus())));

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedOrder = newVal;
            if (newVal != null) {
                statusCombo.setValue(getDisplayStatus(newVal.getStatus()));
            }
        });
    }

    private void setupFilterControls() {
        filterStatusCombo.getItems().add("Tất cả");
        filterStatusCombo.getItems().addAll(statusDisplayMap.keySet());
        filterStatusCombo.setValue("Tất cả");
        filterStatusCombo.setOnAction(e -> filterOrders());

        statusCombo.getItems().addAll(statusDisplayMap.keySet());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOrders());
    }

    private void setupRowClickHandler() {
        orderTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    showOrderDetails(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadOrders() {
        orderList.clear();
        orderList.addAll(orderDao.getAllOrders());
        orderTable.setItems(orderList);
    }

    private void filterOrders() {
        String keyword = searchField.getText().toLowerCase().trim();
        String displayStatus = filterStatusCombo.getValue();
        String statusCode = statusDisplayMap.getOrDefault(displayStatus, "");

        orderTable.setItems(orderList.filtered(order -> {
            boolean matchText = order.getCustomerName().toLowerCase().contains(keyword)
                    || order.getPhone().contains(keyword);
            boolean matchStatus = displayStatus.equals("Tất cả") || order.getStatus().equals(statusCode);
            return matchText && matchStatus;
        }));
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedOrder == null) {
            showError("Vui lòng chọn đơn hàng.");
            return;
        }

        if (!checkSDT(selectedOrder.getPhone())) return;

        String newDisplay = statusCombo.getValue();
        String newStatus = statusDisplayMap.getOrDefault(newDisplay, "");

        if (newStatus.isEmpty()) {
            showError("Vui lòng chọn trạng thái hợp lệ.");
            return;
        }

        if (orderDao.updateOrderStatus(selectedOrder.getId(), newStatus)) {
            selectedOrder.setStatus(newStatus);
            orderTable.refresh();
            showInfo("Cập nhật trạng thái thành công.");
        } else {
            showError("Cập nhật thất bại.");
        }
    }

    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng #" + order.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ObservableList<OrderItem> items = FXCollections.observableArrayList(orderItemDao.getOrderItemsByOrderId(order.getId()));
        double total = items.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        TableView<OrderItem> table = OrderItemTableBuilder.createOrderItemTable(items);
        table.setRowFactory(tv -> {
            TableRow<OrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Book book = bookDao.findBookById(row.getItem().getBookId());
                    if (book != null) {
                        BookDialogUtil.showBookDetails(book);
                    }
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
                        "\nNgày tạo: " + order.getCreatedDate() +
                        "\nTrạng thái: " + getDisplayStatus(order.getStatus())
        );
        Label totalLabel = new Label("Tổng tiền: " + currencyFormat.format(total) + " VNĐ");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        content.getChildren().addAll(info, table, totalLabel);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private String getDisplayStatus(String dbStatus) {
        return statusDisplayMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(dbStatus))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Không rõ");
    }

    private boolean checkSDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            showAlert("Số điện thoại không được để trống.");
            return false;
        }
        if (!sdt.matches("\\d{10}")) {
            showAlert("Số điện thoại phải gồm đúng 10 chữ số.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi nhập liệu");
        alert.setHeaderText("Thông tin không hợp lệ");
        alert.setContentText(message);
        alert.showAndWait();
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