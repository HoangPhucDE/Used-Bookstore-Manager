package com.example.controller.auth;

import com.example.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        // Khởi tạo ComboBox với các vai trò
        roleComboBox.getItems().addAll("Admin", "Nhân viên", "Khách hàng");
        
        // Thêm validation cho các trường input
        addInputValidation();
        
        // Focus vào trường đầu tiên
        fullNameField.requestFocus();
        
        // Setup Enter key navigation
        fullNameField.setOnAction(e -> usernameField.requestFocus());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> roleComboBox.requestFocus());
    }

    @FXML
    private void handleRegister() {
        // Lấy dữ liệu từ form
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate dữ liệu
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || 
            email.isEmpty() || role == null) {
            showMessage("Vui lòng điền đầy đủ thông tin!", false);
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Email không hợp lệ!", false);
            return;
        }

        if (password.length() < 6) {
            showMessage("Mật khẩu phải có ít nhất 6 ký tự!", false);
            return;
        }

        // Kiểm tra username đã tồn tại chưa
        if (isUsernameExists(username)) {
            showMessage("Tên đăng nhập đã tồn tại!", false);
            return;
        }

        // Đăng ký tài khoản
        if (registerUser(fullName, username, password, email, role)) {
            showMessage("Đăng ký thành công! Đang chuyển về trang đăng nhập...", true);
            
            // Delay 1.5 giây rồi chuyển về login
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::handleBackToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showMessage("Đăng ký thất bại! Vui lòng thử lại.", false);
        }
    }

@FXML
private void handleBackToLogin() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/auth/Login.fxml"));
        Parent loginRoot = loader.load();

        Stage stage = (Stage) fullNameField.getScene().getWindow();
        Scene newScene = new Scene(loginRoot);
        stage.setScene(newScene);
        stage.setTitle("Đăng nhập hệ thống");

         stage.setResizable(true);
        stage.setMaximized(true); 

    } catch (IOException e) {
        e.printStackTrace();
      
    }
}


    private boolean registerUser(String fullName, String username, String password, String email, String role) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Bắt đầu transaction
            conn.setAutoCommit(false);
            
            // Tạo tài khoản
            String insertAccountSql = "INSERT INTO taikhoan (username, mat_khau, vai_tro, email) VALUES (?, ?, ?, ?)";
            int accountId;
            
            try (PreparedStatement stmt = conn.prepareStatement(insertAccountSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, role);
                stmt.setString(4, email);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        accountId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
            // Tạo bản ghi trong bảng tương ứng
            String insertUserSql;
            if ("Nhân viên".equals(role) || "Admin".equals(role)) {
                insertUserSql = "INSERT INTO nhanvien (ho_ten, email, id_taikhoan) VALUES (?, ?, ?)";
            } else {
                insertUserSql = "INSERT INTO khachhang (ho_ten, email, id_taikhoan) VALUES (?, ?, ?)";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(insertUserSql)) {
                stmt.setString(1, fullName);
                stmt.setString(2, email);
                stmt.setInt(3, accountId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isUsernameExists(String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM taikhoan WHERE username = ?")) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        messageLabel.setTextFill(isSuccess ? 
            javafx.scene.paint.Color.valueOf("#27ae60") : 
            javafx.scene.paint.Color.valueOf("#e74c3c"));
    }

    private void addInputValidation() {
        String focusStyle = "-fx-background-color: #ffffff; -fx-border-color: #667eea;"
                + "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-padding: 10 15; -fx-font-size: 14px;";
        String normalStyle = "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 15;"
                + "-fx-font-size: 14px;";

        // Thêm focus listeners cho các text fields
        fullNameField.focusedProperty().addListener((obs, oldVal, newVal) ->
                fullNameField.setStyle(newVal ? focusStyle : normalStyle));
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) ->
                usernameField.setStyle(newVal ? focusStyle : normalStyle));
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) ->
                passwordField.setStyle(newVal ? focusStyle : normalStyle));
        emailField.focusedProperty().addListener((obs, oldVal, newVal) ->
                emailField.setStyle(newVal ? focusStyle : normalStyle));
    }
}