package com.example.model;

import java.time.LocalDate;
import java.util.List;

public class Order {
    private int id;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private LocalDate orderDate;
    private String orderType;
    private String status;
    private String createdDate;
    private double total;
    private List<OrderItem> items;
    private int createdByUserId;

    public Order(int id, String customerName, String phone, String email, String address,
                 String orderType, String status, String createdDate) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.orderType = orderType;
        this.status = status;
        this.createdDate = createdDate;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public LocalDate getOrderDate() { return orderDate; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int userId) { this.createdByUserId = userId; }

    public double getTotal() {
        if (items != null && !items.isEmpty()) {
            return items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        }
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
