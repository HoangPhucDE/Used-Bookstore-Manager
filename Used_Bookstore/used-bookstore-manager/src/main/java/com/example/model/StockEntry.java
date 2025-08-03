package com.example.model;

import java.time.LocalDate;

public class StockEntry {
    private int id;
    private int bookId;
    private String bookTitle;
    private int quantity;
    private LocalDate entryDate;

    public StockEntry(int id, int bookId, String bookTitle, int quantity, LocalDate entryDate) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.entryDate = entryDate;
    }

    // Getters và setters
    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int getQuantity() { return quantity; }
    public LocalDate getEntryDate() { return entryDate; }

    public void setId(int id) { this.id = id; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
}
