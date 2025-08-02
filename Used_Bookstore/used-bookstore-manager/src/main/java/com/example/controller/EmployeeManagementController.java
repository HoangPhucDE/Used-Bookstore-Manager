package com.example.controller;

import com.example.controller.dao.AccountDao;
import com.example.controller.dao.EmployeeDao;
import com.example.model.Employee;
import com.example.utils.ValidationUtils;
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

public class EmployeeManagementController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colRole;
    @FXML private TableColumn<Employee, Void> colActions;
    @FXML private TextField searchField;

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final FilteredList<Employee> filteredList = new FilteredList<>(employeeList, p -> true);

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

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
                        || emp.getEmail().toLowerCase().contains(keyword);
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
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("admin", "user"));
        roleBox.getSelectionModel().selectFirst();
        DatePicker dobPicker = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");
        grid.addRow(0, new Label("Họ tên:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("SĐT:"), phoneField);
        grid.addRow(3, new Label("Username:"), usernameField);
        grid.addRow(4, new Label("Mật khẩu:"), passwordField);
        grid.addRow(5, new Label("Vai trò:"), roleBox);
        grid.addRow(6, new Label("Ngày sinh:"), dobPicker);

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
            String role = roleBox.getValue();
            java.time.LocalDate dob = dobPicker.getValue();

            // 1. Kiểm tra rỗng
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                    || username.isEmpty() || password.isEmpty() || dob == null) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                event.consume();
                return;
            }

            // 2. Kiểm tra định dạng
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert("Lỗi", "Email không hợp lệ.");
                emailField.requestFocus();
                event.consume();
                return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                showAlert("Lỗi", "SĐT phải có 10 chữ số và bắt đầu bằng 0.");
                phoneField.requestFocus();
                event.consume();
                return;
            }

            if (!ValidationUtils.isValidUsername(username)) {
                showAlert("Lỗi", "Username phải từ 4 ký tự trở lên.");
                usernameField.requestFocus();
                event.consume();
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                showAlert("Lỗi", "Mật khẩu phải từ 6 ký tự trở lên.");
                passwordField.requestFocus();
                event.consume();
                return;
            }

            if (!ValidationUtils.isValidDateOfBirth(dob)) {
                showAlert("Lỗi", "Ngày sinh không hợp lệ.");
                dobPicker.requestFocus();
                event.consume();
                return;
            }

            // 3. Xác nhận thêm
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận thêm");
            confirm.setHeaderText("Bạn có chắc muốn thêm nhân viên?");
            confirm.setContentText("Tên: " + name + "\nUsername: " + username);
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                event.consume();
                return;
            }

            try {
                AccountDao accountDao = new AccountDao();

                // 4. Kiểm tra trùng
                if (accountDao.findAccountIdByUsername(username) != null) {
                    showAlert("Lỗi", "Username đã tồn tại. Hãy chọn tên khác.");
                    usernameField.requestFocus();
                    event.consume();
                    return;
                }

                if (accountDao.isEmailExists(email)) {
                    showAlert("Lỗi", "Email đã tồn tại. Hãy chọn email khác.");
                    emailField.requestFocus();
                    event.consume();
                    return;
                }

                // 5. Tạo tài khoản + nhân viên
                int accId = employeeDao.createAccount(username, password, role, email);
                if (accId == -1) throw new Exception("Không thể tạo tài khoản.");

                Employee emp = new Employee(null, name, email, phone, role);
                int empId = employeeDao.addEmployee(emp, Date.valueOf(dob), accId);
                if (empId != -1) {
                    emp.setId(String.valueOf(empId));
                    employeeList.add(emp);
                    showAlert("Thành công", "Đã thêm nhân viên mới.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không thể thêm nhân viên:\n" + ex.getMessage());
                event.consume();
            }
        });


        // Xác nhận nếu Cancel khi đã nhập dữ liệu
        dialog.setOnCloseRequest(evt -> {
            if (dialog.getResult() == ButtonType.CANCEL) {
                boolean hasInput = !nameField.getText().isBlank()
                        || !emailField.getText().isBlank()
                        || !phoneField.getText().isBlank()
                        || !usernameField.getText().isBlank()
                        || !passwordField.getText().isBlank()
                        || dobPicker.getValue() != null;

                if (hasInput) {
                    Alert cancelConfirm = new Alert(Alert.AlertType.CONFIRMATION);
                    cancelConfirm.setTitle("Xác nhận huỷ");
                    cancelConfirm.setHeaderText("Bạn có chắc muốn huỷ thao tác?");
                    cancelConfirm.setContentText("Thông tin đã nhập sẽ bị mất.");
                    if (cancelConfirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        evt.consume(); // Ngăn đóng dialog
                    }
                }
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
            try {
                employeeDao.updateEmployee(updated);

                int accId = employeeDao.getAccountIdByEmployeeId(Integer.parseInt(updated.getId()));
                if (accId != -1) {
                    employeeDao.updateAccount(accId, updated);
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
