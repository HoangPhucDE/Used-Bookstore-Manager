package com.example.utils;

import com.example.model.Book;

public class BookUtils {
    public static javafx.util.StringConverter<Book> getBookStringConverter() {
        return new javafx.util.StringConverter<>() {
            @Override
            public String toString(Book book) {
                return (book == null)
                        ? ""
                        : String.format("%s (ID: %d, Tồn kho: %d)", book.getTitle(), book.getId(), book.getStock());
            }

            @Override
            public Book fromString(String string) {
                return null;
            }
        };
    }
}

