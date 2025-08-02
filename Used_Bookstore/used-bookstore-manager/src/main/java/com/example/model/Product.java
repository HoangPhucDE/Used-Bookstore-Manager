package com.example.model;

public class Product {
    private int id;
    private String name;
    private String author;
    private String category;
    private String publisher;
    private int year;
    private double importPrice;
    private double sellPrice;
    private String condition;
    private String imageUrl;
    private int stock;
    private double rating;

    public Product(int id, String name, String author, String category, String publisher,
                   int year, double importPrice, double sellPrice, String condition,
                   String imageUrl, int stock, double rating) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.year = year;
        this.importPrice = importPrice;
        this.sellPrice = sellPrice;
        this.condition = condition;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.rating = rating;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public int getYear() { return year; }
    public double getImportPrice() { return importPrice; }
    public double getSellPrice() { return sellPrice; }
    public String getCondition() { return condition; }
    public String getImageUrl() { return imageUrl; }
    public int getStock() { return stock; }
    public double getRating() { return rating; }

    @Override
    public String toString() {
        return name + " - " + String.format("%,.0f", sellPrice) + " VNĐ";
    }
}
