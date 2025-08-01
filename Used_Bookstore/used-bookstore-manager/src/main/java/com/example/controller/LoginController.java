package com.example.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin đăng nhập!");
            return;
        }

        String role = getRoleByCredentials(username, password);

        if (role != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/Home.fxml"));
                Parent homeRoot = loader.load();

                // Lấy controller và truyền role
                HomeController controller = loader.getController();
                controller.setUser(username, role);

                Scene homeScene = new Scene(homeRoot);
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.setScene(homeScene);
                currentStage.setTitle("Trang chủ");
                currentStage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể tải giao diện chính.");
            }
        } else {
            showAlert("Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }

    private String getRoleByCredentials(String username, String password) {
        if (username.equals("admin") && password.equals("123456")) {
            return "ADMIN";
        } else if (username.equals("nhanvien") && password.equals("123456")) {
            return "EMPLOYEE";
        } else if (username.equals("khach") && password.equals("123456")) {
            return "CUSTOMER";
        }
        return null;
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Thông báo", "Tính năng khôi phục mật khẩu sẽ được cập nhật sớm!");
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #5a6fd8, #6a42a0); -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 0; -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 0; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
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
        String focusStyle = "-fx-background-color: #ffffff; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px;";
        String normalStyle = "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 15; -fx-font-size: 14px;";

        usernameField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            usernameField.setStyle(isNowFocused ? focusStyle : normalStyle);
        });

        passwordField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            passwordField.setStyle(isNowFocused ? focusStyle : normalStyle);
        });
    }
}
