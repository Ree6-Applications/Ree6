package de.presti.ree6.utils.others;

import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.sql.SQLSession;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Map;

/**
 * Utility class used to handle Guild specific stuff that is being used multiple times.
 */
@Slf4j
public class GuildUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private GuildUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if a Member should get a rule, when joining the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleMemberJoin(Guild guild, Member member) {

        SQLSession.getSqlConnector().getSqlWorker().isAutoRoleSetup(guild.getIdLong()).subscribe(x -> {
            if (!x) return;

            if (member.getIdLong() == guild.getOwnerIdLong()) return;

            if (!guild.getSelfMember().canInteract(member)) {
                log.error("[AutoRole] Failed to give a role, when someone joined the Guild!");
                log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                if (guild.getOwner() != null)
                    LanguageService.getByGuild(guild, "message.brs.autoRole.user", member.getUser().getName()).subscribe(message ->
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    privateChannel.sendMessage(message)
                                            .queue()));
                return;
            }

            SQLSession.getSqlConnector().getSqlWorker().getAutoRoles(guild.getIdLong()).subscribe(roles -> {
                for (de.presti.ree6.sql.entities.roles.Role roleEntry : roles) {
                    Role role = guild.getRoleById(roleEntry.getRoleId());

                    if (role != null && !guild.getSelfMember().canInteract(role)) {
                        if (guild.getOwner() != null)
                            LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                    "message.brs.autoRole.hierarchy"
                                    : "message.brs.autoRole.missingPermission", role.getName()).subscribe(message ->
                                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                            privateChannel.sendMessage(message)
                                                    .queue()));
                        return;
                    } else if (role == null) {
                        if (guild.getOwner() != null)
                            guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                    LanguageService.getByGuild(guild, "message.brs.autoRole.deleted").subscribe(message ->
                                            privateChannel.sendMessage(message)
                                                    .queue()));

                        SQLSession.getSqlConnector().getSqlWorker().removeAutoRole(guild.getIdLong(), roleEntry.getRoleId());
                        return;
                    }

                    addRole(guild, member, role);
                }
            });
        });
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleVoiceLevelReward(Guild guild, Member member) {

        SQLSession.getSqlConnector().getSqlWorker().isVoiceLevelRewardSetup(guild.getIdLong()).subscribe(x -> {
            if (!x) return;

            if (member.getIdLong() == guild.getOwnerIdLong()) return;

            SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelData(guild.getIdLong(), member.getUser().getIdLong()).subscribe(data -> {
                if (data == null) return;

                if (!guild.getSelfMember().canInteract(member)) {
                    log.error("[AutoRole] Failed to give a role, when someone leveled up in Voice!");
                    log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                    log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                LanguageService.getByGuild(guild, "message.brs.autoRole.user", member.getUser().getName()).subscribe(message ->
                                        privateChannel.sendMessage(message).queue()));

                    return;
                }

                SQLSession.getSqlConnector().getSqlWorker().getVoiceLevelRewards(guild.getIdLong()).subscribe(roles -> {
                    for (Map.Entry<Long, Long> entry : roles.entrySet()) {

                        if (entry.getKey() <= data.getLevel()) {

                            Role role = guild.getRoleById(entry.getValue());

                            if (role != null && !guild.getSelfMember().canInteract(role)) {
                                if (guild.getOwner() != null)
                                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                            LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                                    "message.brs.autoRole.hierarchy"
                                                    : "message.brs.autoRole.missingPermission", role.getName()).subscribe(message ->
                                                    privateChannel.sendMessage(message).queue()));
                                return;
                            } else if (role == null) {
                                if (guild.getOwner() != null)
                                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                            LanguageService.getByGuild(guild, "message.brs.autoRole.deleted").subscribe(message ->
                                                    privateChannel.sendMessage(message).queue()));

                                SQLSession.getSqlConnector().getSqlWorker().removeVoiceLevelReward(guild.getIdLong(), entry.getValue());
                                return;
                            }

                            addRole(guild, member, role);
                        }
                    }
                });
            });
        });
    }

    /**
     * Check if a Member should get a role, when leveling up in the Guild, and if Ree6 has enough permissions.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     */
    public static void handleChatLevelReward(Guild guild, Member member) {

        SQLSession.getSqlConnector().getSqlWorker().isChatLevelRewardSetup(guild.getIdLong()).subscribe(x -> {
            if (!x) return;

            if (member.getIdLong() == guild.getOwnerIdLong()) return;

            SQLSession.getSqlConnector().getSqlWorker().getChatLevelData(guild.getIdLong(), member.getUser().getIdLong()).subscribe(data -> {
                if (data == null) return;

                if (!guild.getSelfMember().canInteract(member)) {
                    log.error("[AutoRole] Failed to give a Role, when someone leveled up in Chat!");
                    log.error("[AutoRole] Server: {} ({})", guild.getName(), guild.getId());
                    log.error("[AutoRole] Member: {} ({})", member.getUser().getName(), member.getId());

                    if (guild.getOwner() != null)
                        guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                LanguageService.getByGuild(guild, "message.brs.autoRole.user", member.getUser().getName())
                                        .subscribe(message -> privateChannel.sendMessage(message).queue()));
                    return;
                }

                SQLSession.getSqlConnector().getSqlWorker().getChatLevelRewards(guild.getIdLong()).subscribe(roles -> {
                    for (Map.Entry<Long, Long> entry : roles.entrySet()) {

                        if (entry.getKey() <= data.getLevel()) {
                            Role role = guild.getRoleById(entry.getValue());

                            if (role != null && !guild.getSelfMember().canInteract(role)) {
                                if (guild.getOwner() != null)
                                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                            LanguageService.getByGuild(guild, guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES) ?
                                                    "message.brs.autoRole.hierarchy"
                                                    : "message.brs.autoRole.missingPermission", role.getName()).subscribe(message ->
                                                    privateChannel.sendMessage(message).queue()));
                            } else if (role == null) {
                                if (guild.getOwner() != null)
                                    guild.getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                                            LanguageService.getByGuild(guild, "message.brs.autoRole.deleted").subscribe(message ->
                                                    privateChannel.sendMessage(message).queue()));

                                SQLSession.getSqlConnector().getSqlWorker().removeChatLevelReward(guild.getIdLong(), entry.getValue());
                                return;
                            }

                            addRole(guild, member, role);
                        }
                    }
                });
            });
        });
    }

    /**
     * Add a Role to the Member if Ree6 has enough power to do so.
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
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> {
                    String[] languageResource = getFailedRoleReason(guild, member, role);
                    LanguageService.getByGuild(guild, languageResource[0], languageResource[1]).subscribe(message ->
                            privateChannel.sendMessage(message).queue());
                });
        }
    }

    /**
     * Get a String Array with the Language Path and the correct Parameter.
     *
     * @param guild  the {@link Guild} Entity.
     * @param member the {@link Member} Entity.
     * @param role   the {@link Role} Entity.
     * @return the String Array with two entries, 0 = language path and 1 = parameter.
     */
    private static String[] getFailedRoleReason(Guild guild, Member member, Role role) {
        String languageResource = "message.brs.autoRole.missingPermission";

        if (!guild.getSelfMember().canInteract(member)) languageResource = "message.brs.autoRole.user";
        if (guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
            languageResource = "message.brs.autoRole.hierarchy";

        return new String[]{languageResource, guild.getSelfMember().canInteract(member) ? role.getName() : member.getUser().getName()};
    }

    /**
     * Get all roles that Ree6 can manage.
     *
     * @param guild the Guild to get the roles from.
     * @return a List of Roles that Ree6 can manage.
     */
    public static List<Role> getManageableRoles(Guild guild) {
        return guild.getRoles().stream().filter(role -> guild.getSelfMember().canInteract(role) && !role.isManaged() && !role.isPublicRole()).toList();
    }

    /**
     * Checks if a specific user has supported Ree6 via Donations!
     *
     * @param member the User of the current Guild to check.
     * @return true if the User has supported Ree6 via Donations, false if not.
     */
    public static boolean isSupporter(User member) {
        if (member.getId().equalsIgnoreCase(BotConfig.getBotOwner())) return true;

        if (!member.getJDA().retrieveEntitlements().excludeEnded(true).skuIds(1165934495447384144L).complete().isEmpty()) {
            return true;
        }

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
