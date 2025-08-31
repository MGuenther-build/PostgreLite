package util;

public class QuerySecurity {

	private static final String[] FORBIDDEN = {
		    "DELETE", "DROP", "TRUNCATE", "CREATE DATABASE",
		    "CREATE USER", "CREATE ROLE",
		    "ALTER USER", "ALTER ROLE",
		    "GRANT", "REVOKE"
		};

    public static String harmonizeSQL(String sql) {
        if (sql == null)
        	return "";

        String noLineComments = sql.replaceAll("--.*?(\r?\n|$)", " ");
        String noBlockComments = noLineComments.replaceAll("/\\*.*?\\*/", " ");
        String flattened = noBlockComments.replaceAll("[\\r\\n\\t]", " ");
        String cleaned = flattened.replaceAll("\\s{2,}", " ").trim();

        return cleaned;
    }
    
    public static String checkQuery(String sql) {
        if (sql == null || sql.trim().isEmpty())
            return "Kein SQL-Befehl eingegeben.";

        String cleaned = harmonizeSQL(sql).toUpperCase();

        if (sql.trim().endsWith(";"))
            return "Kein Semikolon am Ende erlaubt!";

        if (cleaned.contains(";"))
            return "Mehrfach-Statements sind nicht erlaubt!";

        for (String keyword : FORBIDDEN) {
            if (cleaned.contains(keyword)) {
                return "Befehl '" + keyword + "' ist nur in der AdminZone erlaubt!";
            }
        }

        return null;
    }
    
    public static String checkQuerySafety(String sql, String database) {
        if (sql == null || sql.trim().isEmpty())
            return "Bitte SQL-Befehl eingeben.";

        if (database == null)
            return "Keine Datenbank ausgewählt.";

        String validationMessage = checkQuery(sql);
        if (validationMessage != null)
            return validationMessage;

        return null;
    }
}
