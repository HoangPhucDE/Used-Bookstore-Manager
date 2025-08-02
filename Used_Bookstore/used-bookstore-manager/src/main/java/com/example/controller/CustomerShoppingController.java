package com.example.controller;

import com.example.controller.dao.BookDao;
import com.example.controller.dao.CustomerDao;
import com.example.model.Book;
import com.example.model.Customer;
import com.example.utils.CurrencyFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class CustomerShoppingController {

    @FXML private VBox productContainer;
    @FXML private ListView<String> cartListView;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField customerNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextArea notesArea;

    private final BookDao bookDao = new BookDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final List<Book> cart = new ArrayList<>();
    private final List<Book> allBooks = new ArrayList<>();

    @FXML
    public void initialize() {
        // Load phương thức thanh toán
        paymentMethodComboBox.getItems().addAll(
                "Thanh toán khi nhận hàng (COD)",
                "Chuyển khoản ngân hàng",
                "Ví điện tử (Momo, ZaloPay)",
                "Thẻ tín dụng/ghi nợ"
        );

        // Danh mục thể loại mẫu
        categoryComboBox.getItems().addAll(
                "Tất cả", "Văn học", "Kinh tế", "Kỹ thuật", "Thiếu nhi", "Giáo trình"
        );
        categoryComboBox.getSelectionModel().select("Tất cả");
        categoryComboBox.setOnAction(e -> filterByCategory());

        // Load sách và hiển thị
        allBooks.addAll(bookDao.getAvailableBooks());
        showBooks(allBooks);

        // Tự động điền thông tin khách hàng
        int currentUserId = LoginController.currentUserId;
        Customer customer = customerDao.findCustomerByAccountId(currentUserId);
        if (customer != null) {
            customerNameField.setText(customer.getHoTen());
            phoneField.setText(customer.getSoDienThoai());
            emailField.setText(customer.getEmail());
            addressArea.setText(customer.getDiaChi());
        }
    }

    private void filterByCategory() {
        String selected = categoryComboBox.getValue();
        List<Book> filtered = selected.equals("Tất cả") ?
                bookDao.getAvailableBooks() : bookDao.getBooksByCategory(selected);
        showBooks(filtered);
    }

    private void showBooks(List<Book> books) {
        productContainer.getChildren().clear();

        for (Book book : books) {
            HBox bookBox = new HBox(10);
            bookBox.setStyle("""
            -fx-background-color: #ffffff;
            -fx-background-radius: 10;
            -fx-padding: 15;
            -fx-border-color: #dee2e6;
            -fx-border-radius: 10;
        """);

            VBox details = new VBox(5);

            // Tên sách rõ ràng, đậm, dễ đọc
            Label nameLabel = new Label(book.getTitle());
            nameLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-text-fill: #000000;
        """);

            // Thể loại sách
            Label categoryLabel = new Label("📚 " + book.getCategory());
            categoryLabel.setStyle("""
            -fx-text-fill: #6c757d;
            -fx-font-size: 12px;
        """);

            // Giá tiền
            Label priceLabel = new Label(CurrencyFormatter.format(book.getPrice()));
            priceLabel.setStyle("""
            -fx-text-fill: #28a745;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
        """);

            details.getChildren().addAll(nameLabel, categoryLabel, priceLabel);

            // Nút thêm vào giỏ
            Button addButton = new Button("🛒 Thêm vào giỏ");
            addButton.setStyle("""
            -fx-background-color: #007bff;
            -fx-text-fill: white;
            -fx-background-radius: 5;
            -fx-padding: 8 12;
            -fx-font-weight: bold;
        """);
            addButton.setOnAction(e -> addToCart(book));

            // Spacer để căn nút sang bên phải
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            bookBox.getChildren().addAll(details, spacer, addButton);
            productContainer.getChildren().add(bookBox);
        }
    }

    private void addToCart(Book book) {
        cart.add(book);
        updateCartDisplay();
        showAlert(Alert.AlertType.INFORMATION, "Đã thêm vào giỏ", "Đã thêm \"" + book.getTitle() + "\" vào giỏ hàng.");
    }

    @FXML
    private void handleCheckout() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ.");
            return;
        }

        if (isEmpty(customerNameField) || isEmpty(phoneField) || isEmpty(addressArea) || paymentMethodComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ thông tin bắt buộc.");
            return;
        }

        StringBuilder bill = new StringBuilder();
        bill.append("=== THÔNG TIN ĐẶT HÀNG ===\n\n");
        bill.append("Khách hàng: ").append(customerNameField.getText()).append("\n");
        bill.append("SĐT: ").append(phoneField.getText()).append("\n");
        if (!emailField.getText().isBlank()) bill.append("Email: ").append(emailField.getText()).append("\n");
        bill.append("Địa chỉ: ").append(addressArea.getText()).append("\n");
        bill.append("Thanh toán: ").append(paymentMethodComboBox.getValue()).append("\n\n");

        bill.append("=== CHI TIẾT ĐƠN HÀNG ===\n");
        double total = 0;
        for (Book book : cart) {
            bill.append("• ").append(book.getTitle())
                    .append(" - ").append(CurrencyFormatter.format(book.getPrice())).append("\n");
            total += book.getPrice();
        }

        bill.append("\nTổng tiền: ").append(CurrencyFormatter.format(total));
        if (!notesArea.getText().isBlank()) {
            bill.append("\n\nGhi chú: ").append(notesArea.getText());
        }

        showAlert(Alert.AlertType.INFORMATION, "Đặt hàng thành công!", bill.toString());
        clearForm();
    }

    private void updateCartDisplay() {
        cartListView.getItems().clear();

        if (cart.isEmpty()) {
            cartListView.getItems().add("Giỏ hàng trống");
            totalLabel.setText("Tổng tiền: 0 VNĐ");
            return;
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Double> prices = new HashMap<>();
        double total = 0;

        for (Book book : cart) {
            String name = book.getTitle();
            counts.put(name, counts.getOrDefault(name, 0) + 1);
            prices.put(name, book.getPrice());
            total += book.getPrice();
        }

        for (String name : counts.keySet()) {
            int qty = counts.get(name);
            double price = prices.get(name);
            cartListView.getItems().add(
                    String.format("%s (x%d) - %s", name, qty, CurrencyFormatter.format(price * qty))
            );
        }

        totalLabel.setText("Tổng tiền: " + CurrencyFormatter.format(total));
    }

    private void clearForm() {
        cart.clear();
        updateCartDisplay();
        notesArea.clear();
    }

    private boolean isEmpty(TextInputControl field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}
