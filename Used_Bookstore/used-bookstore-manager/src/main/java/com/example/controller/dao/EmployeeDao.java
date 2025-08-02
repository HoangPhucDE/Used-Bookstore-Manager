package com.example.controller.dao;

import com.example.DatabaseConnection;
import com.example.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String query = """
                SELECT ma_nv, ho_ten, sdt, chuc_vu, taikhoan.email 
                FROM nhanvien
                JOIN taikhoan ON taikhoan.id = nhanvien.id_taikhoan
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Employee(
                        String.valueOf(rs.getInt("ma_nv")),
                        rs.getString("ho_ten"),
                        rs.getString("email"),
                        rs.getString("sdt"),
                        rs.getString("chuc_vu")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int createAccount(String username, String password, String role, String email) throws SQLException {
        String insertAccount = """
                INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai)
                VALUES (?, ?, ?, 'nhanvien', ?, TRUE)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, email);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        return -1;
    }

    public int addEmployee(Employee emp, Date dob, int accId) throws SQLException {
        String insertEmp = """
                INSERT INTO nhanvien (ho_ten, ngay_sinh, sdt, chuc_vu, trang_thai, id_taikhoan)
                VALUES (?, ?, ?, ?, TRUE, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertEmp, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, emp.getName());
            stmt.setDate(2, dob);
            stmt.setString(3, emp.getPhone());
            stmt.setString(4, emp.getRole());
            stmt.setInt(5, accId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void deleteEmployeeAndAccount(int empId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int accId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id_taikhoan FROM nhanvien WHERE ma_nv=?")) {
                ps.setInt(1, empId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) accId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nhanvien WHERE ma_nv=?")) {
                ps.setInt(1, empId);
                ps.executeUpdate();
            }

            if (accId != -1) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM taikhoan WHERE id=?")) {
                    ps.setInt(1, accId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        }
    }

    public int getAccountIdByEmployeeId(int empId) throws SQLException {
        String query = "SELECT id_taikhoan FROM nhanvien WHERE ma_nv=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_taikhoan");
            }
        }
        return -1;
    }

    public void updateEmployee(Employee emp) throws SQLException {
        String updateEmp = "UPDATE nhanvien SET ho_ten=?, sdt=?, chuc_vu=? WHERE ma_nv=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateEmp)) {
            stmt.setString(1, emp.getName());
            stmt.setString(2, emp.getPhone());
            stmt.setString(3, emp.getRole());
            stmt.setInt(4, Integer.parseInt(emp.getId()));
            stmt.executeUpdate();
        }
    }

    public void updateAccount(int accId, Employee emp) throws SQLException {
        String updateAccount = """
            UPDATE taikhoan SET email=?, vai_tro=?, username=?, mat_khau=? WHERE id=?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateAccount)) {
            stmt.setString(1, emp.getEmail());
            stmt.setString(2, emp.getRole());
            stmt.setString(3, emp.getPhone());
            stmt.setString(4, emp.getPhone());
            stmt.setInt(5, accId);
            stmt.executeUpdate();
        }
    }
}
