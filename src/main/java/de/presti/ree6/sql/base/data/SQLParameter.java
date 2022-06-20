package de.presti.ree6.sql.base.data;

public class SQLParameter {

    private String name;
    private Class<?> value;
    private boolean primaryKey = false;

    public SQLParameter(String name, Class<?> value) {
        this.name = name;
        this.value = value;
    }

    public SQLParameter(String name, Class<?> value, boolean primaryKey) {
        this.name = name;
        this.value = value;
        this.primaryKey = primaryKey;
    }


    public String getName() {
        return name;
    }

    public Class<?> getValue() {
        return value;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
