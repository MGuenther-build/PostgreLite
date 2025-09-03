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
        FRIENDLY_TYPES.put("CHAR", "STRING");
        FRIENDLY_TYPES.put("VARCHAR", "STRING");
        FRIENDLY_TYPES.put("TEXT", "STRING");

        // Datum / Zeit
        FRIENDLY_TYPES.put("DATE", "DATE");
        FRIENDLY_TYPES.put("TIME", "TIME");
        FRIENDLY_TYPES.put("TIME WITH TIME ZONE", "TIME");
        FRIENDLY_TYPES.put("TIMESTAMP", "DATETIME");
        FRIENDLY_TYPES.put("TIMESTAMP WITH TIME ZONE", "DATETIME");
        FRIENDLY_TYPES.put("INTERVAL", "INTERVAL");

        // Binär / JSON / XML
        FRIENDLY_TYPES.put("BYTEA", "BYTEA");
        FRIENDLY_TYPES.put("JSON", "JSON");
        FRIENDLY_TYPES.put("JSONB", "JSON");
        FRIENDLY_TYPES.put("UUID", "UUID");
        FRIENDLY_TYPES.put("XML", "XML");

        // Netzwerk / Geometrie
        FRIENDLY_TYPES.put("CIDR", "IP");
        FRIENDLY_TYPES.put("INET", "IP");
        FRIENDLY_TYPES.put("MACADDR", "MAC");
        FRIENDLY_TYPES.put("MACADDR8", "MAC");
        FRIENDLY_TYPES.put("POINT", "GEOMETRY");
        FRIENDLY_TYPES.put("POLYGON", "GEOMETRY");

        // Volltext
        FRIENDLY_TYPES.put("TSVECTOR", "FULLTEXT");
        FRIENDLY_TYPES.put("TSQUERY", "FULLTEXT");
    }

    public static String friendlySqlType(String sqlType) {
        return FRIENDLY_TYPES.getOrDefault(sqlType.toUpperCase(), sqlType);
    }
}
