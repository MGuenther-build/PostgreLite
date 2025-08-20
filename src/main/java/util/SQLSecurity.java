package util;

import java.util.regex.Pattern;

public class SQLSecurity {

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
        "^CREATE\\s+(TEMP(ORARY)?\\s+|UNLOGGED\\s+)?TABLE(\\s+IF\\s+NOT\\s+EXISTS)?\\s+\"?[\\w]+\"?\\s*\\(.*\\)$",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ALTER_ADD_COLUMN_PATTERN = Pattern.compile(
        "^ALTER\\s+TABLE\\s+\"?[\\w\\.]+\"?\\s+ADD\\s+COLUMN\\s+\"?[\\w]+\"?\\s+.+$",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public static String harmonizerSQL(String sql) {
        if (sql == null)
        	return "";

        // entfernt Kommentare und Whitespaces
        String noLineComments = sql.replaceAll("--.*?(\r?\n|$)", " ");
        String noBlockComments = noLineComments.replaceAll("/\\*.*?\\*/", " ");
        String flattened = noBlockComments.replaceAll("[\\r\\n\\t]", " ");
        String cleaned = flattened.replaceAll("\\s{2,}", " ").trim();
        return cleaned;
    }

    public static boolean isAllowedExpertMode(String sql) {
        if (sql == null || sql.trim().isEmpty())
        	return false;

        String cleaned = harmonizerSQL(sql);

        // keine Mehrfach-Statements, BEGIN...END, DO $$...$$
        if (cleaned.matches("(?i).*\\bBEGIN\\b.*") || cleaned.matches("(?is).*DO\\s+\\$\\$.*\\$\\$.*"))
        	return false;

        return CREATE_TABLE_PATTERN.matcher(cleaned).matches()
            || ALTER_ADD_COLUMN_PATTERN.matcher(cleaned).matches();
    }
}
