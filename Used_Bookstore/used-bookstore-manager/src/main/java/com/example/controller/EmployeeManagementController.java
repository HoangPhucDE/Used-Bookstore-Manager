package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.*;

public class EmployeeManagementController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, Void> colActions;
    @FXML private TextField searchField;

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
        String query = """
                SELECT ma_nv, ho_ten, sdt, chuc_vu, taikhoan.email 
                FROM nhanvien
                JOIN taikhoan ON taikhoan.id = nhanvien.id_taikhoan
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
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
            showAlert("Lỗi", "Không thể tải nhân viên: " + e.getMessage());
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

                btnEdit.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> deleteEmployee(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void deleteEmployee(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa nhân viên: " + emp.getName(), ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Bạn chắc chắn muốn xóa?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    int accId = -1;
                    try (PreparedStatement ps = conn.prepareStatement("SELECT id_taikhoan FROM nhanvien WHERE ma_nv=?")) {
                        ps.setInt(1, Integer.parseInt(emp.getId()));
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) accId = rs.getInt(1);
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM nhanvien WHERE ma_nv=?")) {
                        ps.setInt(1, Integer.parseInt(emp.getId()));
                        ps.executeUpdate();
                    }

                    if (accId != -1) {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM taikhoan WHERE id=?")) {
                            ps.setInt(1, accId);
                            ps.executeUpdate();
                        }
                    }

                    employeeList.remove(emp);
                } catch (SQLException e) {
                    showAlert("Lỗi", "Không thể xóa: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void showAddEmployeeDialog() {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhân viên");

        TextField name = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("admin", "user"));
        roleBox.getSelectionModel().selectFirst();
        DatePicker dob = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), name);
        grid.addRow(1, new Label("Email:"), email);
        grid.addRow(2, new Label("SĐT (cũng là username và mật khẩu):"), phone);
        grid.addRow(3, new Label("Vai trò:"), roleBox);
        grid.addRow(4, new Label("Ngày sinh:"), dob);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Employee(null, name.getText(), email.getText(), phone.getText(), roleBox.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(emp -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String insertAccount = """
                    INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai)
                    VALUES (?, ?, ?, 'nhanvien', ?, TRUE)
                """;
                PreparedStatement accStmt = conn.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS);
                accStmt.setString(1, emp.getPhone());
                accStmt.setString(2, emp.getPhone());
                accStmt.setString(3, emp.getRole());
                accStmt.setString(4, emp.getEmail());
                accStmt.executeUpdate();

                int accId;
                try (ResultSet rs = accStmt.getGeneratedKeys()) {
                    rs.next();
                    accId = rs.getInt(1);
                }

                String insertEmp = """
                    INSERT INTO nhanvien (ho_ten, ngay_sinh, sdt, chuc_vu, trang_thai, id_taikhoan)
                    VALUES (?, ?, ?, ?, TRUE, ?)
                """;
                PreparedStatement empStmt = conn.prepareStatement(insertEmp, Statement.RETURN_GENERATED_KEYS);
                empStmt.setString(1, emp.getName());
                empStmt.setDate(2, dob.getValue() != null ? Date.valueOf(dob.getValue()) : null);
                empStmt.setString(3, emp.getPhone());
                empStmt.setString(4, emp.getRole());
                empStmt.setInt(5, accId);
                empStmt.executeUpdate();

                try (ResultSet rs = empStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        emp.setId(String.valueOf(rs.getInt(1)));
                        employeeList.add(emp);
                    }
                }

            } catch (SQLException e) {
                showAlert("Lỗi", "Không thể thêm nhân viên: " + e.getMessage());
            }
        });
    }

    private void showEditDialog(Employee emp) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Sửa nhân viên");

        TextField name = new TextField(emp.getName());
        TextField email = new TextField(emp.getEmail());
        TextField phone = new TextField(emp.getPhone());
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("admin", "user"));
        roleBox.setValue(emp.getRole());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), name);
        grid.addRow(1, new Label("Email:"), email);
        grid.addRow(2, new Label("SĐT:"), phone);
        grid.addRow(3, new Label("Vai trò:"), roleBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                emp.setName(name.getText());
                emp.setEmail(email.getText());
                emp.setPhone(phone.getText());
                emp.setRole(roleBox.getValue());
                return emp;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String updateEmp = """
                    UPDATE nhanvien SET ho_ten=?, sdt=?, chuc_vu=? WHERE ma_nv=?
                """;
                PreparedStatement empStmt = conn.prepareStatement(updateEmp);
                empStmt.setString(1, updated.getName());
                empStmt.setString(2, updated.getPhone());
                empStmt.setString(3, updated.getRole());
                empStmt.setInt(4, Integer.parseInt(updated.getId()));
                empStmt.executeUpdate();

                int accId = -1;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id_taikhoan FROM nhanvien WHERE ma_nv = ?")) {
                    stmt.setInt(1, Integer.parseInt(updated.getId()));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) accId = rs.getInt("id_taikhoan");
                    }
                }

                if (accId != -1) {
                    String updateAccount = """
                        UPDATE taikhoan SET email=?, vai_tro=?, username=?, mat_khau=? WHERE id=?
                    """;
                    PreparedStatement accStmt = conn.prepareStatement(updateAccount);
                    accStmt.setString(1, updated.getEmail());
                    accStmt.setString(2, updated.getRole());
                    accStmt.setString(3, updated.getPhone());
                    accStmt.setString(4, updated.getPhone());
                    accStmt.setInt(5, accId);
                    accStmt.executeUpdate();
                }

                employeeTable.refresh();
            } catch (SQLException e) {
                showAlert("Lỗi", "Không thể cập nhật nhân viên: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
