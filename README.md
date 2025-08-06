# 📚 Used Bookstore Manager

Ứng dụng quản lý hiệu sách cũ được phát triển bởi sinh viên **Đại học Văn Hiến**.

---

## 👨‍👩‍👧‍👦 Nhóm thực hiện

- 👑 **Nguyễn Hoàng Phúc** (Team Leader)
- 👩‍💻 Trần Thị Vân Anh
- 👩‍💻 Nguyễn Nhật Vi
- 👨‍💻 Nguyễn Chí Khánh

---

## 🎯 Giới thiệu hệ thống

Ứng dụng hỗ trợ quản lý toàn diện cho cửa hàng bán sách cũ, bao gồm:
- ✅ Quản lý sách, nhân viên, khách hàng
- ✅ Xử lý đơn hàng tại quầy và online
- ✅ Quản lý nhập kho, tồn kho
- ✅ Thống kê và xuất báo cáo doanh thu
- ✅ Phân quyền: `admin`, `user`, `khach`

Công nghệ sử dụng: `JavaFX` + `MySQL` + `JDBC`.

---

## ⚙️ Công nghệ sử dụng

| Thành phần         | Công nghệ            |
|--------------------|----------------------|
| Giao diện nội bộ   | JavaFX (FXML)        |
| Giao diện khách    | JavaFX UI            |
| Kết nối CSDL       | JDBC                 |
| Cơ sở dữ liệu      | MySQL                |
| Export báo cáo     | Apache POI, iText    |

---

## 👥 Phân quyền người dùng

| Chức năng                   | Admin | Nhân viên | Khách hàng |
|-----------------------------|--------|-----------|------------|
| Đăng nhập                   | ✅     | ✅        | ✅         |
| Quản lý sách                | ✅     | ❌        | ❌         |
| Quản lý nhân viên           | ✅     | ❌        | ❌         |
| Quản lý đơn hàng            | ✅     | ✅        | ❌         |
| Mua sách online             | ❌     | ❌        | ✅         |
| Tạo đơn tại quầy (offline)  | ✅     | ✅        | ❌         |
| Quản lý kho (nhập sách)     | ✅     | ❌        | ❌         |
| Xem thống kê doanh thu      | ✅     | ❌        | ❌         |

---

## 🧩 Chức năng chính

### 📗 Quản lý sách
- Thêm, sửa, xoá sách cũ
- Lưu trữ thông tin: giá nhập, giá bán, hình ảnh, tồn kho
- Chọn tình trạng sách: `Mới`, `Tốt`, `Cũ`, `Trung bình`, `Kém`
- Tìm kiếm và phân loại theo thể loại, trạng thái

---

### 👥 Quản lý nhân viên & tài khoản
- Tạo tài khoản và phân quyền
- Gắn tài khoản với thông tin nhân viên
- Duyệt trạng thái tài khoản

---

### 🛒 Bán hàng
- Bán hàng offline tại quầy (admin/nhân viên)
- Mua sách online qua giỏ hàng (khách hàng)
- Xuất hóa đơn PDF

---

### 📦 Quản lý đơn hàng
- Danh sách đơn hàng: online & offline
- Lọc theo trạng thái: `chờ duyệt`, `đang giao`, `hoàn thành`, `hủy`
- Cập nhật trạng thái đơn hàng
- Xem chi tiết từng đơn

---

### 🏬 Quản lý kho (phiếu nhập)
- Nhập sách vào kho qua giao diện
- Tự động cập nhật tồn kho và lưu lịch sử nhập
- Cho phép nhập nhiều sách cùng lúc
- Theo dõi chi tiết các phiếu nhập

---

### 👤 Giao diện khách hàng
- Đăng nhập với vai trò `khách`
- Tìm kiếm và xem chi tiết sách
- Thêm vào giỏ hàng
- Đặt hàng và theo dõi trạng thái đơn

---

### 📊 Thống kê doanh thu
- Thống kê theo ngày
- Sách bán chạy
- Nhân viên bán tốt nhất
- Xuất báo cáo PDF & Excel

---

## 🗃️ Cơ sở dữ liệu chính

| Bảng                  | Mục đích                         |
|-----------------------|----------------------------------|
| `taikhoan`            | Quản lý đăng nhập và phân quyền |
| `nhanvien`            | Nhân sự nội bộ                   |
| `khachhang`           | Người dùng mua sách              |
| `sach`                | Thông tin sách cũ                |
| `giohang`             | Giỏ hàng của khách               |
| `donhang`             | Đơn hàng đã đặt                  |
| `chitiet_donhang`     | Sách trong từng đơn              |
| `phieu_nhap`          | Phiếu nhập sách vào kho          |
| `chitiet_phieunhap`   | Chi tiết từng lần nhập kho       |

---

## 🚀 Hướng dẫn cài đặt & chạy

### ⚠️ Yêu cầu hệ thống
- Java JDK 17+
- MySQL Server
- IntelliJ IDEA / VSCode
- Scene Builder (nếu dùng FXML)

### 📦 Clone repo

```bash
git clone https://github.com/HoangPhucDE/Used-Bookstore-Manager.git
