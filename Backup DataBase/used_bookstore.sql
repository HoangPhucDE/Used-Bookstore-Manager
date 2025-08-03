CREATE DATABASE IF NOT EXISTS used_bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Used_Bookstore;

-- 1. Bảng tài khoản
CREATE TABLE taikhoan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    vai_tro ENUM('admin', 'user', 'khach') NOT NULL,
    loai_nguoi_dung ENUM('nhanvien', 'khachhang') NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    trang_thai BOOLEAN NOT NULL DEFAULT TRUE,
    ngay_dang_ky DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng nhân viên
CREATE TABLE nhanvien (
    ma_nv INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    ngay_sinh DATE,
    sdt VARCHAR(20),
    chuc_vu VARCHAR(50),
    trang_thai BOOLEAN DEFAULT TRUE,
    id_taikhoan INT UNIQUE,
    FOREIGN KEY (id_taikhoan) REFERENCES taikhoan(id) ON DELETE SET NULL
);

-- 3. Bảng khách hàng
CREATE TABLE khachhang (
    ma_kh INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    sdt VARCHAR(20),
    dia_chi TEXT,
    id_taikhoan INT UNIQUE,
    FOREIGN KEY (id_taikhoan) REFERENCES taikhoan(id) ON DELETE SET NULL
);

-- 4. Bảng sách
CREATE TABLE sach (
    ma_sach INT AUTO_INCREMENT PRIMARY KEY,
    ten_sach VARCHAR(255) NOT NULL,
    tac_gia VARCHAR(100),
    the_loai VARCHAR(50),
    nxb VARCHAR(100),
    nam_xb YEAR,
    gia_nhap DOUBLE NOT NULL CHECK (gia_nhap >= 0),
    gia_ban DOUBLE NOT NULL CHECK (gia_ban >= 0),
    tinh_trang ENUM('moi', 'cu', 'tot', 'trung_binh', 'kem') DEFAULT 'cu',
    hinh_anh TEXT,
    so_luong_ton INT NOT NULL DEFAULT 0 CHECK (so_luong_ton >= 0),
    danh_gia DOUBLE DEFAULT 0 CHECK (danh_gia BETWEEN 0 AND 5) 
);

-- 5. Bảng giỏ hàng
CREATE TABLE giohang (
    ma_kh INT,
    ma_sach INT,
    so_luong INT NOT NULL CHECK (so_luong > 0),
    ngay_them DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ma_kh, ma_sach),
    FOREIGN KEY (ma_kh) REFERENCES khachhang(ma_kh) ON DELETE CASCADE,
    FOREIGN KEY (ma_sach) REFERENCES sach(ma_sach) ON DELETE CASCADE
);

-- 6. Bảng đơn hàng
CREATE TABLE donhang (
    ma_don INT AUTO_INCREMENT PRIMARY KEY,
    loai_don ENUM('online', 'offline', 'trahang', 'nhap_kho') NOT NULL,
    nguoi_tao_id INT NOT NULL,
    ten_kh VARCHAR(100),
    sdt VARCHAR(20),
    email VARCHAR(100),
    dia_chi TEXT,
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP,
    tong_tien DOUBLE NOT NULL CHECK (tong_tien >= 0),
    trang_thai ENUM('cho_duyet', 'dang_giao', 'hoan_thanh', 'huy') DEFAULT 'cho_duyet',
    FOREIGN KEY (nguoi_tao_id) REFERENCES taikhoan(id) ON DELETE CASCADE
);

-- 7. Bảng chi tiết đơn hàng
CREATE TABLE chitiet_donhang (
    ma_don INT,
    ma_sach INT,
    so_luong INT NOT NULL CHECK (so_luong > 0),
    don_gia DOUBLE NOT NULL CHECK (don_gia >= 0),
    PRIMARY KEY (ma_don, ma_sach),
    FOREIGN KEY (ma_don) REFERENCES donhang(ma_don) ON DELETE CASCADE,
    FOREIGN KEY (ma_sach) REFERENCES sach(ma_sach) ON DELETE CASCADE
);

