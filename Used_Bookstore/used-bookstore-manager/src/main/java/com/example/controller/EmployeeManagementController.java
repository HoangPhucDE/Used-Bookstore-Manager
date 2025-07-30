package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Employee;
import com.example.utils.DatabaseUtil;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.*;

public class EmployeeManagementController {
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, String> colId;
    @FXML
    private TableColumn<Employee, String> colName;
    @FXML
    private TableColumn<Employee, String> colEmail;
    @FXML
    private TableColumn<Employee, String> colPhone;
    @FXML
    private TableColumn<Employee, String> colRole;
    @FXML
    private TableColumn<Employee, Void> colActions;
    @FXML
    private TextField searchField;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredList = new FilteredList<>(employeeList, p -> true);

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadEmployeesFromDatabase();
        employeeTable.setItems(filteredList);
        setupSearch();
        addActionButtons();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(emp -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String keyword = newVal.toLowerCase();
                return emp.getName().toLowerCase().contains(keyword) || emp.getEmail().toLowerCase().contains(keyword);
            });
        });
    }

    private void loadEmployeesFromDatabase() {
        employeeList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ma_nv, ho_ten, email, sdt, chuc_vu FROM nhanvien")) {

            while (rs.next()) {
                employeeList.add(new Employee(
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
    }

    private void addActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox box = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                btnEdit.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    showEditDialog(emp);
                });

                btnDelete.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    deleteEmployee(emp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }

            private void deleteEmployee(Employee emp) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }
        });
    }

    @FXML
    private void showAddEmployeeDialog() {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhân viên");

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField roleField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("SĐT:"), phoneField);
        grid.addRow(3, new Label("Chức vụ:"), roleField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Employee(null, nameField.getText(), emailField.getText(), phoneField.getText(), roleField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(emp -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO nhanvien (ho_ten, email, sdt, chuc_vu, trang_thai) VALUES (?, ?, ?, ?, TRUE)",
                         Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, emp.getName());
                stmt.setString(2, emp.getEmail());
                stmt.setString(3, emp.getPhone());
                stmt.setString(4, emp.getRole());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        emp.setId(String.valueOf(keys.getInt(1)));
                        employeeList.add(emp);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void showEditDialog(Employee emp) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Sửa nhân viên");

        TextField nameField = new TextField(emp.getName());
        TextField emailField = new TextField(emp.getEmail());
        TextField phoneField = new TextField(emp.getPhone());
        TextField roleField = new TextField(emp.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("SĐT:"), phoneField);
        grid.addRow(3, new Label("Chức vụ:"), roleField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                emp.setName(nameField.getText());
                emp.setEmail(emailField.getText());
                emp.setPhone(phoneField.getText());
                emp.setRole(roleField.getText());
                return emp;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE nhanvien SET ho_ten = ?, email = ?, sdt = ?, chuc_vu = ? WHERE ma_nv = ?")) {

                stmt.setString(1, updated.getName());
                stmt.setString(2, updated.getEmail());
                stmt.setString(3, updated.getPhone());
                stmt.setString(4, updated.getRole());
                stmt.setInt(5, Integer.parseInt(updated.getId()));
                stmt.executeUpdate();
                employeeTable.refresh();

            } catch (SQLException e) {
                e.printStackTrace();

            }
        });
    }
}