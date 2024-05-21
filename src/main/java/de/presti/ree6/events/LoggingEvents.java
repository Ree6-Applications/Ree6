package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.logger.LogMessage;
import de.presti.ree6.logger.LogTyp;
import de.presti.ree6.logger.events.LogMessageMember;
import de.presti.ree6.logger.events.LogMessageRole;
import de.presti.ree6.logger.events.LogMessageUser;
import de.presti.ree6.logger.events.LogMessageVoice;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.invite.InviteContainer;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Invite;
import de.presti.ree6.sql.entities.webhook.base.Webhook;
import de.presti.ree6.utils.data.ArrayUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateVanityCodeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

/**
 * Event Handler for all Events related to Logs.
 */
public class LoggingEvents extends ListenerAdapter {

    //region Guild

    /**
     * @see ListenerAdapter#onGuildUpdateVanityCode(GuildUpdateVanityCodeEvent)
     */
    @Override
    public void onGuildUpdateVanityCode(@NotNull GuildUpdateVanityCodeEvent event) {
        super.onGuildUpdateVanityCode(event);

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) return;

        if (event.getOldVanityCode() == null) {
            event.getGuild().retrieveVanityInvite().onErrorMap(throwable -> null).queue(vanityInvite ->
                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(new Invite(event.getGuild().getIdLong(), event.getGuild().getOwnerIdLong(), vanityInvite.getUses(), event.getNewVanityCode())));
        } else {
            SQLSession.getSqlConnector().getSqlWorker().getEntity(new Invite(), "FROM Invite WHERE guildAndCode.guildId = :gid AND guildAndCode.code = :code",
                    Map.of("gid", event.getGuild().getIdLong(), "code", event.getOldVanityCode())).thenAccept(invite -> {
                invite.setCode(event.getNewVanityCode());
                SQLSession.getSqlConnector().getSqlWorker().updateEntity(invite);
            });
        }
    }

    /**
     * @see ListenerAdapter#onGuildBan(GuildBanEvent)
     */
    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_memberban").thenAccept(shouldLog -> {
                if (!shouldLog.getBooleanValue()) return;

                WebhookMessageBuilder wm = new WebhookMessageBuilder();

                wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                wm.setUsername(BotConfig.getBotName() + "-Logs");

                WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                we.setColor(Color.BLACK.getRGB());
                we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
                we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
                we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                we.setTimestamp(Instant.now());
                we.setDescription(LanguageService.getByEvent(event, "logging.banned", event.getUser().getAsMention()).join());

                AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.BAN).limit(5).stream().filter(auditLogEntry ->
                        auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

                if (entry != null && entry.getUser() != null)
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor").join() + "**", entry.getUser().getAsMention()));

                wm.addEmbeds(we.build());

                Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).join();
                Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.USER_BAN, event.getUser()));
            });
        });
    }

    /**
     * @see ListenerAdapter#onGuildUnban(GuildUnbanEvent)
     */
    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_memberunban").thenAccept(shouldLog -> {
                if (!shouldLog.getBooleanValue()) return;

                WebhookMessageBuilder wm = new WebhookMessageBuilder();

                wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                wm.setUsername(BotConfig.getBotName() + "-Logs");

                WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                we.setColor(Color.BLACK.getRGB());
                we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
                we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
                we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                we.setTimestamp(Instant.now());
                we.setDescription(LanguageService.getByEvent(event, "logging.unbanned", event.getUser().getAsMention()).join());

                AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).limit(5).stream().filter(auditLogEntry ->
                        auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

                if (entry != null && entry.getUser() != null)
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor").join() + "**", entry.getUser().getAsMention()));

                wm.addEmbeds(we.build());

                Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).join();
                Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.USER_UNBAN, event.getUser()));
            });
        });
    }

    //endregion

    //region Member

    /**
     * @see ListenerAdapter#onGuildMemberJoin(GuildMemberJoinEvent)
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_memberjoin").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;
                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByEvent(event, "logging.joined.default", event.getUser().getAsMention(), TimeFormat.DATE_TIME_SHORT.format(event.getUser().getTimeCreated()), TimeFormat.RELATIVE.format(event.getUser().getTimeCreated())).join());

                    wm.addEmbeds(we.build());
                    Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.SERVER_JOIN, event.getUser()));
                });

                if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_invite").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;
                        WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

                        wm2.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm2.setUsername(BotConfig.getBotName() + "-InviteLogs");

                        if (event.getUser().isBot()) {
                            event.getGuild().retrieveAuditLogs().type(ActionType.BOT_ADD).limit(1).queue(auditLogEntries -> {
                                if (auditLogEntries.isEmpty()) {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()).join());
                                    return;
                                }
                                AuditLogEntry entry = auditLogEntries.get(0);

                                if (entry.getUser() == null) {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()).join());
                                    return;
                                }

                                if (entry.getTargetId().equals(event.getUser().getId())) {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.found", event.getUser().getAsMention(), entry.getUser().getAsMention()).join());
                                } else {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()).join());
                                }
                            });
                        } else {
                            InviteContainer inviteContainer = Main.getInstance().getInviteContainerManager().getRightInvite(event.getGuild());
                            if (inviteContainer != null) {
                                inviteContainer.setUses(inviteContainer.getUses() + 1);
                                if (inviteContainer.isVanity()) {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.vanity", event.getUser().getAsMention()).join());
                                } else {
                                    wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.default", event.getUser().getAsMention(), "<@" + inviteContainer.getCreatorId() + ">", inviteContainer.getCode(), inviteContainer.getUses()).join());
                                }
                                Main.getInstance().getInviteContainerManager().add(inviteContainer);
                            } else {
                                wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.notFound", event.getMember().getAsMention()).join());
                            }
                        }

                        Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm2.build(), event.getGuild(), LogTyp.SERVER_INVITE, event.getUser()));
                    });
                }
            });
        });

    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {


        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_memberleave").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;
                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());

                    if (event.getMember() != null) {
                        we.setDescription(LanguageService.getByEvent(event, "logging.left.default", event.getUser().getAsMention(), TimeFormat.DATE_TIME_SHORT.format(event.getMember().getTimeJoined())).join());
                    } else {
                        we.setDescription(LanguageService.getByEvent(event, "logging.left.slim", event.getUser().getAsMention()).join());
                    }

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.KICK).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.SERVER_LEAVE, event.getUser()));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
        super.onGuildMemberUpdateTimeOut(event);

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_timeout").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getMember().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getMember().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    AuditLogPaginationAction paginationAction = event.getGuild().retrieveAuditLogs().user(event.getUser()).type(ActionType.MEMBER_UPDATE).limit(1);
                    if (event.getNewTimeOutEnd() != null) {
                        if (paginationAction.isEmpty()) {
                            we.setDescription(LanguageService.getByEvent(event, "logging.timeout.started",
                                    event.getUser().getAsMention(),
                                    TimeFormat.DATE_TIME_SHORT.format(event.getNewTimeOutEnd())).join());
                        } else {
                            AuditLogEntry auditLogEntry = paginationAction.getFirst();
                            we.setDescription(LanguageService.getByEvent(event, "logging.timeout.updated",
                                    event.getUser().getAsMention(),
                                    (auditLogEntry.getReason() == null ? "Couldn't find reason" : auditLogEntry.getReason()),
                                    (auditLogEntry.getUser() != null ? auditLogEntry.getUser().getAsMention() : LanguageService.getByGuild(event.getGuild(), "label.unknown")),
                                    TimeFormat.DATE_TIME_SHORT.format(event.getNewTimeOutEnd())).join());
                        }
                    } else {
                        we.setDescription(LanguageService.getByEvent(event, "logging.timeout.ended",
                                event.getUser().getAsMention(),
                                TimeFormat.DATE_TIME_SHORT.format(event.getOldTimeOutEnd())).join());
                    }

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageMember(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ELSE, event.getMember()));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_nickname").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;
                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());

                    if (event.getNewNickname() == null) {
                        we.setDescription(LanguageService.getByEvent(event, "logging.nickname.reset", event.getUser().getAsMention(), event.getOldNickname()).join());
                    } else {
                        we.setDescription(LanguageService.getByEvent(event, "logging.nickname.changed", event.getUser().getAsMention(), event.getNewNickname(), (event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName())).join());
                    }

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageMember(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.NICKNAME_CHANGE, event.getEntity(), event.getOldNickname(), event.getNewNickname()));
                });
            });
        });
    }

    //endregion

    //region Voice


    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        super.onGuildVoiceUpdate(event);
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                if (event.getChannelLeft() == null) {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_voicejoin").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.join", event.getEntity().getUser().getAsMention(), event.getChannelJoined().getAsMention()).join());

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_JOIN, event.getEntity(), event.getChannelJoined()));
                    });
                } else if (event.getChannelJoined() == null) {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_voiceleave").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.leave", event.getEntity().getUser().getAsMention(), event.getChannelLeft().getAsMention()).join());

                        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_VOICE_KICK).limit(5).stream().filter(auditLogEntry ->
                                auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

                        if (entry != null && entry.getUser() != null)
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_LEAVE, event.getEntity(), event.getChannelLeft()));
                    });
                } else {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_voicemove").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.move", event.getEntity().getUser().getAsMention(), event.getChannelLeft().getAsMention(), event.getChannelJoined().getAsMention()).join());

                        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).limit(5).stream().filter(auditLogEntry ->
                                auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

                        if (entry != null && entry.getUser() != null)
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_MOVE, event.getEntity(), event.getChannelLeft(), event.getChannelJoined()));
                    });
                }
            });
        });
    }

    //endregion

    //region Channel

    /**
     * @inheritDoc
     */
    @Override
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                if (event.getChannelType().isAudio()) {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_voicechannel").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();
                        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());

                        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.update.voice", event.getChannel().getAsMention()).join());

                        AuditLogEntry entry;

                        if (event instanceof ChannelCreateEvent) {
                            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.create.voice", event.getChannel().getAsMention()).join());
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else if (event instanceof ChannelDeleteEvent) {
                            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.delete.voice", event.getChannel().getName()).join());
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_DELETE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else if (event instanceof ChannelUpdateNameEvent channelUpdateNameEvent) {
                            we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", channelUpdateNameEvent.getOldValue() != null
                                    ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                            we.addField(new WebhookEmbed.EmbedField(true, "**New name**", channelUpdateNameEvent.getNewValue() != null
                                    ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else {
                            return;
                        }

                        if (entry != null && entry.getUser() != null)
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessage(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));
                    });
                } else {
                    SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_textchannel").thenAccept(shouldLog -> {
                        if (!shouldLog.getBooleanValue()) return;

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());

                        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.update.chat", event.getChannel().getAsMention()).join());
                        we.setDescription(":house: **TextChannel updated:** " + event.getChannel().getAsMention());

                        AuditLogEntry entry;

                        if (event instanceof ChannelCreateEvent) {
                            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.create.chat", event.getChannel().getAsMention()).join());
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else if (event instanceof ChannelDeleteEvent) {
                            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.delete.chat", event.getChannel().getName()).join());
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_DELETE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else if (event instanceof ChannelUpdateNameEvent channelUpdateNameEvent) {
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldName") + "**", channelUpdateNameEvent.getOldValue() != null
                                    ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newName") + "**", channelUpdateNameEvent.getNewValue() != null
                                    ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else if (event instanceof ChannelUpdateNSFWEvent channelUpdateNSFWEvent) {
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.nsfw") + "**", channelUpdateNSFWEvent.getNewValue() + ""));
                            entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).limit(5).stream().filter(auditLogEntry ->
                                    auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
                        } else {
                            return;
                        }

                        if (entry != null && entry.getUser() != null)
                            we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessage(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));
                    });
                }
            });
        });
    }

    //endregion

    //region Role

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_roleadd").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getMember().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getMember().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());

                    StringBuilder finalString = new StringBuilder();

                    for (Role r : event.getRoles()) {
                        finalString.append(":white_check_mark: ").append(r.getName()).append("\n");
                    }

                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.member", event.getMember().getAsMention()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.roles").join() + ":**", finalString.toString()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageMember(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), null, new ArrayList<>(event.getRoles())));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_roleremove").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setThumbnailUrl(event.getMember().getEffectiveAvatarUrl());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getMember().getEffectiveAvatarUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());

                    StringBuilder finalString = new StringBuilder();
                    for (Role r : event.getRoles()) {
                        finalString.append(":no_entry: ").append(r.getName()).append("\n");
                    }

                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.member", event.getMember().getAsMention()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.roles").join() + ":**", finalString.toString()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageMember(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), new ArrayList<>(event.getRoles()), null));
                });
            });
        });


    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolecreate").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.create", event.getRole().getName()).join());

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_CREATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), true, false, false, false));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_roledelete").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.delete", event.getRole().getName()).join());

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_DELETE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, true, false, false));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolename").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldName").join() + "**", event.getOldName()));
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newName").join() + "**", event.getNewName()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldName(), event.getNewName()));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolemention").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;
                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldMentionable").join() + "**", event.getOldValue().toString()));
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newMentionable").join() + "**", event.getNewValue().toString()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, false, true));
                });
            });
        });
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolehoisted").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldHoist").join() + "**", event.getOldValue().toString()));
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newHoist").join() + "**", event.getNewValue().toString()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, true, false));
                });
            });
        });


    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolepermission").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()).join());

                    StringBuilder finalString = new StringBuilder();

                    boolean b = false;
                    for (Permission r : event.getNewPermissions()) {
                        if (!event.getOldPermissions().contains(r)) {
                            if (b) {
                                finalString.append("\n:white_check_mark: ").append(r.getName());
                            } else {
                                finalString.append(":white_check_mark: ").append(r.getName());
                                b = true;
                            }
                        }
                    }

                    for (Permission r : event.getOldPermissions()) {
                        if (!event.getNewPermissions().contains(r)) {
                            if (b) {
                                finalString.append("\n:no_entry: ").append(r.getName());
                            } else {
                                finalString.append(":no_entry: ").append(r.getName());
                                b = true;
                            }
                        }
                    }

                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newPermissions").join() + "**", finalString.toString()));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldPermissions(), event.getNewPermissions()));
                });
            });
        });


    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_rolecolor").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    WebhookMessageBuilder wm = new WebhookMessageBuilder();

                    wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
                    wm.setUsername(BotConfig.getBotName() + "-Logs");

                    WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                    we.setColor(Color.BLACK.getRGB());
                    we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
                    we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                    we.setTimestamp(Instant.now());
                    we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()).join());
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldColor").join() + "**", (event.getOldColor() != null ? event.getOldColor() : Color.gray).getRGB() + ""));
                    we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newColor").join() + "**", (event.getNewColor() != null ? event.getNewColor() : Color.gray).getRGB() + ""));

                    AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                            auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

                    if (entry != null && entry.getUser() != null)
                        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor").join() + "**", entry.getUser().getAsMention()));

                    wm.addEmbeds(we.build());

                    Main.getInstance().getLoggerQueue().add(new LogMessageRole(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), (event.getOldColor() != null ? event.getOldColor() : Color.gray), (event.getNewColor() != null ? event.getNewColor() : Color.gray)));
                });
            });
        });


    }

    //endregion

    //region Message

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getIdLong()).thenAccept(isSetup -> {
            if (!isSetup) return;
            SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getIdLong()).thenAccept(webhook -> {
                SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getIdLong(), "logging_messagedelete").thenAccept(shouldLog -> {
                    if (!shouldLog.getBooleanValue()) return;

                    User user = ArrayUtil.getUserFromMessageList(event.getMessageId());

                    if (user != null) {
                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
                        wm.setUsername(BotConfig.getBotName() + "-Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(user.getName(), user.getEffectiveAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + BotConfig.getAdvertisement(), event.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());

                        Message message = ArrayUtil.getMessageFromMessageListAndRemove(event.getMessageId());

                        if (message != null && message.getActivity() != null) return;

                        boolean isImageAdded = false;

                        if (message != null && !message.getAttachments().isEmpty()) {
                            for (Message.Attachment attachment : message.getAttachments()) {
                                try {
                                    if (!isImageAdded && attachment.isImage()) {
                                        we.setImageUrl(attachment.getProxyUrl());
                                        isImageAdded = true;
                                    } else {
                                        wm.addFile(attachment.getFileName(), attachment.getProxy().download().get());
                                    }
                                } catch (Exception exception) {
                                    wm.append(LanguageService.getByGuild(event.getGuild(), "logging.message.attachmentFailed", attachment.getFileName()).join() + "\n");
                                }
                            }
                            wm.append(LanguageService.getByGuild(event.getGuild(), "logging.message.attachmentNotice").join() + "\n");
                        }

                        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.message.deleted", user.getAsMention(), event.getChannel().getAsMention(),
                                message != null ? message.getContentRaw().length() >= 650 ?
                                        LanguageService.getByGuild(event.getGuild(), "logging.message.tooLong").join() :
                                        message.getContentRaw() : "").join());

                        if (message != null && message.getContentRaw().length() >= 650)
                            wm.addFile("message.txt", message.getContentRaw().getBytes(StandardCharsets.UTF_8));

                        wm.addEmbeds(we.build());

                        Main.getInstance().getLoggerQueue().add(new LogMessageUser(webhook.getWebhookId(), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MESSAGE_DELETE, user));
                    }
                });
            });
        });
    }

    //endregion

    //region Invite

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        if (event.getInvite().getInviter() != null) {
            InviteContainer inv = new InviteContainer(event.getInvite().getInviter().getIdLong(), event.getGuild().getIdLong(), event.getInvite().getCode(), event.getInvite().getUses(), false);
            Main.getInstance().getInviteContainerManager().add(inv);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        Main.getInstance().getInviteContainerManager().remove(event.getGuild().getIdLong(), event.getCode());
    }

    //endregion
}