package com.example.controller.employee;

import com.example.controller.dao.AccountDao;
import com.example.controller.dao.EmployeeDao;
import com.example.model.Employee;
import com.example.utils.ValidationUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.Date;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmployeeManagementController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colDob;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, String> colChucVu;
    @FXML private TableColumn<Employee, Void> colActions;
    @FXML private TextField searchField;

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredList = new FilteredList<>(employeeList, p -> true);

    private final Map<String, String> roleMap = Map.of("admin", "Quản trị viên", "user", "Người dùng");
    private final Map<String, String> chucVuMap = Map.of("quản lý", "Quản lý", "bán hàng", "Bán hàng", "kho", "Kho");
    private final Map<String, String> reverseRoleMap = Map.of("Quản trị viên", "admin", "Người dùng", "user");
    private final Map<String, String> reverseChucVuMap = Map.of("Quản lý", "quản lý", "Bán hàng", "bán hàng", "Kho", "kho");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colChucVu.setCellValueFactory(cell -> {
            String raw = cell.getValue().getChucVu();
            return new ReadOnlyStringWrapper(chucVuMap.getOrDefault(raw, raw));
        });

        colRole.setCellValueFactory(cell -> {
            String raw = cell.getValue().getRole();
            return new ReadOnlyStringWrapper(roleMap.getOrDefault(raw, raw));
        });

        loadEmployees();
        employeeTable.setItems(filteredList);
        setupSearch();
        addActionButtons();
    }

    private void loadEmployees() {
        employeeList.setAll(employeeDao.getAllEmployees());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(emp -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String keyword = newVal.toLowerCase();
                return emp.getName().toLowerCase().contains(keyword)
                        || emp.getEmail().toLowerCase().contains(keyword)
                        || emp.getPhone().contains(keyword);
            });
        });
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
                try {
                    employeeDao.deleteEmployeeAndAccount(Integer.parseInt(emp.getId()));
                    employeeList.remove(emp);
                } catch (SQLException e) {
                    showAlert("Lỗi", "Không thể xóa: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void showAddEmployeeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhân viên mới");

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("Quản trị viên", "Người dùng"));
        ComboBox<String> chucVuBox = new ComboBox<>(FXCollections.observableArrayList("Quản lý", "Bán hàng", "Kho"));
        DatePicker dobPicker = new DatePicker();

        roleBox.getSelectionModel().selectFirst();
        chucVuBox.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.addRow(0, new Label("Họ tên:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("SĐT:"), phoneField);
        grid.addRow(3, new Label("Username:"), usernameField);
        grid.addRow(4, new Label("Mật khẩu:"), passwordField);
        grid.addRow(5, new Label("Chức vụ:"), chucVuBox);
        grid.addRow(6, new Label("Vai trò hệ thống:"), roleBox);
        grid.addRow(7, new Label("Ngày sinh:"), dobPicker);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String vaiTro = reverseRoleMap.get(roleBox.getValue());
            String chucVu = reverseChucVuMap.get(chucVuBox.getValue());
            java.time.LocalDate dob = dobPicker.getValue();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                    username.isEmpty() || password.isEmpty() || dob == null) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                event.consume(); return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                showAlert("Lỗi", "Email không hợp lệ.");
                emailField.requestFocus(); event.consume(); return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                showAlert("Lỗi", "SĐT phải có 10 chữ số và bắt đầu bằng 0.");
                phoneField.requestFocus(); event.consume(); return;
            }

            if (!ValidationUtils.isValidUsername(username)) {
                showAlert("Lỗi", "Username phải từ 4 ký tự.");
                usernameField.requestFocus(); event.consume(); return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                showAlert("Lỗi", "Mật khẩu phải từ 6 ký tự.");
                passwordField.requestFocus(); event.consume(); return;
            }

            if (!ValidationUtils.isValidDateOfBirth(dob)) {
                showAlert("Lỗi", "Ngày sinh không hợp lệ.");
                dobPicker.requestFocus(); event.consume(); return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận thêm");
            confirm.setHeaderText("Bạn có chắc muốn thêm nhân viên?");
            confirm.setContentText("Tên: " + name + "\nUsername: " + username);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                event.consume(); return;
            }

            try {
                AccountDao accountDao = new AccountDao();

                if (accountDao.findAccountIdByUsername(username) != null) {
                    showAlert("Lỗi", "Username đã tồn tại.");
                    usernameField.requestFocus(); event.consume(); return;
                }

                if (accountDao.isEmailExists(email)) {
                    showAlert("Lỗi", "Email đã tồn tại.");
                    emailField.requestFocus(); event.consume(); return;
                }

                int accId = employeeDao.createAccount(username, password, vaiTro, email);
                if (accId == -1) throw new Exception("Không thể tạo tài khoản.");

                Employee emp = new Employee(null, name, email, phone, chucVu, dob.toString());
                int empId = employeeDao.addEmployee(emp, Date.valueOf(dob), accId);
                if (empId != -1) {
                    emp.setId(String.valueOf(empId));
                    emp.setRole(vaiTro);
                    employeeList.add(emp);
                    showAlert("Thành công", "Đã thêm nhân viên mới.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không thể thêm nhân viên:\n" + ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showEditDialog(Employee emp) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Sửa nhân viên");

        TextField name = new TextField(emp.getName());
        TextField email = new TextField(emp.getEmail());
        TextField phone = new TextField(emp.getPhone());

        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("Quản trị viên", "Người dùng"));
        roleBox.setValue(roleMap.getOrDefault(emp.getRole(), emp.getRole()));

        ComboBox<String> chucVuBox = new ComboBox<>(FXCollections.observableArrayList("Quản lý", "Bán hàng", "Kho"));
        chucVuBox.setValue(chucVuMap.getOrDefault(emp.getChucVu(), emp.getChucVu()));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Họ tên:"), name);
        grid.addRow(1, new Label("Email:"), email);
        grid.addRow(2, new Label("SĐT:"), phone);
        grid.addRow(3, new Label("Chức vụ:"), chucVuBox);
        grid.addRow(4, new Label("Vai trò hệ thống:"), roleBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                emp.setName(name.getText());
                emp.setEmail(email.getText());
                emp.setPhone(phone.getText());
                emp.setChucVu(reverseChucVuMap.getOrDefault(chucVuBox.getValue(), chucVuBox.getValue()));
                emp.setRole(reverseRoleMap.getOrDefault(roleBox.getValue(), roleBox.getValue()));
                return emp;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            try {
                employeeDao.updateEmployee(updated);
                int accId = employeeDao.getAccountIdByEmployeeId(Integer.parseInt(updated.getId()));
                if (accId != -1) {
                    employeeDao.updateEmailAndRole(accId, updated.getEmail(), updated.getRole());
                }
                employeeTable.refresh();
            } catch (SQLException e) {
                showAlert("Lỗi", "Không thể cập nhật: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}