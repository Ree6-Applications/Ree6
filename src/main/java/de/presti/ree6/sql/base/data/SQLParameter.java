package de.presti.ree6.sql.base.data;

/**
 * This class is used to represent a SQL Parameter.
 */
public class SQLParameter {

    /**
     * The name of the parameter.
     */
    private String name;

    /**
     * The dataTyp of the parameter.
     */
    private Class<?> value;

    /**
     * If the parameter is a primary key or not.
     */
    private boolean primaryKey = false;

    /**
     * Constructor.
     *
     * @param name The name of the parameter.
     * @param value The dataType of the parameter.
     */
    public SQLParameter(String name, Class<?> value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param name The name of the parameter.
     * @param value The dataType of the parameter.
     * @param primaryKey If the parameter is a primary key or not.
     */
    public SQLParameter(String name, Class<?> value, boolean primaryKey) {
        this.name = name;
        this.value = value;
        this.primaryKey = primaryKey;
    }


    /**
     * Get the name of the parameter.
     *
     * @return The name of the parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the dataType of the parameter.
     *
     * @return The dataType of the parameter.
     */
    public Class<?> getValue() {
        return value;
    }

    /**
     * Get if the parameter is a primary key or not.
     *
     * @return If the parameter is a primary key or not.
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
