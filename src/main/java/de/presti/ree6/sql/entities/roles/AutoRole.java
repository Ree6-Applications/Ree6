package de.presti.ree6.sql.entities.roles;

import de.presti.ree6.sql.base.annotations.Table;

/**
 * SQL Entity for the Auto-Roles.
 */
@Table(name = "AutoRoles")
public class AutoRole extends Role {

    /**
     * @inheritDoc
     */
    public AutoRole(String guildId, String roleId) {
        super(guildId, roleId);
    }
}