package de.presti.ree6.sql.entities.roles;

import jakarta.persistence.*;

/**
 * Role class to store data about roles.
 */
@MappedSuperclass
public class Role {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * The Name of the Role.
     */
    @Column(name = "gid")
    String guildId;

    /**
     * The ID of the Role.
     */
    @Column(name = "rid")
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
