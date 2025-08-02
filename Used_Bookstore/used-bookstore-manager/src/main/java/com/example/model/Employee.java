package com.example.model;

import javafx.beans.property.*;

public class Employee {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty role;
    private final StringProperty ngaySinh; // ✅ Thêm thuộc tính ngày sinh

    public Employee(String id, String name, String email, String phone, String role, String ngaySinh) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.role = new SimpleStringProperty(role);
        this.ngaySinh = new SimpleStringProperty(ngaySinh); // ✅ Gán ngày sinh
    }

    // ✅ Constructor cũ để không bị lỗi nếu bạn vẫn dùng ở chỗ khác
    public Employee(String id, String name, String email, String phone, String role) {
        this(id, name, email, phone, role, "");
    }

    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getRole() { return role.get(); }
    public String getNgaySinh() { return ngaySinh.get(); } // ✅ getter ngày sinh

    public void setId(String id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setRole(String role) { this.role.set(role); }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh.set(ngaySinh); } // ✅ setter ngày sinh

    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty roleProperty() { return role; }
    public StringProperty ngaySinhProperty() { return ngaySinh; } // ✅ property ngày sinh
}
