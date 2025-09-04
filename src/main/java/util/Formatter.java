package util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;



/**
 * Formatter: Formatierung der Datentypen
 */
public class Formatter {

    private static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String[] GEB_SPALTEN = {
        "geburtsdatum", "geburtstag", "geburtsjahr", "geb_datum", "geb_tag", "geb_jahr", "geb-datum", "geb-tag", "geb-jahr",
        "birthdate", "birthday", "dob", "date_of_birth", "birth_day", "birth"
    };

    public static String format(Object value, String columnName) {
        if (value == null)
        	return "";

        boolean isBirthColumn = columnName != null &&
        	    Arrays.stream(GEB_SPALTEN)
        	          .map(String::toLowerCase)
        	          .anyMatch(name -> name.equals(columnName.trim().toLowerCase()));

        if (value instanceof String) {
            return ((String) value).trim();
        }

        if (value instanceof Integer || value instanceof Long) {
            return String.valueOf(value);
        }

        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d)) {
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

        if (value instanceof LocalTime) {
            return ((LocalTime) value).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        if (value instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) value;
            if (isBirthColumn || zdt.toLocalTime().equals(LocalTime.MIDNIGHT)) {
                return zdt.toLocalDate().format(LOCAL_DATE_FORMAT);
            }
            return zdt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm '('VV')'"));
        }

        if (value instanceof Duration) {
            Duration d = (Duration) value;
            long days = d.toDays();
            long hours = d.minusDays(days).toHours();
            return days + " Tage " + hours + " Stunden";
        }

        return value.toString();
    }
    
    /** Überladung für Controller, damit für alle anderen Formatierungen
    * weiter setText(Formatter.format(item)) erkannt wird **/
    public static String format(Object value) {
        return format(value, null);
    }
}