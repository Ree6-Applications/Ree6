package de.presti.ree6.module.invite;

import de.presti.ree6.sql.entities.Invite;
import lombok.Getter;
import lombok.Setter;

/**
 * Classed used to save Data of Invites from the Database.
 */
public class InviteContainer {

    /**
     * The ID of the Guild.
     */
    @Getter
    @Setter
    long guildId;

    /**
     * The ID of the creator.
     */
    @Setter
    @Getter
    long creatorId;

    /**
     * The Code of the Invite.
     */
    @Getter
    @Setter
    String code;

    /**
     * The use count from our Database.
     */
    @Getter
    @Setter
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
     * Constructor for the InviteContainer which saved the Data.
     * @param invite the {@link Invite} to save.
     */
    public InviteContainer(Invite invite) {
        this(invite.getUserId(), invite.getGuild(), invite.getCode(), invite.getUses(), false);
    }

    /**
     * Constructor for the InviteContainer which saved the Data.
     * @param invite the {@link Invite} to save.
     */
    public InviteContainer(net.dv8tion.jda.api.entities.Invite invite) {
        this(invite.getInviter().getIdLong(), invite.getGuild().getIdLong(), invite.getCode(), invite.getUses(), false);
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