-- 8. Bảng phiếu nhập (để quản lý nhập kho)
CREATE TABLE phieu_nhap (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ngay_nhap DATE NOT NULL DEFAULT (CURRENT_DATE),
    nguoi_tao_id INT NOT NULL,
    ghi_chu TEXT,
    FOREIGN KEY (nguoi_tao_id) REFERENCES taikhoan(id) ON DELETE CASCADE
);

-- 9. Bàng chi tiết phiếu nhập
CREATE TABLE chitiet_phieunhap (
    ma_phieu INT,
    ma_sach INT,
    so_luong INT NOT NULL CHECK (so_luong > 0),
    don_gia DOUBLE NOT NULL CHECK (don_gia >= 0),
    PRIMARY KEY (ma_phieu, ma_sach),
    FOREIGN KEY (ma_phieu) REFERENCES phieu_nhap(id) ON DELETE CASCADE,
    FOREIGN KEY (ma_sach) REFERENCES sach(ma_sach) ON DELETE CASCADE
);

USE Used_Bookstore;

-- 1. Tài khoản
INSERT INTO taikhoan (username, mat_khau, vai_tro, loai_nguoi_dung, email, trang_thai, ngay_dang_ky) VALUES
('admin_quyen', '123456@Admin', 'admin', 'nhanvien', 'quyen.admin@example.com', TRUE, NOW()),
('leminh_nhanvien', '123456@Nv', 'user', 'nhanvien', 'leminh.tuan@example.com', TRUE, NOW()),
('trananh_1990', '123456@Khach', 'khach', 'khachhang', 'anhdung.tran@gmail.com', TRUE, NOW()),
('phamhoa_admin', 'hoa123@Admin', 'admin', 'nhanvien', 'phamhoa@example.com', TRUE, NOW()),
('phuocnv', 'phuoc123@Nv', 'user', 'nhanvien', 'phuoc.nv@example.com', TRUE, NOW()),
('vananh_khach', 'vananh123@Khach', 'khach', 'khachhang', 'vananh.kh@example.com', TRUE, NOW()),
('tuananh_khach', 'tuananh123@Khach', 'khach', 'khachhang', 'tuananh.kh@example.com', TRUE, NOW()),
('kimanh_staff', 'kim123@Nv', 'user', 'nhanvien', 'kimanh.staff@example.com', TRUE, NOW()),
('luongadmin', 'luong@Admin', 'admin', 'nhanvien', 'luong.admin@example.com', TRUE, NOW()),
('quanguser', 'quang@Nv', 'user', 'nhanvien', 'quang.nv@example.com', TRUE, NOW());

-- 2. Nhân viên
INSERT INTO nhanvien (ho_ten, ngay_sinh, sdt, chuc_vu, trang_thai, id_taikhoan) VALUES
('Nguyễn Thị Quyên', '1985-05-20', '0901123456', 'Quản lý', TRUE, 1),
('Lê Minh Tuấn', '1992-09-12', '0987123456', 'Bán hàng', TRUE, 2),
('Phạm Thị Hoa', '1989-06-20', '0911000011', 'Quản lý kho', TRUE, 4),
('Lê Phước', '1992-02-14', '0911000012', 'Thu ngân', TRUE, 5),
('Kim Ánh', '1991-11-11', '0909222111', 'Nhập liệu', TRUE, 8),
('Lương Văn Bình', '1988-04-03', '0913456789', 'Quản trị hệ thống', TRUE, 9),
('Phan Quang', '1994-08-25', '0989333444', 'Chăm sóc khách hàng', TRUE, 10);

