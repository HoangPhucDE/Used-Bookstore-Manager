package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Tải file FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/views/auth/Login.fxml"));

        Parent root = loader.load();

        Scene loginScene = new Scene(root);

        primaryStage.setTitle("Used Bookstore Manager - Login");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(true); // Cho phép thay đổi kích thước
        primaryStage.setMaximized(true); // Tự động full màn hình (nếu muốn)

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
