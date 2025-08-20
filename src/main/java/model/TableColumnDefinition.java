package model;

import javafx.beans.property.*;
import service.Table;

public class TableColumnDefinition {
    private final StringProperty columnName = new SimpleStringProperty();
    private final StringProperty baseType = new SimpleStringProperty();
    private final StringProperty typeParameter = new SimpleStringProperty();
    private final BooleanProperty notNull = new SimpleBooleanProperty();
    private final BooleanProperty primaryKey = new SimpleBooleanProperty();
    private final BooleanProperty unique = new SimpleBooleanProperty();
    private final StringProperty defaultValue = new SimpleStringProperty();
    private final StringProperty checkConstraint = new SimpleStringProperty();
    private final StringProperty options = new SimpleStringProperty();

    public TableColumnDefinition(String columnName, String baseType, String typeParameter,
                                 boolean notNull, boolean primaryKey, boolean unique,
                                 String defaultValue, String checkConstraint, String options) {
        this.columnName.set(columnName);
        this.baseType.set(baseType);
        this.typeParameter.set(typeParameter);
        this.notNull.set(notNull);
        this.primaryKey.set(primaryKey);
        this.unique.set(unique);
        this.defaultValue.set(defaultValue);
        this.checkConstraint.set(checkConstraint);
        this.options.set(options);
    }

    // Getter für TableView-Bindung
    public StringProperty columnNameProperty() { return columnName; }
    public StringProperty baseTypeProperty() { return baseType; }
    public StringProperty typeParameterProperty() { return typeParameter; }
    public BooleanProperty notNullProperty() { return notNull; }
    public BooleanProperty primaryKeyProperty() { return primaryKey; }
    public BooleanProperty uniqueProperty() { return unique; }
    public StringProperty defaultValueProperty() { return defaultValue; }
    public StringProperty checkConstraintProperty() { return checkConstraint; }
    public StringProperty optionsProperty() { return options; }

    // Getter für Logik
    public String getColumnName() { return columnName.get(); }
    public String getBaseType() { return baseType.get(); }
    public String getTypeParameter() { return typeParameter.get(); }
    public boolean isNotNull() { return notNull.get(); }
    public boolean isPrimaryKey() { return primaryKey.get(); }
    public boolean isUnique() { return unique.get(); }
    public String getDefaultValue() { return defaultValue.get(); }
    public String getCheckConstraint() { return checkConstraint.get(); }
    public String getOptions() { return options.get(); }

    public String getDataType() {
        if (getTypeParameter() != null && !getTypeParameter().isEmpty()) {
            return getBaseType() + "(" + getTypeParameter() + ")";
        }
        return getBaseType();
    }

    public String toSqlDefinition() {
        StringBuilder sb = new StringBuilder();

        sb.append(getBaseType().toUpperCase());
        if (Table.needsParameter(getBaseType()) && getTypeParameter() != null && !getTypeParameter().isEmpty()) {
            sb.append("(").append(getTypeParameter()).append(")");
        }

        if (isNotNull()) sb.append(" NOT NULL");
        if (isPrimaryKey()) sb.append(" PRIMARY KEY");
        if (isUnique()) sb.append(" UNIQUE");

        if (getDefaultValue() != null && !getDefaultValue().isEmpty()) {
            String safeDefault = getDefaultValue().replace("'", "''");
            sb.append(" DEFAULT '").append(safeDefault).append("'");
        }

        if (getCheckConstraint() != null && !getCheckConstraint().isEmpty()) {
            sb.append(" CHECK (").append(getCheckConstraint()).append(")");
        }

        if (getOptions() != null && !getOptions().isEmpty()) {
            sb.append(" ").append(getOptions());
        }

        return sb.toString();
    }
}
