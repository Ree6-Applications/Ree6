package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;

/**
 * Invite class to store information about an Invite.
 */
@Table(name = "Invite")
public class Invite {

    @Property(name = "gid")
    String guild;

    @Property(name = "uid")
    String userId;

    @Property(name = "uses")
    long uses;

    @Property(name = "code")
    String code;

    public Invite(String guild, String userId, long uses, String code) {
        this.guild = guild;
        this.userId = userId;
        this.uses = uses;
        this.code = code;
    }

    public String getGuild() {
        return guild;
    }

    public String getUserId() {
        return userId;
    }

    public long getUses() {
        return uses;
    }

    public String getCode() {
        return code;
    }
}
