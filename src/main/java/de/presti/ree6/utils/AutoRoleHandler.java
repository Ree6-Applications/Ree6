package de.presti.ree6.utils;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.Map;

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

    public static void handleVoiceLevelReward(Guild g, Member m) {

        if(!Main.sqlWorker.hasVoiceLevelReward(g.getId()))
            return;

        new Thread(() -> {

            long currentxp = Main.sqlWorker.getXPVC(g.getId(), m.getUser().getId());

            int level = 1;

            while (currentxp > 1000) {
                currentxp -= 1000;
                level++;
            }

            for(Map.Entry<Integer, String> entry : Main.sqlWorker.getVoiceLevelRewards(g.getId()).entrySet()) {

                if(entry.getKey() <= level) {
                    if(!m.getRoles().contains(g.getRoleById(entry.getValue()))) {
                        g.addRoleToMember(m, g.getRoleById(entry.getValue())).queue();
                    }
                }

            }
        }).start();
    }

    public static void handleChatLevelReward(Guild g, Member m) {

        if(!Main.sqlWorker.hasChatLevelReward(g.getId()))
            return;

        new Thread(() -> {

            long currentxp = Main.sqlWorker.getXP(g.getId(), m.getUser().getId());

            int level = 1;

            while (currentxp > 1000) {
                currentxp -= 1000;
                level++;
            }

            for(Map.Entry<Integer, String> entry : Main.sqlWorker.getChatLevelRewards(g.getId()).entrySet()) {

                if(entry.getKey() <= level) {
                    if(!m.getRoles().contains(g.getRoleById(entry.getValue()))) {
                        g.addRoleToMember(m, g.getRoleById(entry.getValue())).queue();
                    }
                }

            }
        }).start();
    }

}
