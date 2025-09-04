package util;

import java.util.HashMap;
import java.util.Map;

public class SQLTypeMapper {

    private static final Map<String, String> FRIENDLY_TYPES = new HashMap<>();

    static {
        // Ganzzahlen
        FRIENDLY_TYPES.put("BIGINT", "BIGINT");
        FRIENDLY_TYPES.put("BIGSERIAL", "BIGSERIAL");
        FRIENDLY_TYPES.put("INTEGER", "INTEGER");
        FRIENDLY_TYPES.put("INT4", "INT4");
        FRIENDLY_TYPES.put("SMALLINT", "SMALLINT");
        FRIENDLY_TYPES.put("SERIAL", "SERIAL");

        // Gleitkomma
        FRIENDLY_TYPES.put("REAL", "REAL");
        FRIENDLY_TYPES.put("FLOAT4", "FLOAT");
        FRIENDLY_TYPES.put("DOUBLE PRECISION", "DOUBLE PRECISION");
        FRIENDLY_TYPES.put("FLOAT8", "FLOAT8");
        FRIENDLY_TYPES.put("NUMERIC", "NUMERIC");
        FRIENDLY_TYPES.put("DECIMAL", "DECIMAL");
        FRIENDLY_TYPES.put("MONEY", "MONEY");

        // Boolean
        FRIENDLY_TYPES.put("BOOLEAN", "BOOLEAN");
        FRIENDLY_TYPES.put("BOOL", "BOOLEAN");

        // Zeichenketten
        FRIENDLY_TYPES.put("CHAR", "CHAR");
        FRIENDLY_TYPES.put("VARCHAR", "VARCHAR");
        FRIENDLY_TYPES.put("TEXT", "TEXT");

        // Datum / Zeit
        FRIENDLY_TYPES.put("DATE", "DATE");
        FRIENDLY_TYPES.put("TIME", "TIME");
        FRIENDLY_TYPES.put("TIME WITH TIME ZONE", "TIME");
        FRIENDLY_TYPES.put("TIMETZ", "TIMETZ");
        FRIENDLY_TYPES.put("TIMESTAMP", "DATETIME");
        FRIENDLY_TYPES.put("TIMESTAMP WITH TIME ZONE", "DATETIME TZ");
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
        FRIENDLY_TYPES.put("CIDR", "CIDR");
        FRIENDLY_TYPES.put("INET", "INET");
        FRIENDLY_TYPES.put("MACADDR", "MAC");
        FRIENDLY_TYPES.put("MACADDR8", "MAC8");

        // Geometrie
        FRIENDLY_TYPES.put("POINT", "POINT");
        FRIENDLY_TYPES.put("POLYGON", "POLYGON");
        FRIENDLY_TYPES.put("LINE", "LINE");
        FRIENDLY_TYPES.put("LSEG", "GEOMETRY LSEG");
        FRIENDLY_TYPES.put("BOX", "BOX");
        FRIENDLY_TYPES.put("PATH", "GEO PATH");
        FRIENDLY_TYPES.put("CIRCLE", "CIRCLE");

        // Volltext
        FRIENDLY_TYPES.put("TSVECTOR", "TSVECTOR");
        FRIENDLY_TYPES.put("TSQUERY", "TSQUERY");

        // seltene Typen
        FRIENDLY_TYPES.put("ENUM", "ENUM");
        FRIENDLY_TYPES.put("RANGE", "RANGE");
        FRIENDLY_TYPES.put("INT4RANGE", "INTRANGE");
        FRIENDLY_TYPES.put("INT8RANGE", "INTRANGE");
        FRIENDLY_TYPES.put("NUMRANGE", "NUMRANGE");
        FRIENDLY_TYPES.put("TSRANGE", "TSRANGE");
        FRIENDLY_TYPES.put("TSTZRANGE", "TSTZRANGE");
        FRIENDLY_TYPES.put("DATERANGE", "DATERANGE");
        FRIENDLY_TYPES.put("COMPOSITE", "COMPOSITE");
    }

    
    
    public static String friendlySqlType(String sqlType) {
        if (sqlType == null) {
            return "UNKNOWN";
        }
        
        String upper = sqlType.toUpperCase().trim();

        // Prüfen, ob Parameter enthalten sind
        String baseType = upper.replaceAll("\\(.*\\)", "").trim();
        String friendlyBase = FRIENDLY_TYPES.getOrDefault(baseType, baseType);
        if (upper.contains("(")) {
            if (baseType.equals("VARCHAR") ||
                baseType.equals("CHAR") ||
                baseType.equals("NUMERIC") ||
                baseType.equals("DECIMAL") ||
                baseType.equals("BIT") ||
                baseType.equals("BIT VARYING") ||
                baseType.equals("TIME") ||
                baseType.equals("TIMESTAMP") ||
                baseType.equals("INTERVAL")) 
            {
                String params = upper.substring(upper.indexOf("("));
                return friendlyBase + params;
            }
        }
        return friendlyBase;
    }
}
