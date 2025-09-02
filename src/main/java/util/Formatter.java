package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;



/**
 * Formatter: Darstellung von Java-Typen in der GUI.
 * Ziel: Integer, Long, Double, Boolean, Date, LocalDate, LocalDateTime korrekt anzeigen.
 */
public class Formatter {

    private static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static String format(Object value) {
        if (value == null)
        	return "";

        if (value instanceof String) {
            return ((String) value).trim();
        }

        if (value instanceof Integer) {
            return String.valueOf(value);
        }

        if (value instanceof Long) {
            return String.valueOf(value);
        }

        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d)) { // ganze Zahl → Integer-Format
                if (d <= Integer.MAX_VALUE && d >= Integer.MIN_VALUE) {
                    return String.valueOf((int) d);
                }
                return String.valueOf((long) d);
            }
            return String.format("%.2f", d);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value) ? "✔" : "✘";
        }

        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(LOCAL_DATE_FORMAT);
        }

        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(LOCAL_DATE_TIME_FORMAT);
        }

        if (value instanceof Date) {
            return new SimpleDateFormat("dd.MM.yyyy").format((Date) value);
        }

        // Fallback
        return value.toString();
    }
}
