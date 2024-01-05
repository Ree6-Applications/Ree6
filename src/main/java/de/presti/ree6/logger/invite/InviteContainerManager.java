package de.presti.ree6.logger.invite;

import de.presti.ree6.sql.SQLSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.VanityInvite;
import net.dv8tion.jda.internal.entities.InviteImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class to contain every Invite and manage the Invites in our Database.
 */
@Slf4j
public class InviteContainerManager {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private InviteContainerManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * List of every Invite that has been deleted in the current Session.
     */
    private static final ArrayList<InviteContainer> deletedInvites = new ArrayList<>();

    /**
     * Methode to add or update an Invitation on the Database.
     *
     * @param inviteContainer the {@link InviteContainer} with the data of the Invite.
     */
    public static void addInvite(InviteContainer inviteContainer) {
        try {
            SQLSession.getSqlConnector().getSqlWorker().setInvite(inviteContainer.getGuildId(), inviteContainer.getCreatorId(), inviteContainer.getCode(), inviteContainer.getUses());
        } catch (Exception ex) {
            log.error("[InviteManager] Error while Saving Invites: " + ex.getMessage());
        }
    }

    /**
     * Methode to remove an Invite from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param creator the ID of the Invite creator.
     * @param code    the Code of the Invite.
     */
    public static void removeInvite(long guildID, long creator, String code) {
        SQLSession.getSqlConnector().getSqlWorker().removeInvite(guildID, creator, code);
    }

    /**
     * Methode to remove an Invite from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param code    the Code of the Invite.
     */
    public static void removeInvite(long guildID, String code) {
        SQLSession.getSqlConnector().getSqlWorker().removeInvite(guildID, code);
    }

    /**
     * Convert a {@link VanityInvite} to an {@link Invite}
     * @param guild the {@link Guild} the {@link VanityInvite} is from.
     * @return the {@link Invite}
     */
    public static Invite convertVanityInvite(Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER) &&
                guild.getVanityCode() != null) {
            try {
                VanityInvite vanityInvite = guild.retrieveVanityInvite().complete();
                return new InviteImpl(null, vanityInvite.getCode(), true, Objects.requireNonNullElse(guild.getOwner(), guild.getSelfMember()).getUser(), 0, -1242525,
                        true, OffsetDateTime.now(), vanityInvite.getUses(), null,  null, null, null, Invite.InviteType.UNKNOWN);
            } catch (Exception ex) {
                log.error("[InviteManager] Error while retrieving Vanity Invite: " + ex.getMessage());
            }
        }

        return null;
    }

    /**
     * Get the right {@link InviteContainer}.
     *
     * @param guild the {@link Guild} Entity.
     * @return the {@link InviteContainer} of the Invite.
     */
    public static InviteContainer getRightInvite(Guild guild) {
        // Every Invite from our Database.
        ArrayList<InviteContainer> cachedInvites = getInvites(guild.getIdLong());

        // Every Invite from the Guild.
        ArrayList<Invite> guildInvites = new ArrayList<>(guild.retrieveInvites().complete());

        Invite vanityInvite = convertVanityInvite(guild);
        if (vanityInvite != null) guildInvites.add(vanityInvite);

        // Go through every Invite of the Guild.
        for (Invite inv : guildInvites) {

            boolean foundOne = false;
            // Go through every Invite of the Guild from our Database.
            for (InviteContainer inv2 : cachedInvites) {
                if (!foundOne && inv.getCode().equalsIgnoreCase(inv2.getCode())) {
                    foundOne = true;
                }

                if (inv.getInviter() == null ||
                        !inv.getCode().equalsIgnoreCase(inv2.getCode()) ||
                        inv.getInviter().getIdLong() != inv2.getCreatorId()) continue;

                if (inv.getUses() + 1 == inv2.getUses()) {
                    inv2.setVanity(inv.getMaxAge() == -1242525);
                    if (inv2.isVanity()) {
                        inv2.setGuildId(guild.getIdLong());
                        if (inv2.getCreatorId() == 0) {
                            inv2.setCreatorId(guild.getOwnerIdLong());
                        }
                    }
                    return inv2;
                }
            }

            if (!foundOne && inv != null && inv.getInviter() != null) {
                InviteContainerManager.addInvite(new InviteContainer(inv.getInviter().getIdLong(), guild.getIdLong(), inv.getCode(), inv.getUses(), inv.getMaxUses() == -1242525));
            }
        }

        return null;
    }

    /**
     * Get every InviteContainer of a Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<InviteContainer>} with every Invite saved in our Database.
     */
    public static ArrayList<InviteContainer> getInvites(long guildId) {
        ArrayList<InviteContainer> containerList = new ArrayList<>();

        List<de.presti.ree6.sql.entities.Invite> inviteList = SQLSession.getSqlConnector().getSqlWorker().getInvites(guildId);
        inviteList.forEach(invite -> containerList.add(new InviteContainer(invite.getUserId(), invite.getGuild(), invite.getCode(), invite.getUses(),
                        false)));

        return containerList;
    }

}
