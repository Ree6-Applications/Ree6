package de.presti.ree6.utils;

public class Setting {

    private String name;
    private Object value;

    public Setting(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public boolean getBooleanValue() {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String)value);
        }
        return true;
    }

    public String getStringValue() {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return (Boolean)value + "";
        } else if (getName().equalsIgnoreCase("chatprefix")) {
            return "ree!";
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
