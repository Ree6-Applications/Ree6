package de.presti.ree6.utils;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Map;

/**
 * Utility class used to handle Roles that should be added to Members automatically.
 */
public class AutoRoleHandler {

    /**
     * Check if a Member should get a rule, when joining the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleMemberJoin(Guild guild, Member member) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isAutoRoleSetup(guild.getId())) return;

        new Thread(() -> {

            if (!guild.getSelfMember().canInteract(member)) {
                LoggerImpl.log("AutoRole", "Failed to give a Role, when someone joined the Guild!");
                LoggerImpl.log("AutoRole", "Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                            "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!")
                                    .queue());
                return;
            }

            for (String roleIds : Main.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                Role role = guild.getRoleById(roleIds);

                if (role == null || !guild.getSelfMember().canInteract(role)) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                (role != null ? "The Role that I can't give people when joining is: ``" + role.getName() + "``" :
                                                        "There is a Role that doesn't exists anymore. Please remove it from the Join AutoRole."))
                                        .queue());
                    return;
                }

                if (!member.getRoles().contains(guild.getRoleById(roleIds))) {
                    guild.addRoleToMember(member, role).queue();
                }
            }
        }).start();
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleVoiceLevelReward(Guild guild, Member member) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isVoiceLevelRewardSetup(guild.getId()))
            return;

        new Thread(() -> {

            int level = getLevel(Main.getInstance().getSqlConnector().getSqlWorker().getVoiceXP(guild.getId(), member.getUser().getId()));

            if (!guild.getSelfMember().canInteract(member)) {
                LoggerImpl.log("AutoRole", "Failed to give a Role, when someone leveled up in Voice!");
                LoggerImpl.log("AutoRole", "Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                            sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());

                return;
            }

            for (Map.Entry<Integer, String> entry : Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {

                    Role role = guild.getRoleById(entry.getValue());

                    if (role == null || !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                    (role != null ? "The Role that i cant give people when leveling up is: ``" + role.getName() + "``" :
                                                            "There is a Role that doesn't exists anymore. Please remove it from the Voice AutoRole."))
                                            .queue());
                        return;
                    }

                    addRole(guild, member, role);
                }
            }
        }).start();
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleChatLevelReward(Guild guild, Member member) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isChatLevelRewardSetup(guild.getId()))
            return;

        new Thread(() -> {

            int level = getLevel(Main.getInstance().getSqlConnector().getSqlWorker().getChatXP(guild.getId(), member.getUser().getId()));

            if (!guild.getSelfMember().canInteract(member)) {
                LoggerImpl.log("AutoRole", "Failed to give a Role, when someone leveled up in Chat!");
                LoggerImpl.log("AutoRole", "Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                            sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());

                return;
            }

            for (Map.Entry<Integer, String> entry : Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {
                    Role role = guild.getRoleById(entry.getValue());

                    if (role == null || !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                    (role != null ? "The Role that i cant give people when leveling up is: ``" + role.getName() + "``" :
                                                            "There is a Role that doesn't exists anymore. Please remove it from the Chat AutoRole."))
                                            .queue());
                        return;
                    }

                    addRole(guild, member, role);
                }

            }
        }).start();
    }

    /**
     * Add a Role to the Member, if Ree6 has enough power to do so.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     * @param role   the {@link Role} Entity.
     */
    private static void addRole(Guild guild, Member member, Role role) {
        if (guild.getSelfMember().canInteract(role) && guild.getSelfMember().canInteract(member)) {
            if (!member.getRoles().contains(role)) {
                guild.addRoleToMember(member, role).queue();
            }
        } else {
            LoggerImpl.log("AutoRole", "Failed to give a Role when someone leveled up!");
            LoggerImpl.log("AutoRole", "Server: " + guild.getName());
            if (guild.getOwner() != null)
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                        sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());
        }
    }

    /**
     * Get the Level of the current XP of a Member.
     *
     * @param xp the XP of the Member.
     * @return the Level as {@link Integer}.
     */
    private static int getLevel(long xp) {
        int level = 0;

        while (xp >= 1000) {
            level++;
            xp -= 1000;
        }

        return level;
    }

}
