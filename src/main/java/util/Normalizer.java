package util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;



/**
 * Normalizer: SQL-/Excel-Daten in passende Java-Typen umwandeln
 */
public class Normalizer {

    public static Object normalize(Object value) {
    	try {
	        if (value == null)
	        	return null;
	
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
	        
	        // TIME
	        if (value instanceof java.sql.Time) {
	            java.sql.Time time = (java.sql.Time) value;
	            return time.toLocalTime();
	        }

	        // TIMESTAMPTZ
	        if (value instanceof OffsetDateTime) {
	            OffsetDateTime odt = (OffsetDateTime) value;
	            return odt.toZonedDateTime();
	        }

	        if (value instanceof ZonedDateTime) {
	            return value;
	        }

	        // INTERVAL
	        if (value instanceof Duration) {
	            return value;
	        }
	
	        // DATE
	        if (value instanceof java.sql.Timestamp) {
	            java.sql.Timestamp ts = (java.sql.Timestamp) value;
	            return ts.toLocalDateTime();
	        }

	        if (value instanceof java.sql.Date) {
	            java.sql.Date sqlDate = (java.sql.Date) value;
	            return sqlDate.toLocalDate().atStartOfDay(ZoneId.systemDefault());
	        }

	        if (value instanceof Date) {
	            Date utilDate = (Date) value;
	            try {
	                Instant instant = utilDate.toInstant();
	                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	            } catch (UnsupportedOperationException e) {
	                return utilDate.toString(); // Fallback
	            }
	        }
	        return value;
	    } catch (Exception e) {
	    	return ("[Datentyp nicht verwertbar: " + value + "]");
	    }   	
    }
}
