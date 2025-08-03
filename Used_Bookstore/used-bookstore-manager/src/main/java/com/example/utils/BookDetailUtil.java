package com.example.utils;

import com.example.model.Book;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class BookDetailUtil {
    public static void showBookDetail(Book book) {
        if (book == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Tên sách: ").append(book.getTitle()).append("\n");
        sb.append("Tác giả: ").append(book.getAuthor()).append("\n");
        sb.append("Thể loại: ").append(book.getCategory()).append("\n");
        sb.append("Nhà XB: ").append(book.getPublisher()).append("\n");
        sb.append("Năm XB: ").append(book.getYear()).append("\n");
        sb.append("Giá nhập: ").append(String.format("%,.0f VNĐ", book.getImportPrice())).append("\n");
        sb.append("Giá bán: ").append(String.format("%,.0f VNĐ", book.getPrice())).append("\n");
        sb.append("Tình trạng: ").append(book.getCondition()).append("\n");
        sb.append("Tồn kho: ").append(book.getStock()).append(" cuốn\n");
        sb.append("Đánh giá: ").append(book.getRating()).append(" ★");

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Chi tiết sách");
        alert.setHeaderText(book.getTitle());
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}
