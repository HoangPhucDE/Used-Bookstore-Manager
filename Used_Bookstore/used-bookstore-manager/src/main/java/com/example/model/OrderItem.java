// OrderItem.java
package com.example.model;

public class OrderItem {
    private int bookId;
    private String bookTitle;
    private int quantity;
    private double unitPrice;

    public OrderItem(int bookId, String bookTitle, int quantity, double unitPrice) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getBookId() {return bookId;}
    public String getBookTitle() { return bookTitle; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return quantity * unitPrice; }
}

