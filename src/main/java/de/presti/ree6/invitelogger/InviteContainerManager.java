package de.presti.ree6.invitelogger;

import de.presti.ree6.main.Main;
import de.presti.ree6.utils.Logger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;

import java.util.ArrayList;
import java.util.List;

public class InviteContainerManager {

    private static final ArrayList<InviteContainer> deletedInvites = new ArrayList<>();

    public static void addInvite(InviteContainer inv, String gid) {
        try {
            Main.sqlWorker.setInvite(gid, inv.getCode(), inv.getCreatorid(), inv.getUses());
        } catch (Exception ex) {
            Logger.log("InviteManager", "Error while Saving Invites: " + ex.getMessage());
        }
    }

    public static void removeInvite(String gid, String creator, String code) {
        Main.sqlWorker.removeInvite(gid, creator, code);
    }

    public static void removeInvite(String gid, String code) {
        Main.sqlWorker.removeInvite(gid, code);
    }

    public static InviteContainer getRightInvite(Guild g) {
        if(getInvites(g.getId()) != null) {
            ArrayList<InviteContainer> cachedInvs = getInvites(g.getId());

            List<Invite> invs = g.retrieveInvites().complete();

            for (Invite inv : invs) {
                for (InviteContainer inv2 : cachedInvs) {
                    if (inv.getInviter().getId().equalsIgnoreCase(inv2.getCreatorid()) && inv.getCode().equalsIgnoreCase(inv2.getCode())) {
                        if (inv.getUses() != inv2.getUses()) {
                            return inv2;
                        }
                    }
                }
            }
        }

        return null;

    }

    public static ArrayList<InviteContainer> getInvites(String gid) {
        return Main.sqlWorker.getInvites(gid);
    }

    public static ArrayList<InviteContainer> getDeletedInvites() {
        return deletedInvites;
    }

}
