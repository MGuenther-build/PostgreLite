package util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;



/**
 * Normalizer: Wandelt rohe SQL-/Excel-Daten in passende Java-Typen um.
 * Ziel: Integer, Long, Double korrekt erkennen und Datumswerte in LocalDateTime konvertieren.
 */
public class Normalizer {

    public static Object normalize(Object value) {
        if (value == null) return null;

        // BigDecimal → Integer/Long/Double
        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            try {
                if (bd.scale() <= 0) { // keine Nachkommastellen
                    if (bd.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0 &&
                        bd.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {
                        return bd.intValueExact();
                    }
                    if (bd.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) >= 0 &&
                        bd.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0) {
                        return bd.longValueExact();
                    }
                }
                return bd.doubleValue(); // Dezimal → Double
            } catch (ArithmeticException e) {
                return bd; // Fallback
            }
        }

        // Double → Integer/Long/Double
        if (value instanceof Double) {
            Double d = (Double) value;
            if (d % 1 == 0 && d <= Integer.MAX_VALUE && d >= Integer.MIN_VALUE) {
                return d.intValue();
            }
            if (d % 1 == 0 && d <= Long.MAX_VALUE && d >= Long.MIN_VALUE) {
                return d.longValue();
            }
            return d;
        }

        // Float → Double
        if (value instanceof Float) {
            Float f = (Float) value;
            if (f % 1 == 0 && f <= Integer.MAX_VALUE && f >= Integer.MIN_VALUE) {
                return f.intValue();
            }
            if (f % 1 == 0 && f <= Long.MAX_VALUE && f >= Long.MIN_VALUE) {
                return f.longValue();
            }
            return f.doubleValue();
        }

        // Date → LocalDateTime
        if (value instanceof Date) {
            Date legacyDate = (Date) value;
            Instant instant = legacyDate.toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }

        // Alles andere unverändert
        return value;
    }
}
