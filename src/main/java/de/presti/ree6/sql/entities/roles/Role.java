package de.presti.ree6.sql.entities.roles;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.data.SQLEntity;

/**
 * Role class to store data about roles.
 */
public class Role extends SQLEntity {

    /**
     * The Name of the Role.
     */
    @Property(name = "gid")
    String guildId;

    /**
     * The ID of the Role.
     */
    @Property(name = "rid")
    String roleId;

    /**
     * Constructor.
     */
    public Role() {
    }

    /**
     * Constructor.
     * @param guildId the GuildID of the Role.
     * @param roleId the ID of the Role.
     */
    public Role(String guildId, String roleId) {
        this.guildId = guildId;
        this.roleId = roleId;
    }

    /**
     * Get the GuildID of the Role.
     * @return {@link String} as GuildID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the ID of the Role.
     * @return {@link String} as ID.
     */
    public String getRoleId() {
        return roleId;
    }
}
