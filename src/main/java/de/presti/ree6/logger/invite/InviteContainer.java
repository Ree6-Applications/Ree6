package de.presti.ree6.logger.invite;

/**
 * Classed used to save Data of Invites from the Database.
 */
public class InviteContainer {

    // information about the Invite.
    String creatorId, guildId, code;

    // The use count from our Database.
    int uses;

    /**
     * Constructor for the InviteContainer which saved the Data.
     * @param creatorId the ID of the Creator.
     * @param guildId the ID of the Guild.
     * @param code the Code of the Invite.
     * @param uses the Usage Count of the Invite.
     */
    public InviteContainer(String creatorId, String guildId, String code, int uses) {
        this.creatorId = creatorId;
        this.guildId = guildId;
        this.code = code;
        this.uses = uses;
    }

    /**
     * Get the UserID of the Invite Creator.
     * @return {@link String} as User ID.
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Set the ID of the Creator.
     * @param creatorId the ID of the Creator.
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Get the GuildID of the Guild.
     * @return {@link String} as Guild ID.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Set the ID of the Guild.
     * @param guildId the ID of the Guild.
     */
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    /**
     * Get the Invite Code.
     * @return {@link String} as Invite code.
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

    /**
     * Get the Usage Count of the Invite.
     * @return {@link Integer} as Usage Count.
     */
    public int getUses() {
        return uses;
    }

    /**
     * Set the Usage Count of the Invite.
     * @param uses the Usage Count of the Invite.
     */
    public void setUses(int uses) {
        this.uses = uses;
    }
}