-- 3. Khách hàng
INSERT INTO khachhang (ho_ten, email, sdt, dia_chi, id_taikhoan) VALUES
('Trần Anh Dũng', 'anhdung.tran@gmail.com', '0912121212', '123 Pasteur, Q.3, TP.HCM', 3),
('Nguyễn Vân Anh', 'vananh.kh@example.com', '0911222333', '45 Nguyễn Trãi, Q.5, TP.HCM', 6),
('Đỗ Tuấn Anh', 'tuananh.kh@example.com', '0911444555', '88 Lý Thường Kiệt, Q.10, TP.HCM', 7),
('Lê Thanh Hà', 'thanhha.kh@example.com', '0909888777', '12 Trần Hưng Đạo, Q.1, TP.HCM', NULL),
('Phạm Thị Ngọc', 'ngocpham.kh@example.com', '0909666555', '99 Hai Bà Trưng, Q.3, TP.HCM', NULL),
('Huỳnh Minh Nhật', 'nhat.huynh@example.com', '0909555111', '100 Cách Mạng Tháng 8, Q.10', NULL),
('Trịnh Văn Cường', 'cuongtv.kh@example.com', '0911223344', '22 Hoàng Văn Thụ, Q.Phú Nhuận', NULL);

-- 4. Sách
INSERT INTO sach (
    ten_sach, tac_gia, the_loai, nxb, nam_xb, gia_nhap, gia_ban, tinh_trang, hinh_anh, so_luong_ton, danh_gia
) VALUES
('Dám Nghĩ Lớn', 'David J. Schwartz', 'Phát triển bản thân', 'NXB Trẻ', 2021, 50000, 85000, 'tot', NULL, 10, 4.6),
('Nhà Giả Kim', 'Paulo Coelho', 'Tiểu thuyết', 'NXB Văn Học', 2019, 60000, 95000, 'cu', NULL, 5, 4.8),
('Lập Trình Python Cơ Bản', 'Nguyễn Văn Long', 'Giáo trình', 'NXB Lao Động', 2022, 80000, 120000, 'moi', NULL, 15, 4.5),
('Tư Duy Nhanh Và Chậm', 'Daniel Kahneman', 'Tâm lý học', 'NXB Thế Giới', 2020, 85000, 135000, 'moi', NULL, 8, 4.7),
('Tuổi Trẻ Đáng Giá Bao Nhiêu', 'Rosie Nguyễn', 'Phát triển bản thân', 'NXB Trẻ', 2019, 50000, 88000, 'cu', NULL, 12, 4.4),
('Muôn Kiếp Nhân Sinh', 'Nguyên Phong', 'Tâm linh', 'NXB Tổng Hợp', 2021, 70000, 120000, 'tot', NULL, 20, 4.9),
('Sherlock Holmes Toàn Tập', 'Arthur Conan Doyle', 'Trinh thám', 'NXB Văn Học', 2022, 90000, 150000, 'moi', NULL, 7, 4.8),
('Lược Sử Thời Gian', 'Stephen Hawking', 'Khoa học', 'NXB Trẻ', 2018, 95000, 160000, 'moi', NULL, 9, 4.6),
('Đắc Nhân Tâm', 'Dale Carnegie', 'Phát triển bản thân', 'NXB Tổng Hợp', 2020, 60000, 100000, 'tot', NULL, 14, 4.9),
('Thiên Tài Bên Trái, Kẻ Điên Bên Phải', 'Dương Chí Thành', 'Tâm lý học', 'NXB Lao Động', 2019, 75000, 110000, 'cu', NULL, 6, 4.7),
('Homo Deus: Lược Sử Tương Lai', 'Yuval Noah Harari', 'Khoa học xã hội', 'NXB Thế Giới', 2021, 95000, 150000, 'moi', NULL, 10, 4.5),
('Start With Why', 'Simon Sinek', 'Kinh doanh', 'NXB Trẻ', 2022, 85000, 140000, 'tot', NULL, 13, 4.6),
('Cà Phê Cùng Tony', 'Tony Buổi Sáng', 'Truyền cảm hứng', 'NXB Trẻ', 2018, 55000, 89000, 'cu', NULL, 11, 4.3),
('Combo Dạy Con Làm Giàu', 'Robert T. Kiyosaki', 'Kinh tế', 'NXB Trẻ', 2017, 130000, 210000, 'cu', NULL, 5, 4.8),
('Hành Trình Về Phương Đông', 'Blair T. Spalding', 'Tâm linh', 'NXB Văn Hóa', 2020, 70000, 115000, 'moi', NULL, 9, 4.7);

