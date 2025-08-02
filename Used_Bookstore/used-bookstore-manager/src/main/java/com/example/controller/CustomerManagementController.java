package com.example.controller;

import com.example.controller.dao.AccountDao;
import com.example.controller.dao.CustomerDao;
import com.example.model.Customer;
import com.example.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.Connection;

public class CustomerManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Customer> userTable;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colUsername;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colRole;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, String> colStatus;
    @FXML private TableColumn<Customer, Void> colActions;

    private final CustomerDao customerDao = new CustomerDao();
    private final AccountDao accountDao = new AccountDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("maKh"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        statusFilter.setItems(javafx.collections.FXCollections.observableArrayList("Tất cả", "Đang hoạt động", "Khóa"));
        statusFilter.getSelectionModel().selectFirst();

        loadDanhSachKhachHang("");
        setupActionButtons();
    }

    public void loadDanhSachKhachHang(String tuKhoa) {
        String filter = statusFilter.getValue();
        ObservableList<Customer> danhSach = customerDao.findAll(tuKhoa, filter);
        userTable.setItems(danhSach);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        loadDanhSachKhachHang(keyword);
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                btnEdit.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white;");
                btnEdit.setOnAction(event -> {
                    Customer kh = getTableView().getItems().get(getIndex());
                    showEditDialog(kh);
                });

                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btnDelete.setOnAction(event -> {
                    Customer kh = getTableView().getItems().get(getIndex());
                    handleDelete(kh);
                });
            }

            private final HBox hbox = new HBox(5, btnEdit, btnDelete);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    @FXML
    private void showAddCustomerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm khách hàng mới");
        dialog.setHeaderText("Nhập thông tin khách hàng");

        TextField hoTenField = new TextField();
        TextField emailField = new TextField();
        TextField sdtField = new TextField();
        TextField diaChiField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.addRow(0, new Label("Họ tên:"), hoTenField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Số điện thoại:"), sdtField);
        grid.addRow(3, new Label("Địa chỉ:"), diaChiField);
        grid.addRow(4, new Label("Tên đăng nhập:"), usernameField);
        grid.addRow(5, new Label("Mật khẩu:"), passwordField);

        dialog.getDialogPane().setContent(grid);
        ButtonType addType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addType);
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String hoTen = hoTenField.getText().trim();
            String email = emailField.getText().trim();
            String sdt = sdtField.getText().trim();
            String diaChi = diaChiField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty() || diaChi.isEmpty()
                    || username.isEmpty() || password.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                event.consume();
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                showAlert("Lỗi", "Email không hợp lệ. Vui lòng sửa lại.");
                emailField.requestFocus();
                event.consume();
                return;
            }

            if (!sdt.matches("\\d{9,11}")) {
                showAlert("Lỗi", "Số điện thoại phải là chuỗi số từ 9 đến 11 chữ số.");
                sdtField.requestFocus();
                event.consume();
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                if (accountDao.isEmailExists(email)) {
                    showAlert("Lỗi", "Email đã tồn tại. Vui lòng dùng email khác.");
                    emailField.requestFocus();
                    event.consume();
                    return;
                }

                if (accountDao.findAccountIdByUsername(username) != null) {
                    showAlert("Lỗi", "Tên đăng nhập đã tồn tại. Chọn tên khác.");
                    usernameField.requestFocus();
                    event.consume();
                    return;
                }

                int accountId = accountDao.createNewCustomerAccount(conn, username, password, email);
                customerDao.insertCustomer(conn, hoTen, email, sdt, diaChi, accountId);

                conn.commit();
                showAlert("Thành công", "Đã thêm khách hàng mới.");
                loadDanhSachKhachHang("");

            } catch (Exception e) {
                showAlert("Lỗi", "Không thể thêm khách hàng.\n" + e.getMessage());
                e.printStackTrace();
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void handleDelete(Customer kh) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xoá khách hàng");
        confirm.setHeaderText("Xác nhận xoá khách hàng?");
        confirm.setContentText("Bạn có chắc muốn xoá " + kh.getHoTen() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    int accountId = accountDao.findAccountIdByUsername(kh.getSoDienThoai());

                    if (accountId != -1) {
                        try (var stmt1 = conn.prepareStatement("DELETE FROM khachhang WHERE ma_kh = ?")) {
                            stmt1.setInt(1, kh.getMaKh());
                            stmt1.executeUpdate();
                        }
                        try (var stmt2 = conn.prepareStatement("DELETE FROM taikhoan WHERE id = ?")) {
                            stmt2.setInt(1, accountId);
                            stmt2.executeUpdate();
                        }
                        conn.commit();
                        showAlert("Thành công", "Đã xoá khách hàng và tài khoản.");
                        loadDanhSachKhachHang("");
                    }

                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể xoá khách hàng.\n" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void showEditDialog(Customer kh) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin khách hàng");
        dialog.setHeaderText("Cập nhật thông tin cho: " + kh.getHoTen());

        TextField hoTenField = new TextField(kh.getHoTen());
        TextField emailField = new TextField(kh.getEmail());
        TextField sdtField = new TextField(kh.getSoDienThoai());
        TextField diaChiField = new TextField(kh.getDiaChi());

        ComboBox<String> trangThaiBox = new ComboBox<>();
        trangThaiBox.getItems().addAll("Đang hoạt động", "Khóa");
        trangThaiBox.setValue(kh.getTrangThai());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.addRow(0, new Label("Họ tên:"), hoTenField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Số điện thoại:"), sdtField);
        grid.addRow(3, new Label("Địa chỉ:"), diaChiField);
        grid.addRow(4, new Label("Trạng thái:"), trangThaiBox);

        dialog.getDialogPane().setContent(grid);
        ButtonType updateType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateType, ButtonType.CANCEL);

        Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateType);
        updateButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String hoTen = hoTenField.getText().trim();
            String email = emailField.getText().trim();
            String sdt = sdtField.getText().trim();
            String diaChi = diaChiField.getText().trim();
            String trangThaiText = trangThaiBox.getValue();

            if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty() || diaChi.isEmpty() || trangThaiText == null) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                event.consume();
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                showAlert("Lỗi", "Email không hợp lệ.");
                emailField.requestFocus();
                event.consume();
                return;
            }

            if (!sdt.matches("\\d{9,11}")) {
                showAlert("Lỗi", "Số điện thoại phải là từ 9-11 chữ số.");
                sdtField.requestFocus();
                event.consume();
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);

                Customer updated = new Customer(
                        kh.getMaKh(), hoTen, email, sdt, diaChi, trangThaiText
                );

                customerDao.updateCustomer(conn, updated);

                boolean active = trangThaiText.equals("Đang hoạt động");
                customerDao.updateAccountStatusByCustomerId(conn, kh.getMaKh(), active);

                conn.commit();
                showAlert("Thành công", "Đã cập nhật thông tin khách hàng.");
                loadDanhSachKhachHang("");

            } catch (Exception e) {
                showAlert("Lỗi", "Không thể cập nhật khách hàng.\n" + e.getMessage());
                e.printStackTrace();
                event.consume();
            }
        });

        dialog.showAndWait();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
