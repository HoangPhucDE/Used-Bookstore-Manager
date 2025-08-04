package com.example.model;

import javafx.beans.property.*;

public class Employee {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty role;     // Vai trò hệ thống: admin, user
    private final StringProperty chucVu;   // Chức vụ nghiệp vụ: Quản lý, Kho, Bán hàng...
    private final StringProperty ngaySinh;

    public Employee(String id, String name, String email, String phone, String role, String chucVu, String ngaySinh) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.role = new SimpleStringProperty(role);
        this.chucVu = new SimpleStringProperty(chucVu);
        this.ngaySinh = new SimpleStringProperty(ngaySinh);
    }

    // ✅ Constructor tạm cũ: fallback khi chưa có chucVu và ngaySinh
    public Employee(String id, String name, String email, String phone, String role) {
        this(id, name, email, phone, role, "", "");
    }

    // ✅ Constructor khác: có chucVu nhưng không có ngaySinh
    public Employee(String id, String name, String email, String phone, String role, String chucVu) {
        this(id, name, email, phone, role, chucVu, "");
    }

    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getRole() { return role.get(); }
    public String getChucVu() { return chucVu.get(); }
    public String getNgaySinh() { return ngaySinh.get(); }

    public void setId(String id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setRole(String role) { this.role.set(role); }
    public void setChucVu(String chucVu) { this.chucVu.set(chucVu); }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh.set(ngaySinh); }

    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty roleProperty() { return role; }
    public StringProperty chucVuProperty() { return chucVu; }
    public StringProperty ngaySinhProperty() { return ngaySinh; }
}
