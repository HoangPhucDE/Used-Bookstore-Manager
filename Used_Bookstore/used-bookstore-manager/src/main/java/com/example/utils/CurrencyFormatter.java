// com/example/utils/CurrencyFormatter.java
package com.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    // Format tiền từ kiểu int
    public static String format(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VNĐ";
    }

    // Format tiền từ kiểu double
    public static String format(double amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VNĐ";
    }
}
