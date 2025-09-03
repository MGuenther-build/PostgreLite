package service;

import java.util.List;
import java.util.Map;


/**
 * QueryResult: Korrekte Darstellung der SQL-Typen wie VARCHAR usw statt alles zu Integers,
 * Strings usw. zu machen.
 */
public class QueryResult {
    private final List<Map<String, Object>> rows;
    private final List<String> columnNames;   // Spaltennamen
    private final List<String> sqlTypes;      // SQL-Typen z.B. VARCHAR, INTEGER

    public QueryResult(List<Map<String, Object>> rows, List<String> columnNames, List<String> sqlTypes) {
        this.rows = rows;
        this.columnNames = columnNames;
        this.sqlTypes = sqlTypes;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getSqlTypes() {
        return sqlTypes;
    }
}
