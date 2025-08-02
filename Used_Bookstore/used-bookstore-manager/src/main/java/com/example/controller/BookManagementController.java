package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class BookManagementController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, Integer> colId;
    @FXML
    private TableColumn<Book, String> colTitle;
    @FXML
    private TableColumn<Book, String> colAuthor;
    @FXML
    private TableColumn<Book, String> colCategory;
    @FXML
    private TableColumn<Book, Double> colImportPrice; // ✅ Giá nhập
    @FXML
    private TableColumn<Book, Double> colPrice;
    @FXML
    private TableColumn<Book, Integer> colStock;
    @FXML
    private TableColumn<Book, Double> colRating;
    @FXML
    private TableColumn<Book, Void> colActions;
    @FXML
    private Button addBookBtn;

    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadBooksFromDatabase();
        addActionButtons();
        handleDoubleClickRow();

        if (!"admin".equalsIgnoreCase(LoginController.currentUserRole)) {
            addBookBtn.setVisible(false);
            addBookBtn.setManaged(false);
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colImportPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice")); // ✅
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
    }

    private void loadBooksFromDatabase() {
        bookList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM sach")) {

            while (rs.next()) {
                bookList.add(new Book(
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
                        rs.getString("hinh_anh")));
            }
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải dữ liệu sách: " + e.getMessage());
        }
        bookTable.setItems(bookList);
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                viewBtn.setOnAction(e -> showBookDetails(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> showBookDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (confirmDelete(book.getTitle())) {
                        deleteBookFromDB(book.getId());
                        bookTable.getItems().remove(book);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void handleDoubleClickRow() {
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    showBookDetails(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void searchBook() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            bookTable.setItems(bookList);
            return;
        }

        ObservableList<Book> filtered = FXCollections.observableArrayList();
        for (Book b : bookList) {
            if (b.getTitle().toLowerCase().contains(keyword) || b.getAuthor().toLowerCase().contains(keyword)) {
                filtered.add(b);
            }
        }
        bookTable.setItems(filtered);
    }

    @FXML
    private void showAddBookDialog() {
        showBookDialog(null);
    }

    private void showBookDialog(Book bookToEdit) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(bookToEdit == null ? "Thêm sách" : "Sửa sách");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField();
        TextField author = new TextField();
        TextField category = new TextField();
        TextField importPrice = new TextField(); // ✅
        TextField price = new TextField();
        TextField stock = new TextField();
        TextField rating = new TextField();
        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(100);
        imgPreview.setFitHeight(120);

        Button chooseImg = new Button("Chọn ảnh");
        final String[] imgPath = { null };

        chooseImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                try {
                    File dest = new File("images", file.getName());
                    Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imgPreview.setImage(new Image(dest.toURI().toString()));
                    imgPath[0] = file.getName();
                } catch (IOException ex) {
                    showAlert("Lỗi ảnh", ex.getMessage());
                }
            }
        });

        if (bookToEdit != null) {
            title.setText(bookToEdit.getTitle());
            author.setText(bookToEdit.getAuthor());
            category.setText(bookToEdit.getCategory());
            importPrice.setText(String.valueOf(bookToEdit.getImportPrice()));
            price.setText(String.valueOf(bookToEdit.getPrice()));
            stock.setText(String.valueOf(bookToEdit.getStock()));
            rating.setText(String.valueOf(bookToEdit.getRating()));
            imgPath[0] = bookToEdit.getImagePath();
            if (imgPath[0] != null)
                imgPreview.setImage(new Image(new File("images", imgPath[0]).toURI().toString()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Tên sách:"), title);
        grid.addRow(1, new Label("Tác giả:"), author);
        grid.addRow(2, new Label("Thể loại:"), category);
        grid.addRow(3, new Label("Giá nhập:"), importPrice); // ✅
        grid.addRow(4, new Label("Giá bán:"), price);
        grid.addRow(5, new Label("Tồn kho:"), stock);
        grid.addRow(6, new Label("Đánh giá:"), rating);
        grid.addRow(7, new Label("Ảnh:"), chooseImg);
        grid.add(imgPreview, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    if (title.getText().isBlank() || author.getText().isBlank())
                        throw new Exception("Không được để trống tên hoặc tác giả");

                    double giaNhap = Double.parseDouble(importPrice.getText());
                    double giaBan = Double.parseDouble(price.getText());
                    int tonKho = Integer.parseInt(stock.getText());
                    double dg = Double.parseDouble(rating.getText());

                    if (giaNhap <= 0 || giaBan <= 0)
                        throw new Exception("Giá nhập và bán phải lớn hơn 0");
                    if (giaNhap > giaBan)
                        throw new Exception("Giá nhập không được lớn hơn giá bán");
                    if (tonKho < 0)
                        throw new Exception("Tồn kho không hợp lệ");
                    if (dg < 0 || dg > 5)
                        throw new Exception("Đánh giá phải từ 0 đến 5");

                    return new Book(
                            bookToEdit != null ? bookToEdit.getId() : 0,
                            title.getText(), author.getText(), category.getText(),
                            "", 0, giaNhap, giaBan, "", tonKho, dg, imgPath[0]);
                } catch (Exception ex) {
                    String message;
                    if (ex instanceof NumberFormatException) {
                        message = "Số lượng phải là số nguyên!";
                    } else if (ex instanceof NullPointerException) {
                        message = "Thiếu dữ liệu bắt buộc!";
                    } else {
                        message = "Đã xảy ra lỗi: " + ex.getMessage();
                    }
                    showAlert("Lỗi nhập liệu", message);
                    return null;
                }

            }
            return null;
        });

        dialog.showAndWait().ifPresent(book -> {
            if (bookToEdit == null)
                insertBookToDB(book);
            else
                updateBookInDB(book);
            loadBooksFromDatabase();
        });
    }

    private void showBookDetails(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết sách");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ImageView img = new ImageView();
        if (book.getImagePath() != null) {
            File file = new File("images", book.getImagePath());
            if (file.exists())
                img.setImage(new Image(file.toURI().toString()));
        }
        img.setFitHeight(140);
        img.setFitWidth(100);

        grid.addRow(0, new Label("Tên sách:"), new Label(book.getTitle()));
        grid.addRow(1, new Label("Tác giả:"), new Label(book.getAuthor()));
        grid.addRow(2, new Label("Thể loại:"), new Label(book.getCategory()));
        grid.addRow(3, new Label("Giá nhập:"), new Label(book.getImportPrice() + " đ")); // ✅
        grid.addRow(4, new Label("Giá bán:"), new Label(book.getPrice() + " đ"));
        grid.addRow(5, new Label("Tồn kho:"), new Label(String.valueOf(book.getStock())));
        grid.addRow(6, new Label("Đánh giá:"), new Label(book.getRating() + "★"));
        grid.add(img, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void insertBookToDB(Book b) {
        String sql = "INSERT INTO sach (ten_sach, tac_gia, the_loai, gia_nhap, gia_ban, so_luong_ton, danh_gia, hinh_anh) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, b.getTitle());
            stmt.setString(2, b.getAuthor());
            stmt.setString(3, b.getCategory());
            stmt.setDouble(4, b.getImportPrice());
            stmt.setDouble(5, b.getPrice());
            stmt.setInt(6, b.getStock());
            stmt.setDouble(7, b.getRating());
            stmt.setString(8, b.getImagePath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể thêm sách: " + e.getMessage());
        }
    }

    private void updateBookInDB(Book b) {
        String sql = "UPDATE sach SET ten_sach=?, tac_gia=?, the_loai=?, gia_nhap=?, gia_ban=?, so_luong_ton=?, danh_gia=?, hinh_anh=? WHERE ma_sach=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, b.getTitle());
            stmt.setString(2, b.getAuthor());
            stmt.setString(3, b.getCategory());
            stmt.setDouble(4, b.getImportPrice());
            stmt.setDouble(5, b.getPrice());
            stmt.setInt(6, b.getStock());
            stmt.setDouble(7, b.getRating());
            stmt.setString(8, b.getImagePath());
            stmt.setInt(9, b.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể cập nhật sách: " + e.getMessage());
        }
    }

    private void deleteBookFromDB(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM sach WHERE ma_sach = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể xóa sách: " + e.getMessage());
        }
    }

    private boolean confirmDelete(String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa sách: " + title + "?");
        alert.setContentText("Dữ liệu sẽ không thể khôi phục.");
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