-- 5. Giỏ hàng
INSERT INTO giohang (ma_kh, ma_sach, so_luong) VALUES
(1, 1, 1),
(1, 3, 1),
(2, 4, 1),
(2, 5, 2),
(3, 6, 1),
(4, 2, 1),
(5, 8, 1),
(6, 9, 2);

-- 6. Đơn hàng
INSERT INTO donhang (loai_don, nguoi_tao_id, ten_kh, sdt, email, dia_chi, tong_tien, trang_thai) VALUES
('online', 3, 'Trần Anh Dũng', '0912121212', 'anhdung.tran@gmail.com', '123 Pasteur, Q.3, TP.HCM', 205000, 'cho_duyet'),
('online', 6, 'Nguyễn Vân Anh', '0911222333', 'vananh.kh@example.com', '45 Nguyễn Trãi, Q.5, TP.HCM', 223000, 'cho_duyet'),
('online', 7, 'Đỗ Tuấn Anh', '0911444555', 'tuananh.kh@example.com', '88 Lý Thường Kiệt, Q.10, TP.HCM', 120000, 'cho_duyet'),
('offline', 2, 'Nguyễn Vân Anh', '0911222333', 'vananh.kh@example.com', '45 Nguyễn Trãi, Q.5, TP.HCM', 95000, 'hoan_thanh'),
('offline', 1, 'Lê Thanh Hà', '0909888777', 'thanhha.kh@example.com', '12 Trần Hưng Đạo', 85000, 'hoan_thanh'),
('offline', 1, 'Phạm Thị Ngọc', '0909666555', 'ngocpham.kh@example.com', '99 Hai Bà Trưng', 170000, 'hoan_thanh');

-- 7. Chi tiết đơn hàng
INSERT INTO chitiet_donhang (ma_don, ma_sach, so_luong, don_gia) VALUES
(1, 1, 1, 85000),
(1, 3, 1, 120000),
(2, 4, 1, 135000),
(2, 5, 1, 88000),
(3, 6, 1, 120000),
(4, 2, 1, 95000),
(5, 1, 1, 85000),
(6, 3, 1, 120000),
(6, 4, 1, 135000);

-- 8. Phiếu Nhập
INSERT INTO phieu_nhap (id, ngay_nhap, nguoi_tao_id, ghi_chu) VALUES
(1, '2025-07-01', 1, 'Nhập sách phát triển bản thân đầu tháng 7'),
(2, '2025-07-03', 4, 'Nhập sách tiểu thuyết phổ biến'),
(3, '2025-07-05', 1, 'Bổ sung sách giáo trình Python'),
(4, '2025-07-08', 4, 'Nhập sách tâm lý học và kinh tế'),
(5, '2025-07-12', 1, 'Bổ sung sách khoa học và truyền cảm hứng'),
(6, '2025-07-15', 4, 'Nhập sách trinh thám mới'),
(7, '2025-07-20', 1, 'Nhập sách tâm linh và xã hội'),
(8, '2025-07-22', 4, 'Nhập combo sách phát triển cá nhân'),
(9, '2025-07-25', 1, 'Bổ sung các tựa sách bán chạy'),
(10, '2025-07-30', 4, 'Cuối tháng kiểm kê nhập thêm');

-- 9. Chi tiết phiếu nhập 
INSERT INTO chitiet_phieunhap (ma_phieu, ma_sach, so_luong, don_gia) VALUES
(1, 1, 20, 50000),
(1, 5, 15, 50000),
(1, 9, 25, 60000),

(2, 2, 12, 60000),
(2, 10, 8, 75000),

(3, 3, 30, 80000),

(4, 4, 18, 85000),
(4, 14, 10, 130000),

(5, 8, 10, 95000),
(5, 13, 20, 55000),

(6, 7, 15, 90000),

(7, 6, 22, 70000),
(7, 11, 18, 95000),

(8, 12, 14, 85000),
(8, 1, 10, 50000),

(9, 9, 20, 60000),
(9, 3, 10, 80000),

(10, 15, 12, 70000);
