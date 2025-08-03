package com.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final NumberFormat formatter =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    // Format tiền từ int
    public static String format(int amount) {
        return formatter.format(amount) + " VNĐ";
    }

    // Format tiền từ double
    public static String format(double amount) {
        return formatter.format(amount) + " VNĐ";
    }
}
