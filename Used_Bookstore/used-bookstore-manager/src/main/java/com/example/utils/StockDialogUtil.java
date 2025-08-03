package com.example.utils;

import com.dao.BookDao;
import com.example.model.Book;
import com.example.model.StockEntry;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.util.*;

public class StockDialogUtil {

    public static Optional<List<StockEntry>> showMultiStockDialog(List<StockEntry> prefillEntries) {
        Dialog<List<StockEntry>> dialog = new Dialog<>();
        dialog.setTitle("Nhập kho nhiều sách");
        dialog.setHeaderText("Chỉnh sửa sách cần nhập");

        ButtonType submitBtn = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        VBox rowsBox = new VBox(10);
        rowsBox.setPadding(new Insets(10));

        BookDao bookDao = new BookDao();
        List<Book> allBooks = bookDao.getAllBooks();

        List<Row> rows = new ArrayList<>();

        if (prefillEntries != null && !prefillEntries.isEmpty()) {
            for (StockEntry entry : prefillEntries) {
                Book matchedBook = allBooks.stream()
                        .filter(b -> b.getId() == entry.getBookId())
                        .findFirst().orElse(null);

                if (matchedBook != null) {
                    rows.add(createRowWithData(allBooks, rowsBox, matchedBook, entry.getQuantity(), "tot"));
                }
            }
        } else {
            rows.add(createRow(allBooks, rowsBox));
        }

        Button addRowBtn = new Button("➕ Thêm dòng");
        addRowBtn.setOnAction(e -> rows.add(createRow(allBooks, rowsBox)));

        VBox container = new VBox(15, rowsBox, addRowBtn);
        container.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        dialog.getDialogPane().setContent(scrollPane);

        Node okButton = dialog.getDialogPane().lookupButton(submitBtn);
        okButton.setDisable(true);

        Runnable validator = () -> {
            boolean valid = rows.stream().allMatch(r ->
                    r.bookCombo.getValue() != null &&
                            r.quantityField.getText().matches("\\d+") &&
                            Integer.parseInt(r.quantityField.getText()) > 0 &&
                            r.conditionBox.getValue() != null
            );
            okButton.setDisable(!valid);
        };

        rows.forEach(r -> {
            r.bookCombo.valueProperty().addListener((obs, o, n) -> validator.run());
            r.quantityField.textProperty().addListener((obs, o, n) -> validator.run());
            r.conditionBox.valueProperty().addListener((obs, o, n) -> validator.run());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitBtn) {
                List<StockEntry> result = new ArrayList<>();
                for (Row r : rows) {
                    Book book = r.bookCombo.getValue();
                    int qty = Integer.parseInt(r.quantityField.getText());
                    result.add(new StockEntry(
                            0,
                            book.getId(),
                            book.getTitle(),
                            qty,
                            LocalDate.now(),
                            null, // createdBy
                            r.conditionBox.getValue()
                    ));

                }
                return result;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static Row createRow(List<Book> allBooks, VBox container) {
        return createRowWithData(allBooks, container, null, 0, "tot");
    }

    private static Row createRowWithData(List<Book> allBooks, VBox container, Book book, int quantity, String condition) {
        ComboBox<Book> bookCombo = new ComboBox<>();
        bookCombo.setItems(FXCollections.observableArrayList(allBooks));
        bookCombo.setConverter(BookUtils.getBookStringConverter());
        bookCombo.setPrefWidth(300);
        if (book != null) bookCombo.setValue(book);

        TextField quantityField = new TextField(quantity > 0 ? String.valueOf(quantity) : "");
        quantityField.setPromptText("Số lượng");
        quantityField.setPrefWidth(80);

        ComboBox<String> conditionBox = new ComboBox<>();
        conditionBox.setItems(FXCollections.observableArrayList("moi", "tot", "cu"));
        conditionBox.setPrefWidth(100);
        conditionBox.setPromptText("Tình trạng");
        conditionBox.setValue(condition);

        HBox rowBox = new HBox(10, new Label("Sách:"), bookCombo,
                new Label("SL:"), quantityField,
                new Label("Tình trạng:"), conditionBox);
        HBox.setHgrow(bookCombo, Priority.ALWAYS);
        container.getChildren().add(rowBox);

        return new Row(bookCombo, quantityField, conditionBox);
    }

    private static class Row {
        ComboBox<Book> bookCombo;
        TextField quantityField;
        ComboBox<String> conditionBox;

        Row(ComboBox<Book> bookCombo, TextField quantityField, ComboBox<String> conditionBox) {
            this.bookCombo = bookCombo;
            this.quantityField = quantityField;
            this.conditionBox = conditionBox;
        }
    }
}
