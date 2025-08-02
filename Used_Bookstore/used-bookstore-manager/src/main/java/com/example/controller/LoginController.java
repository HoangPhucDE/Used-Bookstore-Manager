package com.example.controller;

import com.example.DatabaseConnection;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    // Lưu thông tin người đăng nhập
    public static int currentUserId;
    public static String currentUserRole;
    public static String currentUserName;
    public static String currentUserFullname;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin đăng nhập!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM taikhoan WHERE username = ? AND mat_khau = ?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Lưu thông tin user
                    currentUserId = rs.getInt("id");
                    currentUserRole = rs.getString("vai_tro");
                    currentUserName = rs.getString("username");

                    String sql = """
                                SELECT ho_ten FROM nhanvien WHERE id_taikhoan = ?
                                UNION
                                SELECT ho_ten FROM khachhang WHERE id_taikhoan = ?
                            """;
                    try (PreparedStatement nameStmt = conn.prepareStatement(sql)) {
                        nameStmt.setInt(1, currentUserId);
                        nameStmt.setInt(2, currentUserId);
                        try (ResultSet nameRs = nameStmt.executeQuery()) {
                            if (nameRs.next()) {
                                currentUserFullname = nameRs.getString("ho_ten");
                            } else {
                                currentUserFullname = "Người dùng";
                            }
                        }
                    }

                    openHomeView();

                } else {
                    showAlert("Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng.");
                }
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Lỗi kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openHomeView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/View/Home.fxml"));
            Parent homeRoot = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUser(currentUserId, currentUserFullname, currentUserRole); // Truyền thông tin người đăng
                                                                                         // nhập

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(homeRoot));
            stage.setTitle("📚 Trang chủ");

            // Đảm bảo full màn hình
            stage.setMaximized(true);
            // Hoặc sử dụng setFullScreen(true) nếu muốn full screen thật sự
            // stage.setFullScreen(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải giao diện chính.");
        }
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Thông báo", "Tính năng khôi phục mật khẩu sẽ được cập nhật sớm!");
    }

    @FXML
private void handleRegisterLink() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/View/RegisterView.fxml"));
        Parent registerRoot = loader.load();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene newScene = new Scene(registerRoot);
        stage.setScene(newScene);
        stage.setTitle("Đăng ký tài khoản");
 stage.setResizable(true);
        stage.setMaximized(true); 

    } catch (IOException e) {
        e.printStackTrace();
        showAlert("Lỗi", "Không thể mở giao diện đăng ký.");
    }
}

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #5a6fd8, #6a42a0);"
                + "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-padding: 12 0; -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);"
                + "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; "
                + "-fx-font-size: 14px; -fx-padding: 12 0; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initialize() {
        usernameField.requestFocus();
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        addInputValidation();
    }

    private void addInputValidation() {
        String focusStyle = "-fx-background-color: #ffffff; -fx-border-color: #667eea;"
                + "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"
                + "-fx-padding: 10 15; -fx-font-size: 14px;";
        String normalStyle = "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 15;"
                + "-fx-font-size: 14px;";

        usernameField.focusedProperty()
                .addListener((obs, oldVal, now) -> usernameField.setStyle(now ? focusStyle : normalStyle));
        passwordField.focusedProperty()
                .addListener((obs, oldVal, now) -> passwordField.setStyle(now ? focusStyle : normalStyle));
    }
}