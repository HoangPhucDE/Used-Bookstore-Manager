package com.example.controller;

import com.example.controller.dao.AccountDao;
import com.example.controller.dao.BookDao;
import com.example.controller.dao.CustomerDao;
import com.example.controller.dao.OrderDao;
import com.example.controller.dao.OrderItemDao;
import com.example.model.Book;
import com.example.model.Customer;
import com.example.model.OrderItem;
import com.example.controller.dao.CustomerAccountService;
import com.example.utils.BookDialogUtil;
import com.example.utils.PdfExportUtils;


import com.example.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

public class SalesController {

    @FXML private ComboBox<String> bookCombo;
    @FXML private ComboBox<String> orderTypeCombo;
    @FXML private TextField quantityField;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colBookTitle;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colTotalPrice;

    @FXML private TextField nameField, phoneField, emailField, addressField;
    @FXML private TextField usernameField, passwordField;
    @FXML private Label totalLabel;
    @FXML private CheckBox printInvoiceCheckbox;
    @FXML private CheckBox createAccountCheckbox;
    @FXML private VBox accountBox;

    private final BookDao bookDao = new BookDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final AccountDao accountDao = new AccountDao();
    private final CustomerAccountService accountService = new CustomerAccountService();

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private final List<Book> allBooks = new ArrayList<>();

    @FXML
    public void initialize() {
        orderTypeCombo.getItems().addAll("online", "offline", "trahang", "nhap_kho");
        orderTypeCombo.getSelectionModel().selectFirst();

        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        orderTable.setItems(cartItems);

        loadBooksFromDatabase();
        createAccountCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            accountBox.setVisible(newVal);
            accountBox.setManaged(newVal);
        });
        orderTable.setRowFactory(tv -> {
            TableRow<OrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Book book = bookDao.findBookById(row.getItem().getBookId());
                    if (book != null) {
                        BookDialogUtil.showBookDetails(book);
                    }
                }
            });
            return row;
        });

        phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) autoFillCustomerInfo();
        });

        createAccountCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            usernameField.setVisible(newVal);
            usernameField.setManaged(newVal);
            passwordField.setVisible(newVal);
            passwordField.setManaged(newVal);
        });

    }

    private void loadBooksFromDatabase() {
        allBooks.clear();
        allBooks.addAll(bookDao.getAvailableBooks());

        bookCombo.getItems().clear();
        for (Book b : allBooks) {
            bookCombo.getItems().add(b.getTitle());
        }
    }

    @FXML
    public void handleAddItem() {
        String bookTitle = bookCombo.getValue();
        if (bookTitle == null || quantityField.getText().isEmpty()) {
            showError("Vui lòng chọn sách và nhập số lượng.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showError("Số lượng không hợp lệ.");
            return;
        }

        Book selectedBook = allBooks.stream()
                .filter(b -> b.getTitle().equals(bookTitle))
                .findFirst().orElse(null);

        if (selectedBook == null) {
            showError("Không tìm thấy sách.");
            return;
        }

        if (quantity > selectedBook.getStock()) {
            showError("Vượt quá số lượng tồn kho: " + selectedBook.getStock());
            return;
        }

        cartItems.add(new OrderItem(selectedBook.getId(), bookTitle, quantity, selectedBook.getPrice()));
        quantityField.clear();
        updateTotal();
    }

    @FXML
    public void handleDeleteItem() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateTotal();
        } else {
            showError("Vui lòng chọn sách để xóa.");
        }
    }

    @FXML
    public void handleSubmitOrder() {
        if (cartItems.isEmpty()) {
            showError("Chưa có sách trong giỏ hàng.");
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String orderType = orderTypeCombo.getValue();

        // Người bán (đang đăng nhập)
        int createdById = LoginController.currentUserId;

        if (name.isEmpty() || phone.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin khách hàng.");
            return;
        }

        // Nếu tích checkbox thì yêu cầu username + password
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        boolean createAccount = createAccountCheckbox.isSelected();

        if (createAccount && (username.isEmpty() || password.isEmpty())) {
            showError("Vui lòng nhập username và mật khẩu để tạo tài khoản khách hàng.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Tạo tài khoản khách (nếu được chọn)
            if (createAccount) {
                int accId = accountService.createCustomerAccountIfNotExists(
                        conn, username, password, email, name, phone, address
                );
                if (accId == -1) {
                    conn.rollback();
                    showError("Không thể tạo tài khoản khách hàng.");
                    return;
                }
            }

            // 2. Tạo đơn hàng (người tạo là nhân viên đang đăng nhập)
            OrderDao orderDao = new OrderDao();
            int orderId = orderDao.insertOrder(conn, name, phone, email, address, createdById, orderType);

            // 3. Ghi chi tiết đơn hàng
            OrderItemDao orderItemDao = new OrderItemDao();
            orderItemDao.insertOrderItems(conn, orderId, cartItems);

            // 4. Trừ tồn kho
            bookDao.updateStockAfterOrder(conn, cartItems);

            // 5. Commit
            conn.commit();

            // 6. Tùy chọn xuất hóa đơn
            if (printInvoiceCheckbox.isSelected()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Lưu hóa đơn");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                fileChooser.setInitialFileName("HoaDon_MaDon_" + orderId + ".pdf");

                File selectedFile = fileChooser.showSaveDialog(orderTable.getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        PdfExportUtils.exportInvoice(
                                selectedFile,
                                orderId,
                                name,
                                phone,
                                email,
                                address,
                                orderType,
                                new ArrayList<>(cartItems),
                                cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum()
                        );
                        showSuccess("Hóa đơn đã được lưu thành công.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Lỗi khi xuất hóa đơn: " + e.getMessage());
                    }


                    showSuccess("Hóa đơn đã được lưu thành công.");
                }
            }

            showSuccess("Đơn hàng đã lưu thành công.");
            resetForm();
            loadBooksFromDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Lỗi khi xử lý đơn hàng: " + e.getMessage());
        }
    }

    private void saveOrderDetails(Connection conn, int orderId) throws SQLException {
        String sql = "INSERT INTO chitiet_donhang (ma_don, ma_sach, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : cartItems) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getBookId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getUnitPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void updateInventory(Connection conn) throws SQLException {
        String sql = "UPDATE sach SET so_luong_ton = so_luong_ton - ? WHERE ma_sach = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : cartItems) {
                stmt.setInt(1, item.getQuantity());
                stmt.setInt(2, item.getBookId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void autoFillCustomerInfo() {
        String phone = phoneField.getText().trim();
        if (!phone.matches("\\d{10}")) return;

        Customer customer = customerDao.findCustomerByPhone(phone);
        if (customer != null) {
            nameField.setText(customer.getHoTen());
            emailField.setText(customer.getEmail());
            addressField.setText(customer.getDiaChi());
        }
    }

    private void updateTotal() {
        double total = cartItems.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        totalLabel.setText(formatCurrency(total));
    }

    private String formatCurrency(double value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
    }

    private void resetForm() {
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        usernameField.clear();
        passwordField.clear();
        quantityField.clear();
        cartItems.clear();
        totalLabel.setText("0 VNĐ");
        bookCombo.getSelectionModel().clearSelection();
        orderTypeCombo.getSelectionModel().selectFirst();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
