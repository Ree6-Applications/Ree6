package de.presti.ree6.utils;

/**
 * File to store Settings information.
 */
public class Setting {

    // Name / Identifier of the Setting.
    private String name;

    // The value of the Setting.
    private Object value;

    /**
     * Constructor for the Setting.
     * @param name the Name / Identifier of the Setting.
     * @param value the Value of the Setting.
     */
    public Setting(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the Value as Boolean.
     * @return the Value as {@link Boolean}
     */
    public boolean getBooleanValue() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return true;
    }

    /**
     * Get the Value as String.
     * @return Value as {@link String}
     */
    public String getStringValue() {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return value + "";
        } else if (getName().equalsIgnoreCase("chatprefix")) {
            return "ree!";
        }
        return "";
    }

    /**
     * The Name / Identifier of the Setting.
     * @return {@link String} which is the Name / Identifier.
     */
    public String getName() {
        return name;
    }

    /**
     * Change the Name / Identifier of the Setting.
     * @param name new Name / Identifier as {@link String}.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the Value as Object.
     * @return Value as {@link Object}
     */
    public Object getValue() {
        return value;
    }

    /**
     * Change the Value Object of the Setting.
     * @param value new Value as {@link Object}
     */
    public void setValue(Object value) {
        this.value = value;
    }
}