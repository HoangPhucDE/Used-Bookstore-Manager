package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Book;
import com.example.model.Employee;
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
                JOIN taikhoan ON taikhoan.id = nhanvien.id_taikhoan""";

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
            showAlert("Lỗi", "Không thể tải nhân viên từ CSDL: " + e.getMessage());
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
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Xác nhận xóa");
                confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa nhân viên này?");
                confirmAlert.setContentText("Họ tên: " + emp.getName() + "\nSĐT: " + emp.getPhone());

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            // Lấy id_taikhoan từ nhân viên
                            int accountId = -1;
                            try (PreparedStatement stmt = conn.prepareStatement("SELECT id_taikhoan FROM nhanvien WHERE ma_nv = ?")) {
                                stmt.setInt(1, Integer.parseInt(emp.getId()));
                                try (ResultSet rs = stmt.executeQuery()) {
                                    if (rs.next()) {
                                        accountId = rs.getInt("id_taikhoan");
                                    }
                                }
                            }

                            // Xóa nhân viên
                            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM nhanvien WHERE ma_nv = ?")) {
                                stmt.setInt(1, Integer.parseInt(emp.getId()));
                                stmt.executeUpdate();
                            }

                            // Xóa tài khoản nếu có
                            if (accountId != -1) {
                                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM taikhoan WHERE id = ?")) {
                                    stmt.setInt(1, accountId);
                                    stmt.executeUpdate();
                                }
                            }

                            // Xóa khỏi danh sách và hiển thị thông báo
                            employeeList.remove(emp);

                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setTitle("Xóa thành công");
                            info.setHeaderText(null);
                            info.setContentText("Đã xóa nhân viên: " + emp.getName());
                            info.showAndWait();

                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert("Lỗi", "Không thể xóa nhân viên: " + e.getMessage());
                        }
                    }
                });
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
        DatePicker birthDatePicker = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("SĐT (cũng dùng làm username & mật khẩu):"), phoneField);
        grid.addRow(3, new Label("Chức vụ (admin/user):"), roleField);
        grid.addRow(4, new Label("Ngày sinh:"), birthDatePicker); // <-- Thêm dòng này

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Employee(null, nameField.getText(), emailField.getText(), phoneField.getText(), roleField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(emp -> {
            try (Connection conn = DatabaseConnection.getConnection()) {

                String phone = emp.getPhone();

                // 1. Thêm tài khoản
                String insertAccount = """
                INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai)
                VALUES (?, ?, ?, ?, ?, TRUE)
            """;
                PreparedStatement accStmt = conn.prepareStatement(insertAccount, Statement.RETURN_GENERATED_KEYS);

                accStmt.setString(1, phone);
                accStmt.setString(2, phone);
                accStmt.setString(3, emp.getRole());
                accStmt.setString(4, "nhanvien");
                accStmt.setString(5, emp.getEmail());

                accStmt.executeUpdate();

                int accountId;
                try (ResultSet genKeys = accStmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                        accountId = genKeys.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được ID tài khoản.");
                    }
                }

                // 2. Thêm nhân viên (có ngày sinh)
                String insertEmp = """
                INSERT INTO nhanvien (ho_ten, ngay_sinh, sdt, chuc_vu, trang_thai, id_taikhoan)
                VALUES (?, ?, ?, ?, TRUE, ?)
            """;
                PreparedStatement empStmt = conn.prepareStatement(insertEmp, Statement.RETURN_GENERATED_KEYS);

                empStmt.setString(1, emp.getName());
                empStmt.setDate(2, birthDatePicker.getValue() != null ? Date.valueOf(birthDatePicker.getValue()) : null);
                empStmt.setString(3, phone);
                empStmt.setString(4, emp.getRole());
                empStmt.setInt(5, accountId);

                empStmt.executeUpdate();

                try (ResultSet keys = empStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        emp.setId(String.valueOf(keys.getInt(1)));
                        employeeList.add(emp);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể thêm nhân viên: " + e.getMessage());
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
            try (Connection conn = DatabaseConnection.getConnection()) {

                // Cập nhật bảng nhân viên
                String updateEmployee = """
                UPDATE nhanvien
                SET ho_ten = ?, sdt = ?, chuc_vu = ?
                WHERE ma_nv = ?
            """;
                try (PreparedStatement stmt = conn.prepareStatement(updateEmployee)) {
                    stmt.setString(1, updated.getName());
                    stmt.setString(2, updated.getPhone());
                    stmt.setString(3, updated.getRole());
                    stmt.setInt(4, Integer.parseInt(updated.getId()));
                    stmt.executeUpdate();
                }

                // Lấy id_taikhoan từ nhân viên
                int idTaikhoan = -1;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id_taikhoan FROM nhanvien WHERE ma_nv = ?")) {
                    stmt.setInt(1, Integer.parseInt(updated.getId()));
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            idTaikhoan = rs.getInt("id_taikhoan");
                        }
                    }
                }

                if (idTaikhoan != -1) {
                    // Cập nhật bảng tài khoản
                    String updateAccount = """
                    UPDATE taikhoan
                    SET email = ?, vai_tro = ?, username = ?, mat_khau = ?
                    WHERE id = ?
                """;
                    try (PreparedStatement stmt = conn.prepareStatement(updateAccount)) {
                        stmt.setString(1, updated.getEmail());
                        stmt.setString(2, updated.getRole());
                        stmt.setString(3, updated.getPhone()); // username = sdt
                        stmt.setString(4, updated.getPhone()); // mật khẩu = sdt
                        stmt.setInt(5, idTaikhoan);
                        stmt.executeUpdate();
                    }
                }

                employeeTable.refresh();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể cập nhật nhân viên: " + e.getMessage());
            }
        });
    }

}