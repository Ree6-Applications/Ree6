package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLEntity;

/**
 * Invite class to store information about an Invite.
 */
@Table(name = "Invites")
public class Invite extends SQLEntity {

    /**
     * The GuildID of the Invite.
     */
    @Property(name = "gid")
    String guild;

    /**
     * The UserID of the Invite.
     */
    @Property(name = "uid")
    String userId;

    /**
     * The Usages of the Invite.
     */
    @Property(name = "uses", updateQuery = true)
    long uses;

    /**
     * The Code of the Invite.
     */
    @Property(name = "code")
    String code;

    /**
     * Constructor.
     */
    public Invite() {
    }

    /**
     * Constructor for the Invite.
     *
     * @param guild  the GuildID of the Invite.
     * @param userId the UserID of the Invite.
     * @param uses   the Usages of the Invite.
     * @param code   the Code of the Invite.
     */
    public Invite(String guild, String userId, long uses, String code) {
        this.guild = guild;
        this.userId = userId;
        this.uses = uses;
        this.code = code;
    }

    /**
     * Get the GuildID of the Invite.
     *
     * @return {@link String} as GuildID.
     */
    public String getGuild() {
        return guild;
    }

    /**
     * Get the UserID of the Invite.
     *
     * @return {@link String} as UserID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the Usages of the Invite.
     *
     * @return {@link long} as Usages.
     */
    public long getUses() {
        return uses;
    }

    /**
     * Get the Code of the Invite.
     *
     * @return {@link String} as Code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the Code of the Invite.
     * @param code the Code of the Invite.
     */
    public void setCode(String code) {
        this.code = code;
    }
}
