package com.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    /**
     * Chuyển scene mới và luôn set fullscreen
     *
     * @param stage  Stage hiện tại (lấy từ component.getScene().getWindow())
     * @param fxmlPath  Đường dẫn FXML (vd: "/com/example/View/Login.fxml")
     * @param title  Tiêu đề cửa sổ
     */
    public static void switchScene(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);

            // Full màn hình
            stage.setMaximized(true);
            stage.setResizable(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể tải FXML: " + fxmlPath);
        }
    }
}
