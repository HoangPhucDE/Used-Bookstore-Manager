package com.example.controller.dao;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.Connection;

/**
 * Lớp xử lý logic tạo tài khoản và tạo khách hàng kèm theo tài khoản nếu chưa có.
 * Được sử dụng trong quy trình bán hàng khi cần tạo tài khoản khách nhanh.
 */
public class CustomerAccountService {

    private final AccountDao accountDao = new AccountDao();
    private final CustomerDao customerDao = new CustomerDao();

    /**
     * Tạo tài khoản và khách hàng nếu chưa tồn tại.
     *
     * @param conn      Kết nối đang mở (transaction đang xử lý)
     * @param username  Tên đăng nhập
     * @param password  Mật khẩu
     * @param email     Email khách
     * @param name      Tên khách
     * @param phone     Số điện thoại khách
     * @param address   Địa chỉ khách
     * @return ID tài khoản nếu thành công, -1 nếu thất bại hoặc người dùng huỷ.
     */
    public int createCustomerAccountIfNotExists(Connection conn,
                                                String username, String password, String email,
                                                String name, String phone, String address) {
        try {
            // 1. Kiểm tra tài khoản đã tồn tại?
            Integer accountId = accountDao.findAccountIdByUsername(username);
            if (accountId != null) {
                // 1.1. Nếu đã có tài khoản, kiểm tra khách hàng gắn với tài khoản đó
                if (customerDao.customerExistsByAccountId(accountId)) {
                    return accountId;
                }

                // 1.2. Nếu chưa có khách hàng → tạo mới khách gắn với tài khoản đó
                customerDao.insertCustomer(conn, name, email, phone, address, accountId);
                return accountId;
            }

            // 2. Nếu tài khoản chưa tồn tại → hỏi người dùng có muốn tạo mới không?
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Tạo tài khoản mới");
            confirm.setHeaderText(null);
            confirm.setContentText("Tài khoản chưa tồn tại. Bạn có muốn tạo mới không?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return -1;
            }

            // 3. Tạo tài khoản mới trong bảng taikhoan
            int newAccountId = accountDao.createNewCustomerAccount(conn, username, password, email);

            // 4. Gắn tài khoản đó vào khách hàng mới
            customerDao.insertCustomer(conn, name, email, phone, address, newAccountId);

            return newAccountId;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tạo tài khoản hoặc khách hàng: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Không thể xử lý tài khoản");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
