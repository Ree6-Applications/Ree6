package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.logger.events.LogMessage;
import de.presti.ree6.logger.events.LogTyp;
import de.presti.ree6.logger.events.implentation.LogMessageMember;
import de.presti.ree6.logger.events.implentation.LogMessageRole;
import de.presti.ree6.logger.events.implentation.LogMessageUser;
import de.presti.ree6.logger.events.implentation.LogMessageVoice;
import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.logger.invite.InviteContainerManager;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.main.Main;
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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.annotation.Nonnull;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;

public class LoggingEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
            return;

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());

        if (Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberjoin").getBooleanValue()) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setThumbnailUrl(event.getUser().getAvatarUrl());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(event.getUser().getAsMention() + " **joined the Server.**\n:timer: Age of the Account:\n**" + TimeFormat.DATE_TIME_SHORT.format(event.getUser().getTimeCreated()) + "**\n**" + TimeFormat.RELATIVE.format(event.getUser().getTimeCreated()) + "**");

            wm.addEmbeds(we.build());
            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.SERVER_JOIN, event.getUser()));
        }

        if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER) && Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_invite").getBooleanValue()) {

            WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

            wm2.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm2.setUsername("Ree6-InviteLogs");

            InviteContainer inviteContainer = InviteContainerManager.getRightInvite(event.getGuild());

            if (event.getUser().isBot()) {
                event.getGuild().retrieveAuditLogs().type(ActionType.BOT_ADD).limit(1).queue(auditLogEntries -> {
                    if (auditLogEntries.isEmpty()) {
                        wm2.append("**We could not find out who added the Bot** " + event.getUser().getAsMention());
                        return;
                    }
                    AuditLogEntry entry = auditLogEntries.get(0);

                    if (entry.getUser() == null) {
                        wm2.append("**We could not find out who added the Bot** " + event.getUser().getAsMention());
                        return;
                    }

                    if (entry.getTargetId().equals(event.getUser().getId())) {
                        wm2.append("**The Bot** " + event.getUser().getAsMention() + " **has been invited by** <@" + entry.getUser().getId() + ">");
                    }
                });
            } else {
                if (inviteContainer != null) {
                    inviteContainer.setUses(inviteContainer.getUses() + 1);
                    wm2.append(event.getUser().getAsMention() + " **has been invited by** <@" + inviteContainer.getCreatorId() + "> (Code: " + inviteContainer.getCode() + ", Uses: " + inviteContainer.getUses() + ")");
                    InviteContainerManager.addInvite(inviteContainer);
                } else {
                    wm2.append("There was an Issue while trying to find out who Invited " + event.getMember().getAsMention() + ", please use the clear Data command!");
                }
            }

            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm2.build(), event.getGuild(), LogTyp.SERVER_INVITE, event.getUser()));
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event.getMember() != null) {
            we.setDescription(event.getUser().getAsMention() + " **left the Server.**\n:timer: Joined :\n**" + TimeFormat.DATE_TIME_SHORT.format(event.getMember().getTimeJoined()));
        } else {
            we.setDescription(event.getUser().getAsMention() + " **left the Server.**");
        }

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.SERVER_LEAVE, event.getUser()));
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_departure: " + event.getUser().getAsMention() + " **banned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.USER_BAN, event.getUser()));
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberunban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_arriving: " + event.getUser().getAsMention() + " **unbanned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.USER_UNBAN, event.getUser()));
    }


    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_nickname").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event.getNewNickname() == null) {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been reset.\n**Old Nickname:**\n" + event.getOldNickname());
        } else {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been changed.\n**New Nickname:**\n" + event.getNewNickname() + "\n**Old Nickname:**\n" + (event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName()));
        }

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.NICKNAME_CHANGE, event.getEntity(), event.getOldNickname(), event.getNewNickname()));
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicejoin").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **joined the Voicechannel** " + event.getChannelJoined().getAsMention());

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.VC_JOIN, event.getEntity(), event.getChannelJoined()));
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicemove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **switched the Voicechannel from** " + event.getChannelLeft().getAsMention() + " **to** " + event.getChannelJoined().getAsMention() + "**.**");

        wm.addEmbeds(we.build());

        String[] info = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(info[0]), info[1], wm.build(), event.getGuild(), LogTyp.VC_MOVE, event.getEntity(), event.getChannelLeft(), event.getChannelJoined()));
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voiceleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **left the Voicechannel ** " + event.getChannelLeft().getAsMention());

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageVoice(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.VC_LEAVE, event.getEntity(), event.getChannelLeft()));
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleadd").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();

        for (Role r : event.getRoles()) {
            finalString.append(":white_check_mark: ").append(r.getName()).append("\n");
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString.toString()));
        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), null, new ArrayList<>(event.getRoles())));
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) || !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleremove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();
        for (Role r : event.getRoles()) {
            finalString.append(":no_entry: ").append(r.getName()).append("\n");
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString.toString()));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageMember(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.MEMBERROLE_CHANGE, event.getMember(), new ArrayList<>(event.getRoles()), null));
    }

    @Override
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {

        if (event.getChannelType().isAudio()) {
            if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicechannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();
            wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            we.setDescription(":house: **VoiceChannel updated:** " + event.getChannel().getAsMention());

            if (event instanceof ChannelCreateEvent) {
                we.setDescription(":house: **VoiceChannel created:** " + event.getChannel().getAsMention());
            } else if (event instanceof ChannelDeleteEvent) {
                we.setDescription(":house: **VoiceChannel deleted:** ``" + event.getChannel().getName() + "``");
            } else if (event instanceof ChannelUpdateNameEvent channelUpdateNameEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", channelUpdateNameEvent.getOldValue() != null
                        ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", channelUpdateNameEvent.getNewValue() != null
                        ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
            } else {
                return;
            }

            wm.addEmbeds(we.build());

            String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessage(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));
        } else if (event.getChannelType().isMessage()) {
            if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_textchannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            we.setDescription(":house: **TextChannel updated:** " + event.getChannel().getAsMention());

            if (event instanceof ChannelCreateEvent) {
                we.setDescription(":house: **TextChannel created:** " + event.getChannel().getAsMention());
            } else if (event instanceof ChannelDeleteEvent) {
                we.setDescription(":house: **TextChannel deleted:** ``" + event.getChannel().getName() + "``");
            } else if (event instanceof ChannelUpdateNameEvent channelUpdateNameEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", channelUpdateNameEvent.getOldValue() != null
                        ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", channelUpdateNameEvent.getNewValue() != null
                        ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
            } else if (event instanceof ChannelUpdateNSFWEvent channelUpdateNSFWEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**NSFW**", channelUpdateNSFWEvent.getNewValue() + ""));
            } else {
                return;
            }

            wm.addEmbeds(we.build());

            String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessage(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.CHANNELDATA_CHANGE));

        }
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecreate").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been created.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), true, false, false, false));
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roledelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been deleted.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, true, false, false));
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolename").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", event.getOldName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**New name**", event.getNewName()));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldName(), event.getNewName()));
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolemention").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, false, true));
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) &&
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolehoisted").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getRole().getName(), false, false, true, false));
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) &&
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolepermission").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");

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

        we.addField(new WebhookEmbed.EmbedField(true, "**New permissions**", finalString.toString()));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), event.getOldPermissions(), event.getNewPermissions()));
    }

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecolor").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6-Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (event.getOldColor() != null ? event.getOldColor() : Color.gray).getRGB() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New color**", (event.getNewColor() != null ? event.getNewColor() : Color.gray).getRGB() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LogMessageRole(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.ROLEDATA_CHANGE, event.getRole().getIdLong(), (event.getOldColor() != null ? event.getOldColor() : Color.gray), (event.getNewColor() != null ? event.getNewColor() : Color.gray)));
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_messagedelete").getBooleanValue())
            return;

        User user = ArrayUtil.getUserFromMessageList(event.getMessageId());

        if (user != null) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6-Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(user.getAsTag(), user.getAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            Message message = ArrayUtil.getMessageFromMessageList(event.getMessageId());

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
                        wm.append("Couldn't receive InputStream from Attachment, reason: " + exception.getMessage() + "!\n");
                    }
                }
                wm.append("The Message had Attachments, please be careful when checking them out!\n");
            }

            we.setDescription(":wastebasket: **Message of " + user.getAsMention() + " in " + event.getTextChannel().getAsMention() + " has been deleted.**\n" +
                    (message != null ? message.getContentRaw().length() >= 650 ? "Message is too long to display!" : message.getContentRaw() : ""));

            if (message != null && message.getContentRaw().length() >= 650) wm.addFile("message.txt", message.getContentRaw().getBytes(StandardCharsets.UTF_8));

            wm.addEmbeds(we.build());

            String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LogMessageUser(Long.parseLong(infos[0]), infos[1], wm.build(), event.getGuild(), LogTyp.MESSAGE_DELETE, (message != null ? message.getAuthor() :  null)));
        }
    }

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        if (event.getInvite().getInviter() != null) {
            InviteContainer inv = new InviteContainer(event.getInvite().getInviter().getId(), event.getGuild().getId(), event.getInvite().getCode(), event.getInvite().getUses());
            InviteContainerManager.addInvite(inv);
        }
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        InviteContainerManager.removeInvite(event.getGuild().getId(), event.getCode());
    }

}