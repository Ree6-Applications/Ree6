package de.presti.ree6.utils.others;

import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Map;

/**
 * Utility class used to handle Roles that should be added to Members automatically.
 */
@Slf4j
public class AutoRoleHandler {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private AutoRoleHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if a Member should get a rule, when joining the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleMemberJoin(Guild guild, Member member) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isAutoRoleSetup(guild.getId())) return;

        ThreadUtil.createThread(x -> {
            if (!guild.getSelfMember().canInteract(member) && member.getIdLong() != guild.getOwnerIdLong()) {
                log.error("[AutoRole] Failed to give a role, when someone joined the Guild!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                                    .queue());
                return;
            }

            for (de.presti.ree6.sql.entities.roles.Role roles : SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guild.getId())) {
                Role role = guild.getRoleById(roles.getRoleId());

                if (role != null && !guild.getSelfMember().canInteract(role)) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", role.getName()))
                                        .queue());
                    return;
                } else if (role == null) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                        .queue());

                    SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), roles.getRoleId());
                    return;
                }

                addRole(guild, member, role);
            }
        }, null, null, false, true);
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleVoiceLevelReward(Guild guild, Member member) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isVoiceLevelRewardSetup(guild.getId()))
            return;

        ThreadUtil.createThread(x -> {
            long level = SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(guild.getId(), member.getUser().getId()).getLevel();

            if (!guild.getSelfMember().canInteract(member) && member.getIdLong() != guild.getOwnerIdLong()) {
                log.error("[AutoRole] Failed to give a role, when someone leveled up in Voice!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                            .queue());

                return;
            }

            for (Map.Entry<Long, String> entry : SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {

                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", role.getName()))
                                            .queue());
                        return;
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                            .queue());

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), entry.getValue());
                        return;
                    }

                    addRole(guild, member, role);
                }
            }
        }, null, null, false, true);
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleChatLevelReward(Guild guild, Member member) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isChatLevelRewardSetup(guild.getId()))
            return;

        ThreadUtil.createThread(x -> {

            long level = (SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(guild.getId(), member.getUser().getId()).getLevel());

            if (!guild.getSelfMember().canInteract(member) && member.getIdLong() != guild.getOwnerIdLong()) {
                log.error("[AutoRole] Failed to give a Role, when someone leveled up in Chat!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                                    .queue());

                return;
            }

            for (Map.Entry<Long, String> entry : SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guild.getId()).entrySet()) {

                if (entry.getKey() <= level) {
                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", role.getName()))
                                            .queue());
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                            .queue());

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getId(), entry.getValue());
                        return;
                    }

                    addRole(guild, member, role);
                }

            }
        }, null, null, false, true);
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
            log.error("[AutoRole] Failed to give a Role!");
            log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
            if (guild.getOwner() != null)
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", role.getName()))
                                .queue());
        }
    }
}
