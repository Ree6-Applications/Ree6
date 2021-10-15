package de.presti.ree6.utils;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;

import java.util.Map;

public class AutoRoleHandler {

    public static void handleMemberJoin(Guild g, Member m) {

        if (!Main.sqlWorker.hasAutoRoles(g.getId()))
            return;

        new Thread(() -> {

            if (!g.getSelfMember().canInteract(m)) {
                Logger.log("AutoRole", "Failed to give a Role when someone joined!");
                Logger.log("AutoRole", "Server: " + g.getName());
                if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue();
                return;
            }

            for (String ids : Main.sqlWorker.getAutoRoleIDs(g.getId())) {
                if (!m.getRoles().contains(g.getRoleById(ids))) {
                    if (g.getSelfMember().canInteract(g.getRoleById(ids))) {
                        g.addRoleToMember(m, g.getRoleById(ids)).queue();
                    } else {
                        Logger.log("AutoRole", "Failed to give a Role!");
                        Logger.log("AutoRole", "Role: " + g.getRoleById(ids).getName());
                        Logger.log("AutoRole", "Server: " + g.getName());
                        if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\nThe Role that i cant give people when joining is: " + g.getRoleById(ids)).queue();
                    }
                }
            }
        }).start();
    }

    public static void handleVoiceLevelReward(Guild g, Member m) {

        if (!Main.sqlWorker.hasVoiceLevelReward(g.getId()))
            return;

        new Thread(() -> {

            long currentxp = Main.sqlWorker.getXPVC(g.getId(), m.getUser().getId());

            int level = 1;

            while (currentxp > 1000) {
                currentxp -= 1000;
                level++;
            }

            if (!g.getSelfMember().canInteract(m)) {
                Logger.log("AutoRole", "Failed to give a Role when someone leveled up!");
                Logger.log("AutoRole", "Server: " + g.getName());
                if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue();
                return;
            }

            for (Map.Entry<Integer, String> entry : Main.sqlWorker.getVoiceLevelRewards(g.getId()).entrySet()) {

                if (entry.getKey() <= level) {
                    if (!m.getRoles().contains(g.getRoleById(entry.getValue()))) {
                        if (g.getSelfMember().canInteract(g.getRoleById(entry.getValue()))) {
                            g.addRoleToMember(m, g.getRoleById(entry.getValue())).queue();
                        } else {
                            Logger.log("AutoRole", "Failed to give a Role!");
                            Logger.log("AutoRole", "Role: " + g.getRoleById(entry.getValue()).getName());
                            Logger.log("AutoRole", "Server: " + g.getName());
                            if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\nThe Role that i cant give people when leveling up: " + g.getRoleById(entry.getValue())).queue();
                        }
                    }
                }
            }
        }).start();
    }

    public static void handleChatLevelReward(Guild g, Member m) {

        if (!Main.sqlWorker.hasChatLevelReward(g.getId()))
            return;

        new Thread(() -> {

            long currentxp = Main.sqlWorker.getXP(g.getId(), m.getUser().getId());

            int level = 1;

            while (currentxp > 1000) {
                currentxp -= 1000;
                level++;
            }

            if (!g.getSelfMember().canInteract(m)) {
                Logger.log("AutoRole", "Failed to give a Role when someone leveled up!");
                Logger.log("AutoRole", "Server: " + g.getName());
                if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue();
                return;
            }

            for (Map.Entry<Integer, String> entry : Main.sqlWorker.getChatLevelRewards(g.getId()).entrySet()) {

                if (entry.getKey() <= level) {
                    if (!m.getRoles().contains(g.getRoleById(entry.getValue()))) {
                        if (g.getSelfMember().canInteract(g.getRoleById(entry.getValue()))) {
                            g.addRoleToMember(m, g.getRoleById(entry.getValue())).queue();
                        } else {
                            Logger.log("AutoRole", "Failed to give a Role!");
                            Logger.log("AutoRole", "Role: " + g.getRoleById(entry.getValue()).getName());
                            Logger.log("AutoRole", "Server: " + g.getName());
                            if (g.getOwner() != null) g.getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\nIf you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\nThe Role that i cant give people when leveling up: " + g.getRoleById(entry.getValue())).queue();
                        }
                    }
                }

            }
        }).start();
    }

}
