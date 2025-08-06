package com.dao;

import com.example.utils.DatabaseConnection;
import com.example.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CustomerDao {

    /**
     * Kiểm tra khách hàng đã tồn tại theo ID tài khoản
     */
    public boolean customerExistsByAccountId(int accountId) {
        String sql = "SELECT ma_kh FROM khachhang WHERE id_taikhoan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Thêm khách hàng mới gắn với tài khoản
     */
    public void insertCustomer(Connection conn, String name, String email, String phone, String address, int accountId) throws SQLException {
        String sql = """
            INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setInt(5, accountId);
            stmt.executeUpdate();
        }
    }

    /**
     * Tìm thông tin khách hàng theo SĐT
     */
    public Customer findCustomerByPhoneWithoutStatus(String phone) {
        String query = "SELECT ma_kh, ho_ten, email, sdt, dia_chi FROM khachhang WHERE sdt = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setMaKh(rs.getInt("ma_kh"));
                    customer.setHoTen(rs.getString("ho_ten"));
                    customer.setEmail(rs.getString("email"));
                    customer.setSoDienThoai(rs.getString("sdt"));
                    customer.setDiaChi(rs.getString("dia_chi"));
                    return customer;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Customer findCustomerByPhone(String sdt) {
        String sql = "SELECT * FROM khachhang WHERE sdt = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sdt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int maKh = rs.getInt("ma_kh");
                String hoTen = rs.getString("ho_ten");
                String email = rs.getString("email");
                String diaChi = rs.getString("dia_chi");
                boolean trangThaiBool = rs.getBoolean("trang_thai"); // CSDL kiểu BOOLEAN
                String trangThai = trangThaiBool ? "Đang hoạt động" : "Khóa";
                int accountId = rs.getInt("id_taikhoan");

                return new Customer(maKh, hoTen, email, sdt, diaChi, trangThai, accountId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Customer findCustomerByAccountId(int accountId) {
        String sql = """
        SELECT kh.ma_kh, kh.ho_ten, kh.email, kh.sdt, kh.dia_chi, tk.trang_thai
        FROM khachhang kh
        JOIN taikhoan tk ON kh.id_taikhoan = tk.id
        WHERE kh.id_taikhoan = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean trangThaiBool = rs.getBoolean("trang_thai");
                String trangThai = trangThaiBool ? "Đang hoạt động" : "Khóa";

                return new Customer(
                        rs.getInt("ma_kh"),
                        rs.getString("ho_ten"),
                        rs.getString("email"),
                        rs.getString("sdt"),
                        rs.getString("dia_chi"),
                        trangThai,
                        accountId
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



    /**
     * Trả về danh sách khách hàng theo từ khoá và trạng thái
     */
    public ObservableList<Customer> findAll(String keyword, String statusFilter) {
        ObservableList<Customer> list = FXCollections.observableArrayList();

        String sql = """
        SELECT kh.ma_kh, kh.ho_ten, kh.email, kh.sdt, kh.dia_chi, kh.id_taikhoan, tk.trang_thai
        FROM khachhang kh
        JOIN taikhoan tk ON kh.id_taikhoan = tk.id
        WHERE (kh.ho_ten LIKE ? OR kh.email LIKE ? OR kh.sdt LIKE ?)
    """;

        boolean filterStatus = statusFilter != null && !statusFilter.equals("Tất cả");
        if (filterStatus) {
            sql += " AND tk.trang_thai = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            stmt.setString(1, likeKeyword);
            stmt.setString(2, likeKeyword);
            stmt.setString(3, likeKeyword);

            if (filterStatus) {
                boolean statusBool = statusFilter.equals("Đang hoạt động");
                stmt.setBoolean(4, statusBool);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boolean status = rs.getBoolean("trang_thai");
                String statusStr = status ? "Đang hoạt động" : "Khóa";

                Customer kh = new Customer(
                        rs.getInt("ma_kh"),
                        rs.getString("ho_ten"),
                        rs.getString("email"),
                        rs.getString("sdt"),
                        rs.getString("dia_chi"),
                        statusStr,
                        rs.getInt("id_taikhoan") // ✅ thêm accountId vào đây
                );
                list.add(kh);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public void updateCustomer(Connection conn, Customer customer) throws SQLException {
        String sql = """
        UPDATE khachhang
        SET ho_ten = ?, email = ?, sdt = ?, dia_chi = ?
        WHERE ma_kh = ?
    """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getHoTen());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getSoDienThoai());
            stmt.setString(4, customer.getDiaChi());
            stmt.setInt(5, customer.getMaKh());
            stmt.executeUpdate();
        }
    }
    public void updateAccountStatusByCustomerId(Connection conn, int customerId, boolean status) throws SQLException {
        String sql = """
        UPDATE taikhoan
        SET trang_thai = ?
        WHERE id = (
            SELECT id_taikhoan FROM khachhang WHERE ma_kh = ?
        )
    """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        }
    }
}
