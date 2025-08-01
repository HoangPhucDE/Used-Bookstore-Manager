package com.example.model;

public class Customer {
    private int maKh; // tương ứng với cột ma_kh trong CSDL
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String trangThai;

    public Customer(int maKh, String hoTen, String email, String soDienThoai, String diaChi, String trangThai) {
        this.maKh = maKh;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.trangThai = trangThai;
    }

    // Getters
    public int getMaKh() {
        return maKh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getEmail() {
        return email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    // Setters
    public void setMaKh(int maKh) {
        this.maKh = maKh;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
