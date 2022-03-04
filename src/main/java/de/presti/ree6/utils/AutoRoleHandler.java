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
                Main.getInstance().getLogger().error("[AutoRole] Failed to give a Role, when someone joined the Guild!");
                Main.getInstance().getLogger().error("[AutoRole] Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                            "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!")
                                    .queue());
                return;
            }

            for (String roleIds : Main.getInstance().getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                Role role = guild.getRoleById(roleIds);

                if (role != null && !guild.getSelfMember().canInteract(role)) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                "The Role that I can't give people when joining is: ``" + role.getName() + "``")
                                        .queue());
                    return;
                } else if (role == null) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                "There was an invalid Role set, which has been removed now from the AutoRole list.\n" +
                                                "Since it does't exists anymore!")
                                        .queue());

                    Main.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), roleIds);
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

            long level = Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelData(guild.getId(), member.getUser().getId()).getLevel();

            if (!guild.getSelfMember().canInteract(member)) {
                Main.getInstance().getLogger().error("[AutoRole] Failed to give a Role, when someone leveled up in Voice!");
                Main.getInstance().getLogger().error("[AutoRole] Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                            sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());

                return;
            }

            for (Map.Entry<Integer, String> entry : Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {

                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                    (role != null ? "The Role that i cant give people when leveling up is: ``" + role.getName() + "``" :
                                                            "There is a Role that doesn't exists anymore. Please remove it from the Voice AutoRole."))
                                            .queue());
                        return;
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "There was an invalid Role set, which has been removed now from the Voice-AutoRole list.\n" +
                                                    "Since it does't exists anymore!")
                                            .queue());

                        Main.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), entry.getValue());
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

            long level = (Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(guild.getId(), member.getUser().getId()).getLevel());

            if (!guild.getSelfMember().canInteract(member)) {
                Main.getInstance().getLogger().error("[AutoRole] Failed to give a Role, when someone leveled up in Chat!");
                Main.getInstance().getLogger().error("[AutoRole] Server: " + guild.getName());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                            sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());

                return;
            }

            for (Map.Entry<Integer, String> entry : Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {
                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!\n" +
                                                    (role != null ? "The Role that i cant give people when leveling up is: ``" + role.getName() + "``" :
                                                            "There is a Role that doesn't exists anymore. Please remove it from the Chat AutoRole."))
                                            .queue());
                        return;
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                                    "There was an invalid Role set, which has been removed now from the Chat-AutoRole list.\n" +
                                                    "Since it does't exists anymore!")
                                            .queue());

                        Main.getInstance().getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), entry.getValue());
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
            Main.getInstance().getLogger().error("[AutoRole] Failed to give a Role when someone leveled up!");
            Main.getInstance().getLogger().error("[AutoRole] Server: " + guild.getName());
            if (guild.getOwner() != null)
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                        sendMessage("Hey its the BRS(short for Bug-Report-System) from Ree6!\n" +
                                "If you didn't notice im not allowed to AutoRole People because the Role is higher than my own Role!").queue());
        }
    }
}
