package de.presti.ree6.logger.invite;

/**
 * Classed used to save Data of Invites from the Database.
 */
public class InviteContainer {

    /**
     * The ID of the Guild.
     */
    long guildId;

    /**
     * The ID of the creator.
     */
    long creatorId;

    /**
     * The Code of the Invite.
     */
    String code;

    /**
     * The use count from our Database.
     */
    long uses;

    /**
     * If the Invite is a vanity Invite.
     */
    boolean isVanity;

    /**
     * Constructor for the InviteContainer which saved the Data.
     *
     * @param creatorId the ID of the Creator.
     * @param guildId   the ID of the Guild.
     * @param code      the Code of the Invite.
     * @param uses      the Usage Count of the Invite.
     * @param isVanity  if the Invite is a vanity Invite.
     */
    public InviteContainer(long creatorId, long guildId, String code, long uses, boolean isVanity) {
        this.creatorId = creatorId;
        this.guildId = guildId;
        this.code = code;
        this.uses = uses;
        this.isVanity = isVanity;
    }

    /**
     * Get the UserID of the Invite Creator.
     *
     * @return {@link String} as User ID.
     */
    public long getCreatorId() {
        return creatorId;
    }

    /**
     * Set the ID of the Creator.
     *
     * @param creatorId the ID of the Creator.
     */
    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Get the GuildID of the Guild.
     *
     * @return {@link String} as Guild ID.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Set the ID of the Guild.
     *
     * @param guildId the ID of the Guild.
     */
    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    /**
     * Get the Invite Code.
     *
     * @return {@link String} as Invite code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the Code of the Invite.
     *
     * @param code the Code of the Invite.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the Usage Count of the Invite.
     *
     * @return {@link Long} as Usage Count.
     */
    public long getUses() {
        return uses;
    }

    /**
     * Set the Usage Count of the Invite.
     *
     * @param uses the Usage Count of the Invite.
     */
    public void setUses(long uses) {
        this.uses = uses;
    }

    /**
     * Get if the Invite is a Vanity Invite.
     *
     * @return {@link Boolean} as Vanity Invite.
     */
    public boolean isVanity() {
        return isVanity;
    }

    /**
     * Set if the Invite is a Vanity Invite.
     *
     * @param isVanity if the Invite is a Vanity Invite.
     */
    public void setVanity(boolean isVanity) {
        this.isVanity = isVanity;
    }
}

