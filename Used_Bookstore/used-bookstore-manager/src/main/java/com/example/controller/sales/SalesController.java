package com.example.controller.sales;

import com.example.controller.auth.LoginController;
import com.example.controller.dao.*;
import com.example.model.Book;
import com.example.model.Customer;
import com.example.model.OrderItem;
import com.example.utils.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

public class SalesController {

    @FXML private ComboBox<String> bookCombo;
    @FXML private TextField quantityField;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colBookTitle;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, String> colUnitPrice;
    @FXML private TableColumn<OrderItem, String> colTotalPrice;

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
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colUnitPrice.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getFormattedUnitPrice()));
        colTotalPrice.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getFormattedTotalPrice()));

        orderTable.setItems(cartItems);
        orderTable.setEditable(true);
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colQuantity.setOnEditCommit(event -> {
            OrderItem item = event.getRowValue();
            int newQuantity = event.getNewValue();
            if (newQuantity <= 0) {
                cartItems.remove(item);
            } else {
                item.setQuantity(newQuantity);
            }
            updateTotal();
        });

        createAccountCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            accountBox.setVisible(newVal);
            accountBox.setManaged(newVal);
            usernameField.setVisible(newVal);
            usernameField.setManaged(newVal);
            passwordField.setVisible(newVal);
            passwordField.setManaged(newVal);
        });

        orderTable.setRowFactory(tv -> {
            TableRow<OrderItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Book book = bookDao.findBookById(row.getItem().getBookId());
                    if (book != null) BookDialogUtil.showBookDetails(book);
                }
            });
            return row;
        });

        phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) autoFillCustomerInfo();
        });

        loadBooksFromDatabase();
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
            if (quantity <= 0) throw new NumberFormatException();
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

        Optional<OrderItem> existing = cartItems.stream()
                .filter(item -> item.getBookId() == selectedBook.getId())
                .findFirst();

        if (existing.isPresent()) {
            OrderItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > selectedBook.getStock()) {
                showError("Vượt quá số lượng tồn kho: " + selectedBook.getStock());
                return;
            }
            item.setQuantity(newQuantity);
            orderTable.refresh();
        } else {
            if (quantity > selectedBook.getStock()) {
                showError("Vượt quá số lượng tồn kho: " + selectedBook.getStock());
                return;
            }
            cartItems.add(new OrderItem(selectedBook.getId(), bookTitle, quantity, selectedBook.getPrice()));
        }

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

        double totalAmount = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        if (totalAmount <= 0) {
            showError("Tổng tiền đơn hàng phải lớn hơn 0.");
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            showError("Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0.");
            return;
        }

        if (name.isEmpty() || phone.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin khách hàng.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        boolean createAccount = createAccountCheckbox.isSelected();

        if (createAccount) {
            if (username.isEmpty() || password.isEmpty()) {
                showError("Vui lòng nhập username và mật khẩu để tạo tài khoản khách hàng.");
                return;
            }
            if (accountDao.findAccountIdByUsername(username) != null) {
                showError("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.");
                return;
            }
            if (accountDao.isEmailExists(email)) {
                showError("Email đã tồn tại. Vui lòng dùng email khác.");
                return;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int createdById = LoginController.currentUserId;
            int accId = -1;
            if (createAccount) {
                accId = accountService.createCustomerAccountIfNotExists(
                        conn, username, password, email, name, phone, address);
                if (accId == -1) {
                    conn.rollback();
                    showError("Không thể tạo tài khoản khách hàng.");
                    return;
                }
            }

            OrderDao orderDao = new OrderDao();
            int orderId = orderDao.insertOrder(conn, name, phone, email, address, createdById, "offline");

            OrderItemDao orderItemDao = new OrderItemDao();
            orderItemDao.insertOrderItems(conn, orderId, cartItems);

            bookDao.updateStockAfterOrder(conn, cartItems);

            conn.commit();

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
                                "offline",
                                new ArrayList<>(cartItems),
                                totalAmount
                        );
                        showSuccess("Hóa đơn đã được lưu thành công.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Lỗi khi xuất hóa đơn: " + e.getMessage());
                    }
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

    private void autoFillCustomerInfo() {
        String phone = phoneField.getText().trim();
        if (!ValidationUtils.isValidPhone(phone)) return;
        Customer customer = customerDao.findCustomerByPhoneWithoutStatus(phone);
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