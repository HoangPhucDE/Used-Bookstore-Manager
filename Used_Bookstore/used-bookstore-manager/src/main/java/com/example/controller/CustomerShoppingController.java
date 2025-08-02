package com.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CustomerShoppingController {

    @FXML
    private VBox productContainer;

    @FXML
    private ListView<String> cartListView;

    @FXML
    private Label totalLabel;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField customerNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextArea addressArea;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private TextArea notesArea;

    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> cart = new ArrayList<>();

    // Mock Product class
    static class Product {
        String name;
        int price;
        String category;

        Product(String name, int price, String category) {
            this.name = name;
            this.price = price;
            this.category = category;
        }

        @Override
        public String toString() {
            return name + " - " + String.format("%,d", price) + " đ";
        }
    }

    @FXML
    public void initialize() {
        // Khởi tạo phương thức thanh toán
        paymentMethodComboBox.getItems().addAll(
            "Thanh toán khi nhận hàng (COD)",
            "Chuyển khoản ngân hàng",
            "Ví điện tử (Momo, ZaloPay)",
            "Thẻ tín dụng/ghi nợ"
        );

        // Mock danh mục sách
        categoryComboBox.getItems().addAll("Tất cả", "Văn học", "Kinh tế", "Kỹ thuật", "Thiếu nhi", "Giáo trình");
        categoryComboBox.getSelectionModel().select("Tất cả");
        categoryComboBox.setOnAction(e -> filterByCategory());

        // Mock sản phẩm - Sách cũ
        allProducts.add(new Product("Dế Mèn Phiêu Lưu Ký", 30000, "Thiếu nhi"));
        allProducts.add(new Product("Giáo trình Cấu trúc dữ liệu và giải thuật", 45000, "Giáo trình"));
        allProducts.add(new Product("Tư duy nhanh và chậm", 60000, "Kinh tế"));
        allProducts.add(new Product("Lập trình Java cơ bản", 50000, "Kỹ thuật"));
        allProducts.add(new Product("Chuyện cũ Hà Nội", 40000, "Văn học"));
        allProducts.add(new Product("Đắc nhân tâm", 35000, "Kinh tế"));
        allProducts.add(new Product("Toán cao cấp - Bài tập", 30000, "Giáo trình"));
        allProducts.add(new Product("Harry Potter và Hòn đá phù thủy", 55000, "Thiếu nhi"));

        showProducts(allProducts);
    }


    private void filterByCategory() {
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory.equals("Tất cả")) {
            showProducts(allProducts);
        } else {
            List<Product> filtered = new ArrayList<>();
            for (Product p : allProducts) {
                if (p.category.equals(selectedCategory)) {
                    filtered.add(p);
                }
            }
            showProducts(filtered);
        }
    }

    private void showProducts(List<Product> products) {
        productContainer.getChildren().clear();

        for (Product product : products) {
            HBox productBox = new HBox(10);
            productBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

            VBox details = new VBox(5);

            Label nameLabel = new Label(product.name);
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212529;");

            Label priceLabel = new Label(String.format("%,d", product.price) + " đ");
            priceLabel.setStyle("-fx-text-fill: #28a745; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label categoryLabel = new Label("📚 " + product.category);
            categoryLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

            details.getChildren().addAll(nameLabel, categoryLabel, priceLabel);

            Button addButton = new Button("🛒 Thêm vào giỏ");
            addButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 12 8 12; -fx-font-weight: bold;");
            addButton.setOnAction(e -> addToCart(product));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            productBox.getChildren().addAll(details, spacer, addButton);
            productContainer.getChildren().add(productBox);
        }
    }

    private void addToCart(Product product) {
        cart.add(product);
        System.out.println("Added product: " + product.name + " to cart. Cart size: " + cart.size());
        updateCartDisplay();
        
        // Hiển thị thông báo đã thêm sản phẩm
        showAddToCartNotification(product.name);
    }

    private void showAddToCartNotification(String productName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thêm vào giỏ hàng");
        alert.setHeaderText(null);
        alert.setContentText("Đã thêm \"" + productName + "\" vào giỏ hàng!");
        alert.showAndWait();
    }

    @FXML
    private void handleCheckout() {
        // Kiểm tra giỏ hàng có sản phẩm không
        if (cart.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Giỏ hàng trống");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng thêm sản phẩm vào giỏ hàng trước khi đặt hàng!");
            alert.showAndWait();
            return;
        }

        // Kiểm tra thông tin bắt buộc
        if (customerNameField.getText().trim().isEmpty() || 
            phoneField.getText().trim().isEmpty() || 
            addressArea.getText().trim().isEmpty() ||
            paymentMethodComboBox.getValue() == null) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông tin chưa đầy đủ");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng điền đầy đủ thông tin bắt buộc (Họ tên, SĐT, Địa chỉ, Phương thức thanh toán)!");
            alert.showAndWait();
            return;
        }

        // Tạo nội dung đơn hàng
        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("=== THÔNG TIN ĐẶT HÀNG ===\n\n");
        orderDetails.append("Khách hàng: ").append(customerNameField.getText()).append("\n");
        orderDetails.append("Điện thoại: ").append(phoneField.getText()).append("\n");
        if (!emailField.getText().trim().isEmpty()) {
            orderDetails.append("Email: ").append(emailField.getText()).append("\n");
        }
        orderDetails.append("Địa chỉ: ").append(addressArea.getText()).append("\n");
        orderDetails.append("Thanh toán: ").append(paymentMethodComboBox.getValue()).append("\n\n");
        
        orderDetails.append("=== CHI TIẾT ĐỚN HÀNG ===\n");
        int total = 0;
        for (Product product : cart) {
            orderDetails.append("• ").append(product.toString()).append("\n");
            total += product.price;
        }
        orderDetails.append("\nTổng tiền: ").append(String.format("%,d", total)).append(" đ");
        
        if (!notesArea.getText().trim().isEmpty()) {
            orderDetails.append("\n\nGhi chú: ").append(notesArea.getText());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đặt hàng thành công!");
        alert.setHeaderText("Cảm ơn bạn đã mua sách!");
        alert.setContentText(orderDetails.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();

        // Clear form sau khi đặt hàng thành công
        clearForm();
    }

    private void clearForm() {
        cart.clear();
        updateCartDisplay();
        customerNameField.clear();
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
        notesArea.clear();
    }

    private void updateCartDisplay() {
        // Clear và cập nhật ListView trực tiếp
        cartListView.getItems().clear();
        
        int total = 0;
        
        if (cart.isEmpty()) {
            cartListView.getItems().add("Giỏ hàng trống");
            totalLabel.setText("Tổng tiền: 0 đ");
            return;
        }
        
        // Đếm số lượng từng sản phẩm
        java.util.Map<String, Integer> productCount = new java.util.HashMap<>();
        java.util.Map<String, Integer> productPrice = new java.util.HashMap<>();
        
        for (Product p : cart) {
            productCount.put(p.name, productCount.getOrDefault(p.name, 0) + 1);
            productPrice.put(p.name, p.price);
            total += p.price;
        }
        
        // Thêm vào ListView với format đẹp
        for (java.util.Map.Entry<String, Integer> entry : productCount.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();
            int price = productPrice.get(productName);
            
            String displayText = String.format("%s (x%d) - %,d đ", 
                productName, quantity, price * quantity);
            cartListView.getItems().add(displayText);
        }
        
        // Cập nhật tổng tiền
        totalLabel.setText("Tổng tiền: " + String.format("%,d", total) + " đ");
        
        // Debug: In ra console để kiểm tra
        System.out.println("Cart size: " + cart.size());
        System.out.println("ListView items: " + cartListView.getItems().size());
    }
}