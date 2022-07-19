package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLEntity;

/**
 * File to store Settings information.
 */
@Table(name = "Settings")
public class Setting extends SQLEntity {

    /**
     * The ID of the Guild.
     */
    @Property(name = "gid")
    private String guildId;

    /**
     * Name / Identifier of the Setting.
     */
    @Property(name = "name")
    private String name;

    /**
     * The value of the Setting.
     */
    @Property(name = "value")
    private Object value;

    /**
     * Constructor for the Setting.
     *
     * @param guildId the GuildID of the Setting.
     * @param name    the Name / Identifier of the Setting.
     * @param value   the Value of the Setting.
     */
    public Setting(String guildId, String name, Object value) {
        this.guildId = guildId;
        this.name = name;
        this.value = value;
    }

    /**
     * Get the Value as Boolean.
     *
     * @return the Value as {@link Boolean}
     */
    public boolean getBooleanValue() {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        } else if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return true;
    }

    /**
     * Get the Value as String.
     *
     * @return Value as {@link String}
     */
    public String getStringValue() {
        if (value instanceof String stringValue) {
            return stringValue;
        } else if (getName().equalsIgnoreCase("chatprefix")) {
            return "ree!";
        } else if (value instanceof Boolean booleanValue) {
            return booleanValue + "";
        }
        return "";
    }

    /**
     * Get the Guild.
     *
     * @return the Guild ID.
     */
    public String getGuild() {
        return guildId;
    }

    /**
     * The Name / Identifier of the Setting.
     *
     * @return {@link String} which is the Name / Identifier.
     */
    public String getName() {
        return name;
    }

    /**
     * Change the Name / Identifier of the Setting.
     *
     * @param name new Name / Identifier as {@link String}.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the Value as Object.
     *
     * @return Value as {@link Object}
     */
    public Object getValue() {
        return value;
    }

    /**
     * Change the Value Object of the Setting.
     *
     * @param value new Value as {@link Object}
     */
    public void setValue(Object value) {
        this.value = value;
    }
}