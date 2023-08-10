package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.logger.events.LogMessage;
import de.presti.ree6.logger.events.LogTyp;
import de.presti.ree6.logger.events.implentation.LogMessageMember;
import de.presti.ree6.logger.events.implentation.LogMessageRole;
import de.presti.ree6.logger.events.implentation.LogMessageUser;
import de.presti.ree6.logger.events.implentation.LogMessageVoice;
import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.logger.invite.InviteContainerManager;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Invite;
import de.presti.ree6.sql.entities.webhook.Webhook;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Data;
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
     * @inheritDoc
     */
    @Override
    public void onGuildUpdateVanityCode(@NotNull GuildUpdateVanityCodeEvent event) {
        super.onGuildUpdateVanityCode(event);

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) return;

        Invite invite = event.getOldVanityCode() != null ?
                SQLSession.getSqlConnector().getSqlWorker().getEntity(new Invite(), "FROM Invite WHERE guild = :gid AND code = :code",
                        Map.of("gid", event.getGuild().getId(), "code", event.getOldVanityCode()))
                : null;

        if (invite != null) {
            invite.setCode(event.getNewVanityCode());
            SQLSession.getSqlConnector().getSqlWorker().updateEntity(invite);
        } else {
            event.getGuild().retrieveVanityInvite().onErrorMap(throwable -> null).queue(vanityInvite ->
                    SQLSession.getSqlConnector().getSqlWorker().updateEntity(new Invite(event.getGuild().getId(), event.getGuild().getOwnerId(), vanityInvite.getUses(), event.getNewVanityCode())));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByEvent(event, "logging.banned", event.getUser().getAsMention()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.BAN).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.USER_BAN, event.getUser()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberunban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByEvent(event, "logging.unbanned", event.getUser().getAsMention()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.USER_UNBAN, event.getUser()));
    }

    //endregion

    //region Member

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
            return;

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());

        if (SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberjoin").getBooleanValue()) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(LanguageService.getByEvent(event, "logging.joined.default", event.getUser().getAsMention(), TimeFormat.DATE_TIME_SHORT.format(event.getUser().getTimeCreated()), TimeFormat.RELATIVE.format(event.getUser().getTimeCreated())));

            wm.addEmbeds(we.build());
            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.SERVER_JOIN, event.getUser()));
        }

        if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER) && SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_invite").getBooleanValue()) {

            WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

            wm2.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm2.setUsername(Data.getBotName() + "-InviteLogs");

            if (event.getUser().isBot()) {
                event.getGuild().retrieveAuditLogs().type(ActionType.BOT_ADD).limit(1).queue(auditLogEntries -> {
                    if (auditLogEntries.isEmpty()) {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()));
                        return;
                    }
                    AuditLogEntry entry = auditLogEntries.get(0);

                    if (entry.getUser() == null) {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()));
                        return;
                    }

                    if (entry.getTargetId().equals(event.getUser().getId())) {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.found", event.getUser().getAsMention(), entry.getUser().getAsMention()));
                    } else {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.bot.notFound", event.getUser().getAsMention()));
                    }
                });
            } else {
                InviteContainer inviteContainer = InviteContainerManager.getRightInvite(event.getGuild());
                if (inviteContainer != null) {
                    inviteContainer.setUses(inviteContainer.getUses() + 1);
                    if (inviteContainer.isVanity()) {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.vanity", event.getUser().getAsMention()));
                    } else {
                        wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.default", event.getUser().getAsMention(), "<@" + inviteContainer.getCreatorId() + ">", inviteContainer.getCode(), inviteContainer.getUses()));
                    }
                    InviteContainerManager.addInvite(inviteContainer);
                } else {
                    wm2.append(LanguageService.getByEvent(event, "logging.joined.invite.notFound", event.getMember().getAsMention()));
                }
            }

            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm2.build(), event.getGuild(), LogTyp.SERVER_INVITE, event.getUser()));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event.getMember() != null) {
            we.setDescription(LanguageService.getByEvent(event, "logging.left.default", event.getUser().getAsMention(), TimeFormat.DATE_TIME_SHORT.format(event.getMember().getTimeJoined())));
        } else {
            we.setDescription(LanguageService.getByEvent(event, "logging.left.slim", event.getUser().getAsMention()));
        }

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.KICK).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.SERVER_LEAVE, event.getUser()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
        super.onGuildMemberUpdateTimeOut(event);


        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_timeout").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        AuditLogPaginationAction paginationAction = event.getGuild().retrieveAuditLogs().user(event.getUser()).type(ActionType.MEMBER_UPDATE).limit(1);
        if (event.getNewTimeOutEnd() != null) {
            if (paginationAction.isEmpty()) {
                we.setDescription(LanguageService.getByEvent(event, "logging.timeout.started",
                        event.getUser().getAsMention(),
                        TimeFormat.DATE_TIME_SHORT.format(event.getNewTimeOutEnd())));
            } else {
                AuditLogEntry auditLogEntry = paginationAction.getFirst();
                we.setDescription(LanguageService.getByEvent(event, "logging.timeout.updated",
                        event.getUser().getAsMention(),
                        (auditLogEntry.getReason() == null ? "Couldn't find reason" : auditLogEntry.getReason()),
                        (auditLogEntry.getUser() != null ? auditLogEntry.getUser().getAsMention() : LanguageService.getByGuild(event.getGuild(), "label.unknown")),
                        TimeFormat.DATE_TIME_SHORT.format(event.getNewTimeOutEnd())));
            }
        } else {
            we.setDescription(LanguageService.getByEvent(event, "logging.timeout.ended",
                    event.getUser().getAsMention(),
                    TimeFormat.DATE_TIME_SHORT.format(event.getOldTimeOutEnd())));
        }

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ELSE, event.getMember()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_nickname").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event.getNewNickname() == null) {
            we.setDescription(LanguageService.getByEvent(event, "logging.nickname.reset", event.getUser().getAsMention(), event.getOldNickname()));
        } else {
            we.setDescription(LanguageService.getByEvent(event, "logging.nickname.changed", event.getUser().getAsMention(), event.getNewNickname(), (event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName())));
        }

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.NICKNAME_CHANGE, event.getEntity(), event.getOldNickname(), event.getNewNickname()));
    }

    //endregion

    //region Voice


    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        super.onGuildVoiceUpdate(event);
        if (event.getChannelLeft() == null) {
            if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicejoin").getBooleanValue())
                return;


            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.join", event.getEntity().getUser().getAsMention(), event.getChannelJoined().getAsMention()));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_JOIN, event.getEntity(), event.getChannelJoined()));
        } else if (event.getChannelJoined() == null) {
            if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voiceleave").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.leave", event.getEntity().getUser().getAsMention(), event.getChannelLeft().getAsMention()));

            AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_VOICE_KICK).limit(5).stream().filter(auditLogEntry ->
                    auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

            if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_LEAVE, event.getEntity(), event.getChannelLeft()));
        } else {
            if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicemove").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getEffectiveName(), event.getEntity().getEffectiveAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(LanguageService.getByEvent(event, "logging.voicechannel.move", event.getEntity().getUser().getAsMention(), event.getChannelLeft().getAsMention(), event.getChannelJoined().getAsMention()));

            AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).limit(5).stream().filter(auditLogEntry ->
                    auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

            if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByEvent(event, "label.actor") + "**", entry.getUser().getAsMention()));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.VC_MOVE, event.getEntity(), event.getChannelLeft(), event.getChannelJoined()));
        }
    }

    //endregion

    //region Channel

    /**
     * @inheritDoc
     */
    @Override
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {

        if (event.getChannelType().isAudio()) {
            if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicechannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();
            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.update.voice", event.getChannel().getAsMention()));

            AuditLogEntry entry;

            if (event instanceof ChannelCreateEvent) {
                we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.create.voice", event.getChannel().getAsMention()));
                entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).limit(5).stream().filter(auditLogEntry ->
                        auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
            } else if (event instanceof ChannelDeleteEvent) {
                we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.delete.voice", event.getChannel().getName()));
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

            if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessage(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));
        } else if (event.getChannelType().isMessage()) {
            if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_textchannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.update.chat", event.getChannel().getAsMention()));
            we.setDescription(":house: **TextChannel updated:** " + event.getChannel().getAsMention());

            AuditLogEntry entry;

            if (event instanceof ChannelCreateEvent) {
                we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.create.chat", event.getChannel().getAsMention()));
                entry = event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).limit(5).stream().filter(auditLogEntry ->
                        auditLogEntry.getTargetIdLong() == event.getChannel().getIdLong()).findFirst().orElse(null);
            } else if (event instanceof ChannelDeleteEvent) {
                we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.channel.delete.chat", event.getChannel().getName()));
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

            if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessage(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));

        }
    }

    //endregion

    //region Role

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleadd").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();

        for (Role r : event.getRoles()) {
            finalString.append(":white_check_mark: ").append(r.getName()).append("\n");
        }

        we.setDescription(LanguageService.getByGuild(event.getGuild(),"logging.member", event.getMember().getAsMention()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.roles") + ":**", finalString.toString()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), null, new ArrayList<>(event.getRoles())));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) || !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleremove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getEffectiveAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();
        for (Role r : event.getRoles()) {
            finalString.append(":no_entry: ").append(r.getName()).append("\n");
        }

        we.setDescription(LanguageService.getByGuild(event.getGuild(),"logging.member", event.getMember().getAsMention()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.roles") + ":**", finalString.toString()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getMember().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), new ArrayList<>(event.getRoles()), null));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecreate").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.create", event.getRole().getName()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_CREATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), true, false, false, false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roledelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.delete", event.getRole().getName()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_DELETE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, true, false, false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolename").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldName") + "**", event.getOldName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newName") + "**", event.getNewName()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldName(), event.getNewName()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolemention").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldMentionable") + "**", event.getOldValue().toString()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newMentionable") + "**", event.getNewValue().toString()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, false, true));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolehoisted").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldHoist") + "**", event.getOldValue().toString()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newHoist") + "**", event.getNewValue().toString()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, true, false));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolepermission").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()));

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

        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newPermissions") + "**", finalString.toString()));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldPermissions(), event.getNewPermissions()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecolor").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername(Data.getBotName() + "-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.role.update", event.getRole().getName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.oldColor") + "**", (event.getOldColor() != null ? event.getOldColor() : Color.gray).getRGB() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.newColor") + "**", (event.getNewColor() != null ? event.getNewColor() : Color.gray).getRGB() + ""));

        AuditLogEntry entry = event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(5).stream().filter(auditLogEntry ->
                auditLogEntry.getTargetIdLong() == event.getRole().getIdLong()).findFirst().orElse(null);

        if (entry != null && entry.getUser() != null) we.addField(new WebhookEmbed.EmbedField(true, "**" + LanguageService.getByGuild(event.getGuild(), "label.actor") + "**", entry.getUser().getAsMention()));

        wm.addEmbeds(we.build());

        Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), (event.getOldColor() != null ? event.getOldColor() : Color.gray), (event.getNewColor() != null ? event.getNewColor() : Color.gray)));
    }

    //endregion

    //region Message

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {

        if (!SQLSession.getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !SQLSession.getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_messagedelete").getBooleanValue())
            return;

        User user = ArrayUtil.getUserFromMessageList(event.getMessageId());

        if (user != null) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm.setUsername(Data.getBotName() + "-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(user.getName(), user.getEffectiveAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.getAdvertisement(), event.getGuild().getIconUrl()));
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
                        wm.append(LanguageService.getByGuild(event.getGuild(), "logging.message.attachmentFailed", attachment.getFileName()) + "\n");
                    }
                }
                wm.append(LanguageService.getByGuild(event.getGuild(), "logging.message.attachmentNotice") + "\n");
            }

            we.setDescription(LanguageService.getByGuild(event.getGuild(), "logging.message.deleted", user.getAsMention(), event.getChannel().getAsMention(),
                    message != null ? message.getContentRaw().length() >= 650 ?
                            LanguageService.getByGuild(event.getGuild(), "logging.message.tooLong") :
                            message.getContentRaw() : ""));

            if (message != null && message.getContentRaw().length() >= 650)
                wm.addFile("message.txt", message.getContentRaw().getBytes(StandardCharsets.UTF_8));

            wm.addEmbeds(we.build());

            Webhook webhook = SQLSession.getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(webhook.getWebhookId()), webhook.getToken(), wm.build(), event.getGuild(), LogTyp.MESSAGE_DELETE, user));
        }
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
            InviteContainer inv = new InviteContainer(event.getInvite().getInviter().getId(), event.getGuild().getId(), event.getInvite().getCode(), event.getInvite().getUses(), false);
            InviteContainerManager.addInvite(inv);
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

        InviteContainerManager.removeInvite(event.getGuild().getId(), event.getCode());
    }

    //endregion
}