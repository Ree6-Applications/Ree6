package de.presti.ree6.utils;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class AutoRoleHandler {

    public static void handleMemberJoin(Guild g, Member m) {

        if(!Main.sqlWorker.hasAutoRoles(g.getId()))
            return;

        new Thread(() -> {
            for(String ids : Main.sqlWorker.getAutoRoleIDs(g.getId())) {
                if(!m.getRoles().contains(g.getRoleById(ids))) {
                    g.addRoleToMember(m, g.getRoleById(ids)).queue();
                }
            }
        }).start();
    }

}
