package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class BookManagementController {

    @FXML private TextField timkiem;
    @FXML private TextField titleInput;
    @FXML private TextField authorInput;
    @FXML private TextField categoryInput;
    @FXML private TextField publisherInput;
    @FXML private TextField yearInput;
    @FXML private TextField importPriceInput;
    @FXML private TextField priceInput;
    @FXML private ComboBox<String> conditionComboBox;
    @FXML private TextField stockInput;
    @FXML private TextField ratingInput;
    @FXML private VBox inputSection;
    @FXML private Button addBookButton;
    @FXML private ImageView bookImageView;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colCategory;
    @FXML private TableColumn<Book, Double> colPrice;
    @FXML private TableColumn<Book, Integer> colStock;
    @FXML private TableColumn<Book, Double> colRating;
    @FXML private TableColumn<Book, Void> colActions;

    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private File selectedImageFile;
    private Book bookBeingEdited = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colAuthor.setCellValueFactory(data -> data.getValue().authorProperty());
        colCategory.setCellValueFactory(data -> data.getValue().categoryProperty());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        colStock.setCellValueFactory(data -> data.getValue().stockProperty().asObject());
        colRating.setCellValueFactory(data -> data.getValue().ratingProperty().asObject());

        conditionComboBox.getItems().setAll("moi", "cu", "tot", "trung_binh", "kem");

        loadBooksFromDatabase();
        addActionButtons();

        bookImageView.setOnMouseClicked(e -> chooseImage());
    }

    private void loadBooksFromDatabase() {
        bookList.clear();
        String query = "SELECT * FROM sach";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
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
                bookList.add(book);
            }
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải dữ liệu sách: " + e.getMessage());
        }
        bookTable.setItems(bookList);
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnEdit.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    bookBeingEdited = book;

                    titleInput.setText(book.getTitle());
                    authorInput.setText(book.getAuthor());
                    categoryInput.setText(book.getCategory());
                    publisherInput.setText(book.getPublisher());
                    yearInput.setText(String.valueOf(book.getYear()));
                    importPriceInput.setText(String.valueOf(book.getImportPrice()));
                    priceInput.setText(String.valueOf(book.getPrice()));
                    conditionComboBox.setValue(book.getCondition());
                    stockInput.setText(String.valueOf(book.getStock()));
                    ratingInput.setText(String.valueOf(book.getRating()));
                    addBookButton.setText("Cập nhật sách");

                    inputSection.setVisible(true);
                    inputSection.setManaged(true);

                    if (book.getImagePath() != null) {
                        File img = new File("images/" + book.getImagePath());
                        bookImageView.setImage(img.exists() ? new Image(img.toURI().toString()) : null);
                    } else bookImageView.setImage(null);
                });

                btnDelete.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "DELETE FROM sach WHERE ma_sach = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, book.getId());
                            stmt.executeUpdate();
                        }
                        showAlert("Đã xóa", "Sách đã được xóa.");
                        loadBooksFromDatabase();
                    } catch (SQLException ex) {
                        showAlert("Lỗi", "Không thể xóa: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn hình ảnh");
        File imagesFolder = new File("images");
        if (imagesFolder.exists()) {
            chooser.setInitialDirectory(imagesFolder);
        }
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        Window window = bookImageView.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            selectedImageFile = file;
            copyImageToImagesFolder(file);
            bookImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    private void copyImageToImagesFolder(File sourceFile) {
        File destDir = new File("images");
        if (!destDir.exists()) destDir.mkdirs();
        File destFile = new File(destDir, sourceFile.getName());

        try {
            if (!destFile.exists()) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            showAlert("Lỗi", "Không thể sao chép ảnh: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddBook() {
        try {
            String title = titleInput.getText();
            String author = authorInput.getText();
            String category = categoryInput.getText();
            String publisher = publisherInput.getText();
            int year = Integer.parseInt(yearInput.getText());
            double importPrice = Double.parseDouble(importPriceInput.getText());
            double salePrice = Double.parseDouble(priceInput.getText());
            String condition = conditionComboBox.getValue();
            int stock = Integer.parseInt(stockInput.getText());
            double rating = Double.parseDouble(ratingInput.getText());

            String imageFileName = selectedImageFile != null ? selectedImageFile.getName()
                    : bookBeingEdited != null ? bookBeingEdited.getImagePath() : null;

            if (imageFileName == null) {
                showAlert("Thiếu ảnh", "Vui lòng chọn ảnh sách.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (bookBeingEdited == null) {
                    String sql = "INSERT INTO sach (ten_sach, tac_gia, the_loai, nxb, nam_xb, gia_nhap, gia_ban, tinh_trang, so_luong_ton, danh_gia, hinh_anh) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, title);
                        stmt.setString(2, author);
                        stmt.setString(3, category);
                        stmt.setString(4, publisher);
                        stmt.setInt(5, year);
                        stmt.setDouble(6, importPrice);
                        stmt.setDouble(7, salePrice);
                        stmt.setString(8, condition);
                        stmt.setInt(9, stock);
                        stmt.setDouble(10, rating);
                        stmt.setString(11, imageFileName);
                        stmt.executeUpdate();
                        showAlert("Thành công", "Đã thêm sách mới.");
                    }
                } else {
                    String sql = "UPDATE sach SET ten_sach=?, tac_gia=?, the_loai=?, nxb=?, nam_xb=?, gia_nhap=?, gia_ban=?, tinh_trang=?, so_luong_ton=?, danh_gia=?, hinh_anh=? WHERE ma_sach=?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, title);
                        stmt.setString(2, author);
                        stmt.setString(3, category);
                        stmt.setString(4, publisher);
                        stmt.setInt(5, year);
                        stmt.setDouble(6, importPrice);
                        stmt.setDouble(7, salePrice);
                        stmt.setString(8, condition);
                        stmt.setInt(9, stock);
                        stmt.setDouble(10, rating);
                        stmt.setString(11, imageFileName);
                        stmt.setInt(12, bookBeingEdited.getId());
                        stmt.executeUpdate();
                        showAlert("Cập nhật", "Đã cập nhật sách.");
                    }
                }
            }

            clearInputs();
            loadBooksFromDatabase();
            inputSection.setVisible(false);
            inputSection.setManaged(false);
            bookBeingEdited = null;

        } catch (Exception e) {
            showAlert("Lỗi nhập liệu", e.getMessage());
        }
    }

    @FXML
    private void showAddBookDialog() {
        inputSection.setVisible(true);
        inputSection.setManaged(true);
        clearInputs();
        bookBeingEdited = null;
    }

    private void clearInputs() {
        titleInput.clear();
        authorInput.clear();
        categoryInput.clear();
        publisherInput.clear();
        yearInput.clear();
        importPriceInput.clear();
        priceInput.clear();
        stockInput.clear();
        ratingInput.clear();
        conditionComboBox.getSelectionModel().clearSelection();
        bookImageView.setImage(null);
        selectedImageFile = null;
        addBookButton.setText("Thêm sách");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void searchBook() {
        String keyword = timkiem.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            bookTable.setItems(bookList); // Hiển thị lại tất cả nếu rỗng
            return;
        }

        ObservableList<Book> filteredList = FXCollections.observableArrayList();
        for (Book book : bookList) {
            if (book.getTitle().toLowerCase().contains(keyword) ||
                    book.getAuthor().toLowerCase().contains(keyword) ||
                    book.getCategory().toLowerCase().contains(keyword)) {
                filteredList.add(book);
            }
        }
        bookTable.setItems(filteredList);
    }

}
