package util;

import java.util.HashMap;
import java.util.Map;

public class SQLTypeMapper {

    private static final Map<String, String> FRIENDLY_TYPES = new HashMap<>();

    static {
        // Ganzzahlen
        FRIENDLY_TYPES.put("BIGINT", "LONG");
        FRIENDLY_TYPES.put("BIGSERIAL", "LONG");
        FRIENDLY_TYPES.put("INTEGER", "INTEGER");
        FRIENDLY_TYPES.put("INT4", "INTEGER");
        FRIENDLY_TYPES.put("SMALLINT", "INTEGER");
        FRIENDLY_TYPES.put("SERIAL", "INTEGER");

        // Gleitkomma
        FRIENDLY_TYPES.put("REAL", "FLOAT");
        FRIENDLY_TYPES.put("FLOAT4", "FLOAT");
        FRIENDLY_TYPES.put("DOUBLE PRECISION", "DOUBLE");
        FRIENDLY_TYPES.put("FLOAT8", "DOUBLE");
        FRIENDLY_TYPES.put("NUMERIC", "DECIMAL");
        FRIENDLY_TYPES.put("DECIMAL", "DECIMAL");
        FRIENDLY_TYPES.put("MONEY", "MONEY");

        // Boolean
        FRIENDLY_TYPES.put("BOOLEAN", "BOOLEAN");
        FRIENDLY_TYPES.put("BOOL", "BOOLEAN");

        // Zeichenketten
        FRIENDLY_TYPES.put("CHAR", "TEXT");
        FRIENDLY_TYPES.put("VARCHAR", "TEXT");
        FRIENDLY_TYPES.put("TEXT", "TEXT");

        // Datum / Zeit
        FRIENDLY_TYPES.put("DATE", "DATE");
        FRIENDLY_TYPES.put("TIME", "TIME");
        FRIENDLY_TYPES.put("TIME WITH TIME ZONE", "TIME");
        FRIENDLY_TYPES.put("TIMETZ", "TIME");
        FRIENDLY_TYPES.put("TIMESTAMP", "DATETIME");
        FRIENDLY_TYPES.put("TIMESTAMP WITH TIME ZONE", "DATETIME");
        FRIENDLY_TYPES.put("TIMESTAMPTZ", "DATETIME");
        FRIENDLY_TYPES.put("INTERVAL", "INTERVAL");

        // Binär / JSON / XML
        FRIENDLY_TYPES.put("BYTEA", "BYTEA");
        FRIENDLY_TYPES.put("JSON", "JSON");
        FRIENDLY_TYPES.put("JSONB", "JSON");
        FRIENDLY_TYPES.put("UUID", "UUID");
        FRIENDLY_TYPES.put("XML", "XML");
        FRIENDLY_TYPES.put("HSTORE", "KEY/VALUE");

        // Netzwerk
        FRIENDLY_TYPES.put("CIDR", "IP");
        FRIENDLY_TYPES.put("INET", "IP");
        FRIENDLY_TYPES.put("MACADDR", "MAC");
        FRIENDLY_TYPES.put("MACADDR8", "MAC");

        // Geometrie
        FRIENDLY_TYPES.put("POINT", "GEOMETRY");
        FRIENDLY_TYPES.put("POLYGON", "GEOMETRY");
        FRIENDLY_TYPES.put("LINE", "GEOMETRY");
        FRIENDLY_TYPES.put("LSEG", "GEOMETRY");
        FRIENDLY_TYPES.put("BOX", "GEOMETRY");
        FRIENDLY_TYPES.put("PATH", "GEOMETRY");
        FRIENDLY_TYPES.put("CIRCLE", "GEOMETRY");

        // Volltext
        FRIENDLY_TYPES.put("TSVECTOR", "FULLTEXT");
        FRIENDLY_TYPES.put("TSQUERY", "FULLTEXT");

        // seltene Typen
        FRIENDLY_TYPES.put("ENUM", "ENUM");
        FRIENDLY_TYPES.put("RANGE", "RANGE");
        FRIENDLY_TYPES.put("INT4RANGE", "RANGE");
        FRIENDLY_TYPES.put("INT8RANGE", "RANGE");
        FRIENDLY_TYPES.put("NUMRANGE", "RANGE");
        FRIENDLY_TYPES.put("TSRANGE", "RANGE");
        FRIENDLY_TYPES.put("TSTZRANGE", "RANGE");
        FRIENDLY_TYPES.put("DATERANGE", "RANGE");
        FRIENDLY_TYPES.put("COMPOSITE", "COMPOSITE");
    }

    public static String friendlySqlType(String sqlType) {
        if (sqlType == null)
        	return "UNKNOWN";
        String upper = sqlType.toUpperCase();
        return FRIENDLY_TYPES.getOrDefault(upper, upper); // Fallback
    }
}
