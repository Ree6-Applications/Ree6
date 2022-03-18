package de.presti.ree6.logger.invite;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to contain every Invite and manage the Invites in our Database.
 */
public class InviteContainerManager {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private InviteContainerManager() {
        throw new IllegalStateException("Utility class");
    }

    // List of every Invite that has been deleted in the current Session.
    private static final ArrayList<InviteContainer> deletedInvites = new ArrayList<>();

    /**
     * Methode to add or update an Invitation on the Database.
     *
     * @param inviteContainer the {@link InviteContainer} with the data of the Invite.
     * @param guildID         the ID of the Guild.
     */
    public static void addInvite(InviteContainer inviteContainer, String guildID) {
        try {
            Main.getInstance().getSqlConnector().getSqlWorker().setInvite(guildID, inviteContainer.getCode(), inviteContainer.getCreatorId(), inviteContainer.getUses());
        } catch (Exception ex) {
            Main.getInstance().getLogger().error("[InviteManager] Error while Saving Invites: " + ex.getMessage());
        }
    }

    /**
     * Methode to remove an Invite from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param creator the ID of the Invite creator.
     * @param code    the Code of the Invite.
     */
    public static void removeInvite(String guildID, String creator, String code) {
        Main.getInstance().getSqlConnector().getSqlWorker().removeInvite(guildID, creator, code);
    }

    /**
     * Methode to remove an Invite from the Database.
     *
     * @param guildID the ID of the Guild.
     * @param code    the Code of the Invite.
     */
    public static void removeInvite(String guildID, String code) {
        Main.getInstance().getSqlConnector().getSqlWorker().removeInvite(guildID, code);
    }

    /**
     * Get the right {@link InviteContainer}.
     *
     * @param guild the {@link Guild} Entity.
     * @return the {@link InviteContainer} of the Invite.
     */
    public static InviteContainer getRightInvite(Guild guild) {
        if (getInvites(guild.getId()) != null) {
            // Every Invite from our Database.
            ArrayList<InviteContainer> cachedInvites = getInvites(guild.getId());

            // Every Invite from the Guild.
            List<Invite> guildInvites = guild.retrieveInvites().complete();

            // Go through every Invite of the Guild.
            for (Invite inv : guildInvites) {
                // Go through every Invite of the Guild from our Database.
                for (InviteContainer inv2 : cachedInvites) {
                    // Check if the Invites aren't null and if there is one that matches the InviteContainer.
                    if (inv != null && inv.getInviter() != null && inv.getInviter().getId().equalsIgnoreCase(inv2.getCreatorId()) && inv.getCode().equalsIgnoreCase(inv2.getCode()) && inv.getUses() != inv2.getUses()) {
                        // Return the InviteContainer.
                        return inv2;
                    }
                }
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
    public static ArrayList<InviteContainer> getInvites(String guildId) {
        return (ArrayList<InviteContainer>) Main.getInstance().getSqlConnector().getSqlWorker().getInvites(guildId);
    }

    /**
     * Get every deleted Invite of the current Session.
     *
     * @return {@link ArrayList<InviteContainer>} with every Invite deleted in the current Session.
     */
    public static ArrayList<InviteContainer> getDeletedInvites() {
        return deletedInvites;
    }

}
