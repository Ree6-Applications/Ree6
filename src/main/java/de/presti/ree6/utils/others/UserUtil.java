package de.presti.ree6.utils.others;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Map;

/**
 * Utility class used to handle User specific stuff that is being used multiple times.
 */
@Slf4j
public class UserUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private UserUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if a Member should get a rule, when joining the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleMemberJoin(Guild guild, Member member) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isAutoRoleSetup(guild.getIdLong())) return;

        if (member.getIdLong() == guild.getOwnerIdLong()) return;

        ThreadUtil.createThread(x -> {
            if (!guild.getSelfMember().canInteract(member)) {
                log.error("[AutoRole] Failed to give a role, when someone joined the Guild!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                                    .queue());
                return;
            }

            for (de.presti.ree6.sql.entities.roles.Role roles : SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guild.getIdLong())) {
                Role role = guild.getRoleById(roles.getRoleId());

                if (role != null && !guild.getSelfMember().canInteract(role)) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage(LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                                "message.brs.autoRole.hierarchy"
                                                : "message.brs.autoRole.missingPermission", role.getName()))
                                        .queue());
                    return;
                } else if (role == null) {
                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                        .queue());

                    SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getIdLong(), roles.getRoleId());
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

        if (!SQLSession.getSqlConnector().getSqlWorker().isVoiceLevelRewardSetup(guild.getIdLong()))
            return;

        if (member.getIdLong() == guild.getOwnerIdLong()) return;

        ThreadUtil.createThread(x -> {
            long level = SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(guild.getIdLong(), member.getUser().getIdLong()).getLevel();

            if (!guild.getSelfMember().canInteract(member)) {
                log.error("[AutoRole] Failed to give a role, when someone leveled up in Voice!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                            .queue());

                return;
            }

            for (Map.Entry<Long, Long> entry : SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guild.getIdLong()).entrySet()) {

                if (entry.getKey() <= level) {

                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                                    "message.brs.autoRole.hierarchy"
                                                    : "message.brs.autoRole.missingPermission", role.getName()))
                                            .queue());
                        return;
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                            .queue());

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getIdLong(), entry.getValue());
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

        if (!SQLSession.getSqlConnector().getSqlWorker().isChatLevelRewardSetup(guild.getIdLong()))
            return;

        if (member.getIdLong() == guild.getOwnerIdLong()) return;

        ThreadUtil.createThread(x -> {

            long level = (SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(guild.getIdLong(), member.getUser().getIdLong()).getLevel());

            if (!guild.getSelfMember().canInteract(member)) {
                log.error("[AutoRole] Failed to give a Role, when someone leveled up in Chat!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                if (guild.getOwner() != null)
                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.hierarchy", "@everyone"))
                                    .queue());

                return;
            }

            for (Map.Entry<Long, Long> entry : SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guild.getIdLong()).entrySet()) {

                if (entry.getKey() <= level) {
                    Role role = guild.getRoleById(entry.getValue());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                                    "message.brs.autoRole.hierarchy"
                                                    : "message.brs.autoRole.missingPermission", role.getName()))
                                            .queue());
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(LanguageService.getByGuild(guild, "message.brs.autoRole.deleted"))
                                            .queue());

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getIdLong(), entry.getValue());
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
                        privateChannel.sendMessage(LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                        "message.brs.autoRole.hierarchy"
                                        : "message.brs.autoRole.missingPermission", role.getName()))
                                .queue());
        }
    }

    /**
     * Checks if a specific user has supported Ree6 via Donations!
     * @param member the User of the current Guild to check.
     * @return true if the User has supported Ree6 via Donations, false if not.
     */
    public static boolean isSupporter(ISnowflake member) {
        Guild ree6Guild = BotWorker.getShardManager().getGuildById(805149057004732457L);

        if (ree6Guild != null) {
            Member ree6Member = ree6Guild.getMemberById(member.getId());

            if (ree6Member != null) {
                return ree6Member.getRoles().stream()
                        .anyMatch(c -> c.getIdLong() == 910133809327009822L);
            }
        }
        return false;
    }
}
