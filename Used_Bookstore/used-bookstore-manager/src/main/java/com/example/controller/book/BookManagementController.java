package com.example.controller.book;

import com.example.controller.auth.LoginController;
import com.dao.BookDao;
import com.example.model.Book;
import com.example.utils.CurrencyFormatter;
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
import java.util.Optional;

public class BookManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colCategory;
    @FXML private TableColumn<Book, Double> colImportPrice;
    @FXML private TableColumn<Book, Double> colPrice;
    @FXML private TableColumn<Book, Integer> colStock;
    @FXML private TableColumn<Book, Double> colRating;
    @FXML private TableColumn<Book, Void> colActions;
    @FXML private Button addBookBtn;

    private final BookDao bookDao = new BookDao();
    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadBooks();
        setupSearch();
        setupActions();

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
        colImportPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        colImportPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : CurrencyFormatter.format(value));
            }
        });

        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : CurrencyFormatter.format(value));
            }
        });

    }

    private void loadBooks() {
        bookList.setAll(bookDao.getAllBooks());
        bookTable.setItems(bookList);
        bookTable.refresh();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchBook());
    }

    @FXML
    private void searchBook() {
        String keyword = toUnaccented(searchField.getText().trim());
        if (keyword.isEmpty()) {
            bookTable.setItems(bookList);
            bookTable.refresh(); // để khôi phục lại các nút xem/sửa/xóa
            return;
        }

        ObservableList<Book> filtered = FXCollections.observableArrayList();
        for (Book b : bookList) {
            if (
                    toUnaccented(b.getTitle()).contains(keyword) ||
                            toUnaccented(b.getAuthor()).contains(keyword) ||
                            toUnaccented(b.getCategory()).contains(keyword) ||
                            String.valueOf(b.getPrice()).contains(keyword) ||
                            String.valueOf(b.getImportPrice()).contains(keyword)
            ) {
                filtered.add(b);
            }
        }

        bookTable.setItems(filtered);
        bookTable.refresh(); // để giữ lại nút hành động
    }

    private void setupActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button editBtn = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.setOnAction(e -> showBookDetails(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> {
                    if (isAdmin()) showBookDialog(getTableView().getItems().get(getIndex()));
                });
                deleteBtn.setOnAction(e -> {
                    if (isAdmin()) {
                        Book b = getTableView().getItems().get(getIndex());
                        if (confirmDelete(b.getTitle())) {
                            bookDao.deleteBook(b.getId());
                            loadBooks();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

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

    private boolean isAdmin() {
        return "admin".equalsIgnoreCase(LoginController.currentUserRole);
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
        TextField importPrice = new TextField();
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
        grid.addRow(3, new Label("Giá nhập:"), importPrice);
        grid.addRow(4, new Label("Giá bán:"), price);
        grid.addRow(5, new Label("Tồn kho:"), stock);
        grid.addRow(6, new Label("Đánh giá:"), rating);
        grid.addRow(7, new Label("Ảnh:"), chooseImg);
        grid.add(imgPreview, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Nút OK: kiểm tra + xác nhận lưu
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (title.getText().isBlank() || author.getText().isBlank())
                    throw new Exception("Tên sách và tác giả không được để trống.");

                double giaNhap = Double.parseDouble(importPrice.getText());
                double giaBan = Double.parseDouble(price.getText());
                int tonKho = Integer.parseInt(stock.getText());
                double dg = Double.parseDouble(rating.getText());

                if (giaNhap <= 0 || giaBan <= 0)
                    throw new Exception("Giá nhập và giá bán phải lớn hơn 0.");
                if (giaNhap > giaBan)
                    throw new Exception("Giá nhập không được lớn hơn giá bán.");
                if (tonKho < 0)
                    throw new Exception("Số lượng tồn kho không hợp lệ.");
                if (dg < 0 || dg > 5)
                    throw new Exception("Đánh giá phải nằm trong khoảng từ 0 đến 5.");

                // xác nhận lưu
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận lưu");
                confirm.setHeaderText("Bạn có chắc chắn muốn lưu sách \"" + title.getText().trim() + "\"?");
                confirm.setContentText("Thông tin sẽ được cập nhật vào hệ thống.");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    event.consume(); // Không lưu nếu không xác nhận
                    return;
                }

                Book book = new Book(
                        bookToEdit != null ? bookToEdit.getId() : 0,
                        title.getText(), author.getText(), category.getText(),
                        "", 0, giaNhap, giaBan, "", tonKho, dg, imgPath[0]);

                if (bookToEdit == null)
                    bookDao.insertBook(book);
                else
                    bookDao.updateBook(book);

                loadBooks();

            } catch (NumberFormatException ex) {
                event.consume();
                showAlert("Lỗi định dạng", "Vui lòng nhập đúng định dạng số cho giá, tồn kho và đánh giá.");
            } catch (Exception ex) {
                event.consume();
                showAlert("Lỗi nhập liệu", ex.getMessage());
            }
        });

        // Nút Cancel: xác nhận huỷ
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận huỷ");
            confirm.setHeaderText("Bạn có chắc chắn muốn huỷ bỏ thao tác không?");
            confirm.setContentText("Mọi thay đổi sẽ không được lưu.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                event.consume(); // Không huỷ nếu không xác nhận
            }
        });

        dialog.showAndWait();
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
        grid.addRow(3, new Label("Giá nhập:"), new Label(CurrencyFormatter.format(book.getImportPrice())));
        grid.addRow(4, new Label("Giá bán:"), new Label(CurrencyFormatter.format(book.getPrice())));
        grid.addRow(5, new Label("Tồn kho:"), new Label(String.valueOf(book.getStock())));
        grid.addRow(6, new Label("Đánh giá:"), new Label(book.getRating() + "★"));
        grid.add(img, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private String toUnaccented(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // bỏ dấu
                .replaceAll("đ", "d").replaceAll("Đ", "D") // xử lý đ/Đ
                .toLowerCase();
    }

    private boolean confirmCancelDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận huỷ");
        alert.setHeaderText("Bạn có chắc chắn muốn huỷ bỏ thao tác không?");
        alert.setContentText("Mọi thay đổi sẽ không được lưu.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
