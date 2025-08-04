package com.example.controller.shared;

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

    @FXML private Button homeBtn, bookBtn, employeeBtn, userBtn, salesBtn, statsBtn, orderStatusBtn;
    @FXML private StackPane contentPane;
    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private Label usernameLabel;
    @FXML private Button stockBtn;

    private String role;
    private String username;
    private double xOffset = 0, yOffset = 0;

    private final String ACTIVE_STYLE = "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 12 20; -fx-font-size: 14px; -fx-font-weight: 500; -fx-alignment: center-left;";
    private final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #2c3e50; -fx-background-radius: 8; -fx-padding: 12 20; -fx-font-size: 14px; -fx-font-weight: 500; -fx-alignment: center-left;";

    @FXML
    public void initialize() {
        goHome();
        enableWindowDrag();
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

    public void setUser(int userID, String username, String role) {
        this.role = role;
        this.username = username;
        usernameLabel.setText("👤 " + username);

        switch (role) {
            case "admin" -> {
                // Full quyền
            }
            case "user" -> {
                employeeBtn.setVisible(false);
                userBtn.setVisible(false);
                statsBtn.setVisible(false);
                goBook();
            }
            case "khach" -> {
                sidebar.setVisible(false);
                goShopping();
            }
        }
    }

    private void setActiveButton(Button activeButton) {
        resetAllButtons();
        if (activeButton != null) activeButton.setStyle(ACTIVE_STYLE);
    }

    private void resetAllButtons() {
        if (homeBtn != null) homeBtn.setStyle(INACTIVE_STYLE);
        if (bookBtn != null) bookBtn.setStyle(INACTIVE_STYLE);
        if (employeeBtn != null) employeeBtn.setStyle(INACTIVE_STYLE);
        if (userBtn != null) userBtn.setStyle(INACTIVE_STYLE);
        if (salesBtn != null) salesBtn.setStyle(INACTIVE_STYLE);
        if (statsBtn != null) statsBtn.setStyle(INACTIVE_STYLE);
        if (orderStatusBtn != null) orderStatusBtn.setStyle(INACTIVE_STYLE);
        if (stockBtn != null) stockBtn.setStyle(INACTIVE_STYLE);
    }

    @FXML private void goHome()      { setActiveButton(homeBtn); loadPage("/com/example/views/dashboard/Dashboard.fxml"); }
    @FXML private void goBook()      { setActiveButton(bookBtn); loadPage("/com/example/views/book/BookManagement.fxml"); }
    @FXML private void goEmployee()  { setActiveButton(employeeBtn); loadPage("/com/example/views/employee/EmployeeManagement.fxml"); }
    @FXML private void goUser()      { setActiveButton(userBtn); loadPage("/com/example/views/customer/CustomerManagement.fxml"); }
    @FXML private void goSales()     { setActiveButton(salesBtn); loadPage("/com/example/views/sales/SalesView.fxml"); }
    @FXML private void goStats()     { setActiveButton(statsBtn); loadPage("/com/example/views/statistics/Statistics.fxml"); }
    @FXML private void goOrders()    { setActiveButton(orderStatusBtn); loadPage("/com/example/views/order/OrderStatus.fxml"); }
    @FXML private void goShopping()  { loadPage("/com/example/views/customer/CustomerShopping.fxml"); }
    @FXML private void goStock()    {setActiveButton(stockBtn); loadPage("/com/example/views/stock/StockManagementView.fxml");}

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            contentPane.getChildren().add(new Label("Không thể tải trang: " + fxmlPath));
        }
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/auth/Login.fxml"));
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
