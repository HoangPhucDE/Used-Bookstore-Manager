package com.example.model;

import com.example.utils.CurrencyFormatter;

public class OrderItem {
    private int bookId;             // Mã sách
    private String bookTitle;      // Tên sách
    private int quantity;          // Số lượng mua
    private double unitPrice;      // Đơn giá

    // Constructor khởi tạo OrderItem
    public OrderItem(int bookId, String bookTitle, int quantity, double unitPrice) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getter: Mã sách
    public int getBookId() {
        return bookId;
    }

    // Getter: Tên sách
    public String getBookTitle() {
        return bookTitle;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter: Số lượng
    public int getQuantity() {
        return quantity;
    }

    // Getter: Đơn giá (raw)
    public double getUnitPrice() {
        return unitPrice;
    }

    // Tính tổng giá = đơn giá * số lượng
    public double getTotalPrice() {
        return unitPrice * quantity;
    }

    // ✅ Định dạng đơn giá: 120.000 VNĐ
    public String getFormattedUnitPrice() {
        return CurrencyFormatter.format(unitPrice);
    }

    // ✅ Định dạng tổng tiền: 240.000 VNĐ
    public String getFormattedTotalPrice() {
        return CurrencyFormatter.format(getTotalPrice());
    }
}
