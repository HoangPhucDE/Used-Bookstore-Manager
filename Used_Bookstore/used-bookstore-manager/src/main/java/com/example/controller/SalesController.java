package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Book;
import com.example.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesController {

    @FXML private ComboBox<String> bookCombo;
    @FXML private ComboBox<String> orderTypeCombo;
    @FXML private TextField quantityField;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colBookTitle;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colUnitPrice;
    @FXML private TableColumn<OrderItem, Double> colTotalPrice;
    @FXML private Button deleteItemButton;

    @FXML private TextField nameField, phoneField, emailField, addressField;
    @FXML private Label totalLabel;

    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private final List<Book> allBooks = new ArrayList<>();

    @FXML
    public void initialize() {
        loadBooksFromDatabase();

        orderTypeCombo.getItems().addAll("online", "offline", "trahang", "nhap_kho");
        orderTypeCombo.getSelectionModel().selectFirst();

        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        orderTable.setItems(cartItems);

        phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                autoFillCustomerInfo();
            }
        });
    }

    private Integer createCustomerAccountIfNotExists(Connection conn, String phone, String email, String name, String address) throws SQLException {
        // 1. Kiểm tra tài khoản theo số điện thoại (username)
        String checkUserQuery = "SELECT id FROM taikhoan WHERE username = ?";
        try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery)) {
            checkUserStmt.setString(1, phone);
            ResultSet userRs = checkUserStmt.executeQuery();
            if (userRs.next()) {
                int existingId = userRs.getInt("id");

                // 1.1 Kiểm tra khách hàng đã tồn tại theo id_taikhoan
                String checkKHQuery = "SELECT ma_kh FROM khachhang WHERE id_taikhoan = ?";
                try (PreparedStatement checkKHStmt = conn.prepareStatement(checkKHQuery)) {
                    checkKHStmt.setInt(1, existingId);
                    ResultSet khRs = checkKHStmt.executeQuery();
                    if (khRs.next()) {
                        return existingId; // ✅ đã có tài khoản và khách hàng
                    }
                }

                // 1.2 Nếu chưa có khách hàng, thì tạo khách hàng
                String insertKHQuery = """
                INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan)
                VALUES (?, ?, ?, ?, ?)
            """;
                try (PreparedStatement insertKHStmt = conn.prepareStatement(insertKHQuery)) {
                    insertKHStmt.setString(1, name);
                    insertKHStmt.setString(2, email);
                    insertKHStmt.setString(3, phone);
                    insertKHStmt.setString(4, address);
                    insertKHStmt.setInt(5, existingId);
                    insertKHStmt.executeUpdate();
                }

                return existingId;
            }
        }

        // 2. Kiểm tra email đã tồn tại chưa (email UNIQUE)
        String checkEmailQuery = "SELECT id FROM taikhoan WHERE email = ?";
        try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery)) {
            checkEmailStmt.setString(1, email);
            ResultSet emailRs = checkEmailStmt.executeQuery();
            if (emailRs.next()) {
                showAlert("Lỗi", "Email đã tồn tại. Vui lòng dùng email khác.");
                return null;
            }
        }

        // 3. Hỏi người dùng có muốn tạo tài khoản mới không
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Tạo tài khoản mới");
        confirm.setHeaderText(null);
        confirm.setContentText("Khách hàng này chưa có tài khoản. Tạo tài khoản mới?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return null; // người dùng từ chối
        }

        // 4. Tạo tài khoản mới
        int accountId;
        String insertAccountQuery = """
        INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai, ngay_dang_ky)
        VALUES (?, ?, 'khach', 'khachhang', ?, TRUE, NOW())
    """;
        try (PreparedStatement insertAccountStmt = conn.prepareStatement(insertAccountQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertAccountStmt.setString(1, phone); // username
            insertAccountStmt.setString(2, phone); // password
            insertAccountStmt.setString(3, email);
            insertAccountStmt.executeUpdate();

            ResultSet genKeys = insertAccountStmt.getGeneratedKeys();
            if (genKeys.next()) {
                accountId = genKeys.getInt(1);
            } else {
                throw new SQLException("Không thể lấy ID tài khoản vừa tạo.");
            }
        }

        // 5. Tạo khách hàng mới gắn với tài khoản
        String insertCustomerQuery = """
        INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan)
        VALUES (?, ?, ?, ?, ?)
    """;
        try (PreparedStatement insertCustomerStmt = conn.prepareStatement(insertCustomerQuery)) {
            insertCustomerStmt.setString(1, name);
            insertCustomerStmt.setString(2, email);
            insertCustomerStmt.setString(3, phone);
            insertCustomerStmt.setString(4, address);
            insertCustomerStmt.setInt(5, accountId);
            insertCustomerStmt.executeUpdate();
        }

        return accountId;
    }


    private void resetForm() {
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        quantityField.clear();
        bookCombo.getSelectionModel().clearSelection();
        orderTypeCombo.getSelectionModel().selectFirst();
    }

    private void loadBooksFromDatabase() {
        String query = """
    SELECT ma_sach, ten_sach, tac_gia, the_loai, nxb, nam_xb,
           gia_nhap, gia_ban, tinh_trang, so_luong_ton, danh_gia, hinh_anh
    FROM sach
    WHERE so_luong_ton > 0
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            allBooks.clear();
            bookCombo.getItems().clear();

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("ma_sach"),
                        rs.getString("ten_sach"),
                        rs.getString("tac_gia"),
                        rs.getString("the_loai"),
                        rs.getString("nxb"),                // publisher
                        rs.getInt("nam_xb"),                // year
                        rs.getDouble("gia_nhap"),           // importPrice
                        rs.getDouble("gia_ban"),            // salePrice
                        rs.getString("tinh_trang"),         // condition
                        rs.getInt("so_luong_ton"),          // stock
                        rs.getDouble("danh_gia"),           // rating
                        rs.getString("hinh_anh")            // imagePath
                );
                allBooks.add(book);
                bookCombo.getItems().add(book.getTitle());
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải sách từ CSDL: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddItem() {
        String bookTitle = bookCombo.getValue();
        String qtyText = quantityField.getText();

        if (bookTitle == null || qtyText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng chọn sách và nhập số lượng.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ.");
            return;
        }

        Book selectedBook = allBooks.stream()
                .filter(book -> book.getTitle().equals(bookTitle))
                .findFirst()
                .orElse(null);

        if (selectedBook == null) {
            showAlert("Lỗi", "Không tìm thấy sách.");
            return;
        }

        if (quantity > selectedBook.getStock()) {
            showAlert("Lỗi", "Số lượng đặt vượt quá số lượng tồn kho. Hiện có: " + selectedBook.getStock());
            return;
        }

        cartItems.add(new OrderItem(selectedBook.getId(), bookTitle, quantity, selectedBook.getPrice()));
        updateTotal();
        quantityField.clear();
    }

    @FXML
    private void handleDeleteItem() {
        OrderItem selectedItem = orderTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn có chắc chắn muốn xóa sách này khỏi giỏ hàng?");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    cartItems.remove(selectedItem);
                    updateTotal();
                }
            });
        } else {
            showAlert("Thông báo", "Vui lòng chọn sách để xóa.");
        }
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        totalLabel.setText(String.format("%.0f VNĐ", total));
    }

    private void showRecentOrder(int orderId) {
        String query = "SELECT ma_don, ten_kh, tong_tien, ngay_tao, loai_don FROM donhang WHERE ma_don = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StringBuilder builder = new StringBuilder();
                builder.append("🧾 Đơn hàng mới:\n\n");
                builder.append("Mã đơn: ").append(rs.getInt("ma_don")).append("\n");
                builder.append("Khách hàng: ").append(rs.getString("ten_kh")).append("\n");
                builder.append("Tổng tiền: ").append(String.format("%.0f VNĐ", rs.getDouble("tong_tien"))).append("\n");
                builder.append("Ngày tạo: ").append(rs.getString("ngay_tao")).append("\n");
                builder.append("Loại đơn: ").append(rs.getString("loai_don"));

                showAlert("✅ Đơn hàng gần đây", builder.toString());
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể hiển thị đơn hàng gần đây.");
        }
    }

    @FXML
    public void handleSubmitOrder() {
        // Kiểm tra dữ liệu đầu vào
        if (cartItems.isEmpty() || nameField.getText().trim().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập thông tin và thêm sách vào đơn.");
            return;
        }

        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // ✅ Tạo tài khoản và khách hàng nếu chưa có
            Integer userId = createCustomerAccountIfNotExists(conn, phone, email, name, address);
            if (userId == null) {
                conn.rollback();
                return;
            }

            // ✅ Tạo đơn hàng
            String insertOrder = """
            INSERT INTO donhang (ten_kh, sdt, email, dia_chi, tong_tien, nguoi_tao_id, ngay_tao, loai_don)
            VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)
        """;
            String orderType = orderTypeCombo.getValue();

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                double total = cartItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

                orderStmt.setString(1, name);
                orderStmt.setString(2, phone);
                orderStmt.setString(3, email);
                orderStmt.setString(4, address);
                orderStmt.setDouble(5, total);
                orderStmt.setInt(6, userId); // ✅ dùng ID vừa tạo
                orderStmt.setString(7, orderType);

                orderStmt.executeUpdate();

                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    conn.rollback();
                    showAlert("Lỗi", "Không thể tạo đơn hàng.");
                    return;
                }

                int orderId = generatedKeys.getInt(1);

                // ✅ Ghi chi tiết đơn hàng + cập nhật tồn kho
                String insertItem = "INSERT INTO chitiet_donhang (ma_don, ma_sach, so_luong, don_gia) VALUES (?, ?, ?, ?)";
                String updateStock = "UPDATE sach SET so_luong_ton = so_luong_ton - ? WHERE ma_sach = ?";

                try (PreparedStatement itemStmt = conn.prepareStatement(insertItem);
                     PreparedStatement stockStmt = conn.prepareStatement(updateStock)) {

                    for (OrderItem item : cartItems) {
                        Book book = allBooks.stream()
                                .filter(b -> b.getTitle().equals(item.getBookTitle()))
                                .findFirst()
                                .orElseThrow();

                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, book.getId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.addBatch();

                        stockStmt.setInt(1, item.getQuantity());
                        stockStmt.setInt(2, book.getId());
                        stockStmt.addBatch();
                    }

                    itemStmt.executeBatch();
                    stockStmt.executeBatch();
                }

                // ✅ Commit và hiển thị kết quả
                conn.commit();
                showRecentOrder(orderId);
                showAlert("Thành công", "Đơn hàng đã được lưu. Tổng tiền: " + total + " VNĐ");
                cartItems.clear();
                updateTotal();
                resetForm();
                bookCombo.getItems().clear();
                allBooks.clear();
                loadBooksFromDatabase();

            } catch (SQLException e) {
                conn.rollback();
                showAlert("Lỗi", "Không thể lưu đơn hàng: " + e.getMessage());
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể kết nối cơ sở dữ liệu: " + e.getMessage());
        }
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    private void autoFillCustomerInfo() {
        String sdt = phoneField.getText().trim();
        if (sdt.isEmpty()) return;

        String query = "SELECT ten_kh, email, dia_chi FROM donhang WHERE sdt = ? ORDER BY ngay_tao DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, sdt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("ten_kh"));
                emailField.setText(rs.getString("email"));
                addressField.setText(rs.getString("dia_chi"));
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tự động điền thông tin: " + e.getMessage());
        }
    }
}
