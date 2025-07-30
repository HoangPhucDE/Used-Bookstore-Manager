
package com.example.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeController {

    // Navigation buttons
    @FXML
    private Button homeBtn;
    @FXML
    private Button bookBtn;
    @FXML
    private Button employeeBtn;
    @FXML
    private Button userBtn;
    @FXML
    private Button salesBtn;
    @FXML
    private Button statsBtn;
    @FXML
    private StackPane contentPane;

    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox sidebar;
    @FXML
    private Label usernameLabel;

    private String role;

    private double xOffset = 0;
    private double yOffset = 0;

    // Button styles
    private final String ACTIVE_STYLE = "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 12 20; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-alignment: center-left;";

    private final String INACTIVE_STYLE = "-fx-background-color: transparent; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 12 20; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500; " +
            "-fx-alignment: center-left;";
    private String username;

    @FXML
    public void initialize() {
        goHome(); // Load trang dashboard mặc định
        enableWindowDrag(); // Cho phép kéo cửa sổ từ rootPane
    }

    private void enableWindowDrag() {
        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        rootPane.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void setUser(String username, String role) {
        this.role = role;
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText("👤 " + username);
        }

        switch (role) {
            case "admin" -> {
                // Admin: full quyền, không cần ẩn gì cả
            }
            case "user" -> {
                bookBtn.setVisible(false); // Không được quản lý sách
                employeeBtn.setVisible(false); // Không được quản lý nhân viên
                userBtn.setVisible(false);
                statsBtn.setVisible(false);
                sidebar.setVisible(false);
                goSales();
            }
            case "khach" -> {
                sidebar.setVisible(false); // Ẩn sidebar cho khách hàng
                bookBtn.setVisible(false); // Không được quản lý sách
                employeeBtn.setVisible(false); // Không được quản lý nhân viên
                userBtn.setVisible(false);
                salesBtn.setVisible(false);
                statsBtn.setVisible(false);
                goShopping(); // Chuyển đến trang mua sắm
            }
        }
    }

    private void setActiveButton(Button activeButton) {
        resetAllButtons();
        activeButton.setStyle(ACTIVE_STYLE);
    }

    private void resetAllButtons() {
        if (homeBtn != null)
            homeBtn.setStyle(INACTIVE_STYLE);
        if (bookBtn != null)
            bookBtn.setStyle(INACTIVE_STYLE);
        if (employeeBtn != null)
            employeeBtn.setStyle(INACTIVE_STYLE);
        if (userBtn != null)
            userBtn.setStyle(INACTIVE_STYLE);
        if (salesBtn != null)
            salesBtn.setStyle(INACTIVE_STYLE);
        if (statsBtn != null)
            statsBtn.setStyle(INACTIVE_STYLE);
    }

    @FXML
    private void goHome() {
        setActiveButton(homeBtn);
        loadHomePage();
    }

    @FXML
    private void goBook() {
        setActiveButton(bookBtn);
        loadPage("/com/example/view/BookManagement.fxml");
    }

    @FXML
    private void goEmployee() {
        setActiveButton(employeeBtn);
        loadPage("/com/example/view/EmployeeManagement.fxml");
    }

    @FXML
    private void goUser() {
        setActiveButton(userBtn);
        loadPage("/com/example/view/UserManagement.fxml");
    }

    @FXML
    private void goSales() {
        setActiveButton(salesBtn);
        loadPage("/com/example/view/SalesView.fxml");
    }

    @FXML
    private void goStats() {
        setActiveButton(statsBtn);
        loadPage("/com/example/view/Statistics.fxml");
    }

    @FXML
    private void goShopping() {
        // setActiveButton(statsBtn);
        loadPage("/com/example/view/CustomerShopping.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể load trang: " + fxmlPath);
        }
    }

    private void loadHomePage() {
        contentPane.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/Dashboard.fxml"));
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            Label welcomeLabel = new Label("🎉 Chào mừng đến với Hệ thống Quản lý Thư viện");
            welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            contentPane.getChildren().add(welcomeLabel);
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/view/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle("Đăng nhập");
            currentStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể chuyển về màn hình đăng nhập.");
        }
    }
}
