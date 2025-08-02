// com/example/utils/CurrencyFormatter.java
package com.example.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    public static String format(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VND";
    }
}
