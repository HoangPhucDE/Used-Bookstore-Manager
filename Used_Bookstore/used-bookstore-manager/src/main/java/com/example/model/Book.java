package com.example.model;

import javafx.beans.property.*;

public class Book {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty category;
    private final StringProperty publisher;
    private final IntegerProperty year;
    private final DoubleProperty importPrice;
    private final DoubleProperty price;
    private final StringProperty condition;
    private final IntegerProperty stock;
    private final DoubleProperty rating;
    private final StringProperty imagePath;

    public Book(int id, String title, String author, String category,
                String publisher, int year, double importPrice, double price,
                String condition, int stock, double rating, String imagePath) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.category = new SimpleStringProperty(category);
        this.publisher = new SimpleStringProperty(publisher);
        this.year = new SimpleIntegerProperty(year);
        this.importPrice = new SimpleDoubleProperty(importPrice);
        this.price = new SimpleDoubleProperty(price);
        this.condition = new SimpleStringProperty(condition);
        this.stock = new SimpleIntegerProperty(stock);
        this.rating = new SimpleDoubleProperty(rating);
        this.imagePath = new SimpleStringProperty(imagePath);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getTitle() { return title.get(); }
    public String getAuthor() { return author.get(); }
    public String getCategory() { return category.get(); }
    public String getPublisher() { return publisher.get(); }
    public int getYear() { return year.get(); }
    public double getImportPrice() { return importPrice.get(); }
    public double getPrice() { return price.get(); }
    public String getCondition() { return condition.get(); }
    public int getStock() { return stock.get(); }
    public double getRating() { return rating.get(); }
    public String getImagePath() { return imagePath.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setTitle(String title) { this.title.set(title); }
    public void setAuthor(String author) { this.author.set(author); }
    public void setCategory(String category) { this.category.set(category); }
    public void setPublisher(String publisher) { this.publisher.set(publisher); }
    public void setYear(int year) { this.year.set(year); }
    public void setImportPrice(double price) { this.importPrice.set(price); }
    public void setPrice(double price) { this.price.set(price); }
    public void setCondition(String condition) { this.condition.set(condition); }
    public void setStock(int stock) { this.stock.set(stock); }
    public void setRating(double rating) { this.rating.set(rating); }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }

    // Property methods for JavaFX bindings
    public IntegerProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty publisherProperty() { return publisher; }
    public IntegerProperty yearProperty() { return year; }
    public DoubleProperty importPriceProperty() { return importPrice; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty conditionProperty() { return condition; }
    public IntegerProperty stockProperty() { return stock; }
    public DoubleProperty ratingProperty() { return rating; }
    public StringProperty imagePathProperty() { return imagePath; }
}
