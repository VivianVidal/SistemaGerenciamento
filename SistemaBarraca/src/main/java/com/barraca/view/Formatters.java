
package com.barraca.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatters {
    private static final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Formatters() {}

    public static String money(BigDecimal v) {
        if (v == null) return currency.format(0);
        return currency.format(v);
    }

    public static String date(LocalDate d) {
        if (d == null) return "";
        return d.format(date);
    }

    public static LocalDate parseDate(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        return LocalDate.parse(s, date);
    }
}
