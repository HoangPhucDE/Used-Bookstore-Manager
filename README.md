# 📚 Used Bookstore Manager

Hello! This is a project from **Van Hien University**.

### 👨‍👩‍👧‍👦 Nhóm thực hiện:
- 👑 **Nguyen Hoang Phuc**  (Team Leader)
- 👩‍💻 Tran Thi Van Anh
- 👩‍💻Nguyen Nhat Vi
- 👨‍💻 Nguyen Chi Khanh

---

## 📘 HỆ THỐNG QUẢN LÝ VÀ BÁN SÁCH CŨ

### 🎯 Giới thiệu

Hệ thống ứng dụng được xây dựng nhằm:
- ✅ Quản lý hiệu sách nội bộ (sách, nhân viên, tài khoản, hóa đơn, thống kê)
- ✅ Hỗ trợ khách hàng đặt mua sách cũ thông qua ứng dụng JavaFX

Ứng dụng sử dụng **JavaFX + JDBC + MySQL**, có phân quyền rõ ràng: **Admin**, **Nhân viên**, **Khách hàng**.

---

## ⚙️ Công nghệ sử dụng

| Thành phần         | Công nghệ                    |
|--------------------|------------------------------|
| Giao diện nội bộ   | JavaFX (FXML)                |
| Giao diện khách    | JavaFX App                   |
| Kết nối CSDL       | JDBC                         |
| Cơ sở dữ liệu      | MySQL                        |
| Báo cáo & Export   | Apache POI, iText (tuỳ chọn) |

---

## 👥 Phân quyền người dùng

| Chức năng              | Admin | Nhân viên | Khách hàng |
|------------------------|-------|-----------|------------|
| Đăng nhập              | ✅    | ✅        | ✅         |
| Quản lý sách           | ✅    | ❌        | ❌         |
| Quản lý nhân viên      | ✅    | ❌        | ❌         |
| Mua sách online        | ❌    | ❌        | ✅         |
| Tạo đơn offline        | ✅    | ✅        | ❌         |
| Thống kê doanh thu     | ✅    | ❌        | ❌         |

---

## 🧩 Chức năng chính

### 📗 Quản lý sách
- Thêm / sửa / xóa sách
- Tình trạng sách: mới, cũ, tốt, trung bình
- Tìm kiếm, phân loại

### 👨‍💼 Quản lý nhân viên & tài khoản
- Tạo tài khoản cho nhân viên
- Phân quyền `admin`, `user`, `khach`

### 🛒 Bán hàng
- Tạo đơn hàng tại quầy (offline)
- Đặt hàng online qua giỏ hàng

### 👤 Khách hàng
- Xem sách, tìm kiếm
- Thêm vào giỏ hàng
- Thanh toán và theo dõi đơn hàng

### 📊 Thống kê
- Doanh thu theo thời gian
- Sách bán chạy
- Xuất báo cáo PDF hoặc Excel

---

## 🗃️ Cơ sở dữ liệu chính

| Bảng              | Chức năng                        |
|-------------------|----------------------------------|
| `taikhoan`        | Quản lý đăng nhập, phân quyền    |
| `nhanvien`        | Quản lý nhân viên nội bộ         |
| `khachhang`       | Lưu thông tin người dùng mua     |
| `sach`            | Thông tin sách cũ                |
| `giohang`         | Giỏ hàng của khách               |
| `donhang`         | Đơn hàng chung (online/offline)  |
| `chitiet_donhang` | Chi tiết các sách trong đơn hàng |

---

## 🚀 Cài đặt & chạy ứng dụng

### ✅ Yêu cầu hệ thống
- Java JDK 17 trở lên
- MySQL Server
- IntelliJ IDEA / VSCode
- Scene Builder (tùy chọn cho JavaFX)

---

#### Clone project
git clone https://github.com/HoangPhucDE/Used-Bookstore-Manager.git
