package de.presti.ree6.module.invite;

import de.presti.ree6.module.IManager;
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
import java.util.Map;
import java.util.Objects;

/**
 * Utility class to contain every Invite and manage the Invites in our Database.
 */
@Slf4j
public class InviteContainerManager implements IManager<InviteContainer> {

    /**
     * Constructor.
     */
    public InviteContainerManager() {
        load();
    }

    private final ArrayList<InviteContainer> invites = new ArrayList<>();

    /**
     * Get every InviteContainer of a Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link ArrayList<InviteContainer>} with every Invite saved in our Database.
     */
    public List<InviteContainer> getInvites(long guildId) {
        return invites.stream().filter(x -> x.guildId == guildId).toList();
    }

    @Override
    public void load() {
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new de.presti.ree6.sql.entities.Invite(), "FROM Invite", null)
                .subscribe(invites1 -> replace(invites1.stream().map(InviteContainer::new).toList()));
    }

    /**
     * Remove all the cache Information about a Guild and refresh it from the Database.
     *
     * @param guildId the ID of the Guild.
     */
    public void refreshGuild(long guildId) {
        invites.removeIf(x -> x.guildId == guildId);
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new de.presti.ree6.sql.entities.Invite(), "FROM Invite WHERE guildAndCode.guildId = :gid", Map.of("gid", guildId))
                .subscribe(invites1 -> replace(invites1.stream().map(InviteContainer::new).toList()));
    }

    /**
     * Methode to replace the Invite of a Guild.
     *
     * @param invite the {@link Invite} to replace.
     */
    public void replaceInvite(Invite invite) {
        if (invite.getGuild() == null) return;
        remove(invite.getGuild().getIdLong(), invite.getCode());
        add(new InviteContainer(invite));
    }

    /**
     * Methode to add or update an Invitation on the Database.
     *
     * @param inviteContainer the {@link InviteContainer} with the data of the Invite.
     */
    @Override
    public void add(InviteContainer inviteContainer) {
        getList().removeIf(inviteContainer1 -> inviteContainer1.getGuildId() == inviteContainer.getGuildId() &&
                inviteContainer1.getCreatorId() == inviteContainer.getCreatorId() &&
                inviteContainer1.getCode().equals(inviteContainer.getCode()));

        getList().add(inviteContainer);
        try {
            SQLSession.getSqlConnector().getSqlWorker().updateEntity(new de.presti.ree6.sql.entities.Invite(inviteContainer.getGuildId(), inviteContainer.getCreatorId(), inviteContainer.getUses(), inviteContainer.getCode())).block();
        } catch (Exception ex) {
            log.error("[InviteManager] Error while Saving Invites: " + ex.getMessage());
        }
    }

    /**
     * Methode to remove an Invitation from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param creator the ID of the Invite creator.
     * @param code    the Code of the Invite.
     */
    public void remove(long guildID, long creator, String code) {
        getList().removeIf(x -> x.getGuildId() == guildID && x.getCreatorId() == creator && x.getCode().equals(code));
        SQLSession.getSqlConnector().getSqlWorker().removeInvite(guildID, creator, code);
    }

    /**
     * Methode to remove an Invitation from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param code    the Code of the Invite.
     */
    public void remove(long guildID, String code) {
        getList().removeIf(x -> x.getGuildId() == guildID && x.getCode().equals(code));
        SQLSession.getSqlConnector().getSqlWorker().removeInvite(guildID, code);
    }

    @Override
    public void remove(InviteContainer inviteContainer) {
        getList().remove(inviteContainer);
        SQLSession.getSqlConnector().getSqlWorker().removeInvite(inviteContainer.getGuildId(), inviteContainer.getCode());
    }

    @Override
    public InviteContainer get(String inviteCode) {
        return IManager.super.get(inviteCode);
    }

    @Override
    public List<InviteContainer> getList() {
        return invites;
    }

    /**
     * Convert a {@link VanityInvite} to an {@link Invite}
     *
     * @param guild the {@link Guild} the {@link VanityInvite} is from.
     * @return the {@link Invite}
     */
    public static Invite convertVanityInvite(Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER) &&
                guild.getVanityCode() != null) {
            try {
                VanityInvite vanityInvite = guild.retrieveVanityInvite().complete();
                return new InviteImpl(null, vanityInvite.getCode(), true, Objects.requireNonNullElse(guild.getOwner(), guild.getSelfMember()).getUser(), 0, -1242525,
                        true, OffsetDateTime.now(), vanityInvite.getUses(), null, new InviteImpl.GuildImpl(guild), null, null, Invite.InviteType.UNKNOWN);
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
    public InviteContainer getRightInvite(Guild guild) {
        // Every Invite from the Guild.
        ArrayList<Invite> guildInvites = new ArrayList<>(guild.retrieveInvites().complete());

        // Load the Vanity URL into the list so that we can track it as well.
        Invite vanityInvite = convertVanityInvite(guild);
        if (vanityInvite != null) guildInvites.add(vanityInvite);

        // Go through every Invite of the Guild.
        for (Invite inv : guildInvites) {
            boolean foundOne = false;

            // Don't even continue with the Loop if the Inviter is null.
            if (inv.getInviter() == null) {
                continue;
            }

            // Go through every Invite of the Guild from our Database.
            for (InviteContainer databaseInvite : getInvites(guild.getIdLong())) {

                // Check if its correct invite.
                if (inv.getCode().equalsIgnoreCase(databaseInvite.getCode())) {
                    foundOne = true;

                    // Check if the Creator of the Invite isn't the same as in our Database.
                    if (inv.getInviter().getIdLong() != databaseInvite.getCreatorId()) {

                        // Check if its Vanity Invite.
                        if (vanityInvite != null && inv.getCode().equalsIgnoreCase(vanityInvite.getCode())) {
                            // Correct the information.
                            databaseInvite.setVanity(true);
                            databaseInvite.setGuildId(guild.getIdLong());
                            if (databaseInvite.getCreatorId() == 0) {
                                databaseInvite.setCreatorId(guild.getOwnerIdLong());
                            }
                        } else {
                            // This should never be reached so, log it.
                            log.warn("Detected a very weird Invite? Owner does not match database entry! Guild: " + guild.getName() + " (" + guild.getId() + ") Invite: " + inv.getInviter().getIdLong() + " Database: " + databaseInvite.getCreatorId());
                            break;
                        }
                    }

                    // Check if the Invite from Discord exactly one more usage than in our Database.
                    // If so, we most likely got the correct one.
                    // Big Issue, if many users join at the same time the value updates quicker than Ree6 can react.
                    // So -1 would not work at all.
                    if (inv.getUses() - 1 == databaseInvite.getUses()) {
                        databaseInvite.setVanity(vanityInvite != null && databaseInvite.getCode().equalsIgnoreCase(vanityInvite.getCode()));
                        return databaseInvite;
                    }
                }
            }

            // If we found one but didn't return it, replace it.
            if (foundOne) {
                replaceInvite(inv);
            } else {
                // If we didn't find one, add it.
                add(new InviteContainer(inv.getInviter().getIdLong(), guild.getIdLong(), inv.getCode(), inv.getUses(), inv.getMaxUses() == -1242525));
            }
        }

        return null;
    }

}
