package com.example.controller;

import com.example.DatabaseConnection;
import com.example.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    private final ObservableList<Customer> danhSachKhachHang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("maKh"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        statusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Đang hoạt động", "Khóa"));
        statusFilter.getSelectionModel().selectFirst();

        loadDanhSachKhachHang("");
        setupActionButtons();
    }

    public void loadDanhSachKhachHang(String tuKhoa) {
        danhSachKhachHang.clear();

        String filter = statusFilter.getValue();
        boolean locTrangThai = filter != null && !filter.equals("Tất cả");

        String sql = """
            SELECT kh.*, tk.trang_thai
            FROM khachhang kh
            JOIN taikhoan tk ON kh.id_taikhoan = tk.id
            WHERE (kh.ho_ten LIKE ? OR kh.email LIKE ? OR kh.sdt LIKE ?)
        """ + (locTrangThai ? " AND tk.trang_thai = ?" : "");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String keyword = "%" + tuKhoa + "%";
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            stmt.setString(3, keyword);

            if (locTrangThai) {
                boolean filterValue = filter.equals("Đang hoạt động");
                stmt.setBoolean(4, filterValue);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boolean status = rs.getBoolean("trang_thai");
                String hienThiTrangThai = status ? "Đang hoạt động" : "Khóa";

                Customer kh = new Customer(
                        rs.getInt("ma_kh"),
                        rs.getString("ho_ten"),
                        rs.getString("email"),
                        rs.getString("sdt"),
                        rs.getString("dia_chi"),
                        hienThiTrangThai
                );
                danhSachKhachHang.add(kh);
            }

            userTable.setItems(danhSachKhachHang);

        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách khách hàng.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        loadDanhSachKhachHang(keyword);
    }

    @FXML
    private void showAddUserDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thêm khách hàng");
        dialog.setHeaderText("Tạo khách hàng và tài khoản đăng nhập");

        TextField hoTenField = new TextField();
        TextField emailField = new TextField();
        TextField sdtField = new TextField();
        TextField diaChiField = new TextField();
        ComboBox<String> trangThaiBox = new ComboBox<>();
        trangThaiBox.getItems().addAll("Đang hoạt động", "Khóa");
        trangThaiBox.getSelectionModel().selectFirst();

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
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        dialog.setResultConverter(button -> {
            if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String hoTen = hoTenField.getText().trim();
                String email = emailField.getText().trim();
                String sdt = sdtField.getText().trim();
                String diaChi = diaChiField.getText().trim();
                String trangThaiText = trangThaiBox.getValue();

                if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập đầy đủ họ tên, email, số điện thoại.");
                    return null;
                }

                boolean trangThai = trangThaiText.equals("Đang hoạt động");

                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    String insertUser = """
                        INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai)
                        VALUES (?, ?, 'khach', 'khachhang', ?, ?)
                    """;

                    int idTaiKhoan;
                    try (PreparedStatement stmt = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, sdt);
                        stmt.setString(2, sdt);
                        stmt.setString(3, email);
                        stmt.setBoolean(4, trangThai);
                        stmt.executeUpdate();

                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            idTaiKhoan = rs.getInt(1);
                        } else {
                            conn.rollback();
                            showAlert("Lỗi", "Không thể tạo tài khoản.");
                            return null;
                        }
                    }

                    String insertKH = """
                        INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan)
                        VALUES (?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement stmt = conn.prepareStatement(insertKH)) {
                        stmt.setString(1, hoTen);
                        stmt.setString(2, email);
                        stmt.setString(3, sdt);
                        stmt.setString(4, diaChi);
                        stmt.setInt(5, idTaiKhoan);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    showAlert("Thành công", "Đã thêm khách hàng và tài khoản.");
                    loadDanhSachKhachHang("");

                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể thêm khách hàng.\n" + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
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

    private void showEditDialog(Customer kh) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin khách hàng");
        dialog.setHeaderText("Cập nhật thông tin cho: " + kh.getHoTen());

        TextField hoTenField = new TextField(kh.getHoTen());
        TextField emailField = new TextField(kh.getEmail());
        TextField sdtField = new TextField(kh.getSoDienThoai());
        TextField diaChiField = new TextField(kh.getDiaChi());

        ComboBox<String> trangThaiBox = new ComboBox<>();
        trangThaiBox.getItems().addAll("Đang hoạt động", "Khóa");
        trangThaiBox.setValue(kh.getTrangThai()); // Đặt trạng thái hiện tại

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
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        dialog.setResultConverter(button -> {
            if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                String hoTen = hoTenField.getText().trim();
                String email = emailField.getText().trim();
                String sdt = sdtField.getText().trim();
                String diaChi = diaChiField.getText().trim();
                String trangThaiText = trangThaiBox.getValue();

                if (hoTen.isEmpty() || email.isEmpty() || sdt.isEmpty() || trangThaiText == null) {
                    showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
                    return null;
                }

                boolean trangThaiBool = trangThaiText.equals("Đang hoạt động");

                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.setAutoCommit(false);

                    // Cập nhật thông tin khách hàng
                    try (PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE khachhang SET ho_ten = ?, email = ?, sdt = ?, dia_chi = ? WHERE ma_kh = ?
                """)) {
                        stmt.setString(1, hoTen);
                        stmt.setString(2, email);
                        stmt.setString(3, sdt);
                        stmt.setString(4, diaChi);
                        stmt.setInt(5, kh.getMaKh());
                        stmt.executeUpdate();
                    }

                    // Lấy id_taikhoan
                    int idTaiKhoan = -1;
                    try (PreparedStatement stmt = conn.prepareStatement("""
                    SELECT id_taikhoan FROM khachhang WHERE ma_kh = ?
                """)) {
                        stmt.setInt(1, kh.getMaKh());
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) idTaiKhoan = rs.getInt("id_taikhoan");
                    }

                    // Cập nhật trạng thái tài khoản
                    if (idTaiKhoan != -1) {
                        try (PreparedStatement stmt = conn.prepareStatement("""
                        UPDATE taikhoan SET trang_thai = ? WHERE id = ?
                    """)) {
                            stmt.setBoolean(1, trangThaiBool);
                            stmt.setInt(2, idTaiKhoan);
                            stmt.executeUpdate();
                        }
                    }

                    conn.commit();

                    showAlert("Thành công", "Thông tin khách hàng đã được cập nhật.");

                    // Reload danh sách
                    loadDanhSachKhachHang("");

                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể cập nhật khách hàng.\n" + e.getMessage());
                }
            }
            return null;
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

                    int idTaiKhoan = -1;
                    try (PreparedStatement findStmt = conn.prepareStatement(
                            "SELECT id_taikhoan FROM khachhang WHERE ma_kh = ?")) {
                        findStmt.setInt(1, kh.getMaKh());
                        ResultSet rs = findStmt.executeQuery();
                        if (rs.next()) {
                            idTaiKhoan = rs.getInt("id_taikhoan");
                        }
                    }

                    try (PreparedStatement deleteKH = conn.prepareStatement(
                            "DELETE FROM khachhang WHERE ma_kh = ?")) {
                        deleteKH.setInt(1, kh.getMaKh());
                        deleteKH.executeUpdate();
                    }

                    if (idTaiKhoan != -1) {
                        try (PreparedStatement deleteTK = conn.prepareStatement(
                                "DELETE FROM taikhoan WHERE id = ?")) {
                            deleteTK.setInt(1, idTaiKhoan);
                            deleteTK.executeUpdate();
                        }
                    }

                    conn.commit();
                    danhSachKhachHang.remove(kh);
                    showAlert("Thành công", "Đã xoá khách hàng và tài khoản.");
                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể xoá khách hàng.\n" + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
