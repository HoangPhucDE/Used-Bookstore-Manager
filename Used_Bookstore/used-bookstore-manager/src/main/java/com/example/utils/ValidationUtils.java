package com.example.utils;

import java.time.LocalDate;

public class ValidationUtils {

    // ✅ Kiểm tra số điện thoại Việt Nam hợp lệ (bắt đầu bằng 0, có 10 chữ số)
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("0\\d{9}");
    }

    // (Tuỳ chọn) Có thể thêm các hàm khác nếu muốn tái sử dụng:
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^\\S+@\\S+\\.\\S+$");
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 4;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    // Kiểm tra ngày sinh hợp lệ: không phải tương lai, không quá cũ
    public static boolean isValidDateOfBirth(LocalDate dob) {
        return dob != null
                && !dob.isAfter(LocalDate.now())                   // Không ở tương lai
                && !dob.isBefore(LocalDate.of(1900, 1, 1));        // Không quá xa
    }
}

