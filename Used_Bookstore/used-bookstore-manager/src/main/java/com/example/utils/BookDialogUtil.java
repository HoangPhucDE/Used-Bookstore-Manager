package com.example.utils;

import com.example.model.Book;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.File;

public class BookDialogUtil {

    public static void showBookDetails(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết sách");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Hiển thị ảnh sách nếu có
        ImageView img = new ImageView();
        if (book.getImagePath() != null) {
            File file = new File("images", book.getImagePath());
            if (file.exists()) {
                img.setImage(new Image(file.toURI().toString()));
            }
        }
        img.setFitHeight(140);
        img.setFitWidth(100);

        // Hiển thị thông tin sách
        grid.addRow(0, new Label("Tên sách:"), new Label(book.getTitle()));
        grid.addRow(1, new Label("Tác giả:"), new Label(book.getAuthor()));
        grid.addRow(2, new Label("Thể loại:"), new Label(book.getCategory()));

        // Định dạng giá bán
        grid.addRow(3, new Label("Giá bán:"), new Label(CurrencyFormatter.format(book.getPrice())));

        // Nếu muốn hiển thị giá nhập, bỏ comment dòng sau:
        // grid.addRow(4, new Label("Giá nhập:"), new Label(CurrencyFormatter.format(book.getImportPrice())));

        grid.addRow(4, new Label("Tồn kho:"), new Label(String.valueOf(book.getStock())));
        grid.addRow(5, new Label("Đánh giá:"), new Label(book.getRating() + "★"));

        // Thêm ảnh vào dòng cuối
        grid.add(img, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}
