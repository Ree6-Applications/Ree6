package de.presti.ree6.sql.entities.roles;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;

@Table(name = "ChatLevelAutoRoles")
public class ChatAutoRole extends Role {

    /**
     * The needed level for this AutoRole.
     */
    @Property(name = "lvl")
    int level;

    /**
     * Constructor.
     */
    public ChatAutoRole() {
    }

    /**
     * Constructor.
     *
     * @param guildId the GuildID of the Role.
     * @param roleId  the ID of the Role.
     * @param level  the needed level for this AutoRole.
     */
    public ChatAutoRole(String guildId, String roleId, int level) {
        super(guildId, roleId);
        this.level = level;
    }

    /**
     * Get the needed level for this AutoRole.
     *
     * @return the needed level for this AutoRole.
     */
    public int getLevel() {
        return level;
    }
}
