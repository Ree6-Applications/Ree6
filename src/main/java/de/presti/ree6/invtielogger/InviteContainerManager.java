package de.presti.ree6.invtielogger;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InviteContainerManager {

    private static HashMap<String, ArrayList<InviteContainer>> invites = new HashMap<>();
    private static ArrayList<InviteContainer> deletedInvites = new ArrayList<>();

    public static void addInvite(InviteContainer inv, String gid) {
        ArrayList<InviteContainer> invs = new ArrayList<>();
        if(invites.containsKey(gid)) {
            invs = invites.get(gid);

            if (!invs.contains(inv)) {
                invs.add(inv);
            }
            invites.remove(gid);
        } else {
            invs.add(inv);
        }

        invites.put(gid, invs);
    }

    public static void removeInvite(String gid, String code) {
        ArrayList<InviteContainer> invs = new ArrayList<>();

        if(invites.containsKey(gid)) {
            for(InviteContainer inv : getInvites(gid)) {
                if(!inv.code.equalsIgnoreCase(code)) {
                    invs.add(inv);
                } else {
                    deletedInvites.add(inv);
                }
            }
            invites.remove(gid);
        }

        invites.put(gid, invs);
    }

    public static InviteContainer getRightInvite(Guild g) {
        if(invites.containsKey(g.getId())) {
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
        return invites.get(gid);
    }

    public static ArrayList<InviteContainer> getDeletedInvites() {
        return deletedInvites;
    }

    public static HashMap<String, ArrayList<InviteContainer>> getInvites() {
        return invites;
    }

}
