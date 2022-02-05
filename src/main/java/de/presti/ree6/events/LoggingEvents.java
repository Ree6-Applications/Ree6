package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.logger.events.*;
import de.presti.ree6.logger.invite.InviteContainer;
import de.presti.ree6.logger.invite.InviteContainerManager;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.TimeUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
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

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@SuppressWarnings("CommentedOutCode")
public class LoggingEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
            return;

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());

        if (Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberjoin").getBooleanValue()) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setThumbnailUrl(event.getUser().getAvatarUrl());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(event.getUser().getAsMention() + " **joined the Server.**\n:timer: Age of the Account:\n``" + event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "``\n**" + TimeUtil.getFormattedDate(TimeUtil.getDifferenceBetween(event.getUser().getTimeCreated().toLocalDateTime(), LocalDateTime.now())) + "**");

            wm.addEmbeds(we.build());
            Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerUserData(event.getUser()), LoggerMessage.LogTyp.SERVER_JOIN));
        }

        if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER) && Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_invite").getBooleanValue()) {

            WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

            wm2.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm2.setUsername("Ree6InviteLogs");

            InviteContainer inviteContainer = InviteContainerManager.getRightInvite(event.getGuild());

            if (inviteContainer != null) {
                inviteContainer.setUses(inviteContainer.getUses() + 1);
                wm2.append(event.getUser().getAsMention() + " **has been invited by** <@" + inviteContainer.getCreatorId() + "> (Code: " + inviteContainer.getCode() + ", Uses: " + inviteContainer.getUses() + ")");
                InviteContainerManager.addInvite(inviteContainer, event.getGuild().getId());
            } else {
                wm2.append("Couldn't find out how " + event.getMember().getAsMention() + " joined :C");
            }

            Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm2.build(), LoggerMessage.LogTyp.SERVER_INVITE));
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getUser().getAsMention() + " **left the Server.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerUserData(event.getUser()), LoggerMessage.LogTyp.SERVER_LEAVE));
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {


        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_departure: " + event.getUser().getAsMention() + " **banned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerUserData(event.getUser()), LoggerMessage.LogTyp.USER_BAN));
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_memberunban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_arriving: " + event.getUser().getAsMention() + " **unbanned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.USER_UNBAN));
    }


    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_nickname").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getEntity(), event.getOldNickname(), event.getNewNickname()), LoggerMessage.LogTyp.NICKNAME_CHANGE));
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicejoin").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **joined the Voicechannel** " + event.getChannelJoined().getAsMention());

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelJoined(), LoggerVoiceData.LoggerVoiceTyp.JOIN), LoggerMessage.LogTyp.VC_JOIN));
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicemove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **switched the Voicechannel from** " + event.getChannelLeft().getAsMention() + " **to** " + event.getChannelJoined().getAsMention() + "**.**");

        wm.addEmbeds(we.build());

        String[] info = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(info[0]), info[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelLeft(), event.getChannelJoined(), LoggerVoiceData.LoggerVoiceTyp.MOVE), LoggerMessage.LogTyp.VC_MOVE));
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voiceleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **left the Voicechannel ** " + event.getChannelLeft().getAsMention());

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelLeft(), LoggerVoiceData.LoggerVoiceTyp.LEAVE), LoggerMessage.LogTyp.VC_LEAVE));
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleadd").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getMember(), null, new ArrayList<>(event.getRoles())), LoggerMessage.LogTyp.MEMBERROLE_CHANGE));
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) || !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roleremove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getMember(), new ArrayList<>(event.getRoles()), null), LoggerMessage.LogTyp.MEMBERROLE_CHANGE));
    }

    @Override
    public void onGenericChannel(@Nonnull GenericChannelEvent event) {

        if (event.getChannelType().isAudio()) {
            if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_voicechannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();
            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

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
            } else if (event instanceof ChannelUpdateNameEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", ((ChannelUpdateNameEvent) event).getOldValue() != null
                        ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", ((ChannelUpdateNameEvent) event).getNewValue() != null
                        ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
            } else {
                return;
            }

            wm.addEmbeds(we.build());

            String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.CHANNELDATA_CHANGE));
        } else if(event.getChannelType().isMessage()) {
            if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                    !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_textchannel").getBooleanValue())
                return;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

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
            } else if (event instanceof ChannelUpdateNameEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", ((ChannelUpdateNameEvent) event).getOldValue() != null
                        ? ((ChannelUpdateNameEvent) event).getOldValue() : event.getChannel().getName()));
                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", ((ChannelUpdateNameEvent) event).getNewValue() != null
                        ? ((ChannelUpdateNameEvent) event).getNewValue() : event.getChannel().getName()));
            } else if (event instanceof ChannelUpdateNSFWEvent) {
                we.addField(new WebhookEmbed.EmbedField(true, "**NSFW**", ((ChannelUpdateNSFWEvent) event).getNewValue() + ""));
            } else {
                return;
            }

            wm.addEmbeds(we.build());

            String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
            Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.CHANNELDATA_CHANGE));

        }
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecreate").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been created.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), true, false, false, false), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_roledelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been deleted.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, true, false, false), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolename").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getOldName(), event.getNewName()), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolemention").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, false, false, true), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) &&
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolehoisted").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, false, true, false), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) &&
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolepermission").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getOldPermissions(), event.getNewPermissions()), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_rolecolor").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

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
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), (event.getOldColor() != null ? event.getOldColor() : Color.gray), (event.getNewColor() != null ? event.getNewColor() : Color.gray)), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }


       /* @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        if (event.getMessage().getContentRaw().isEmpty())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getAuthor().getAsTag(), event.getAuthor().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));

        we.setDescription(":pencil2: **Message send by** " + event.getMember().getUser().getAsMention() + " **in** " + event.getChannel().getAsMention() + " **has been edited.**\n**Old**\n```" + ArrayUtil.getMessageFromMessageList(event.getMessageId()) + "```\n**New**\n```" + event.getMessage().getContentRaw() + "```");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);

        ArrayUtil.updateMessage(event.getMessageId(), event.getMessage().getContentRaw());

    } */

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()) ||
                !Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "logging_messagedelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsTag(), ArrayUtil.getUserFromMessageList(event.getMessageId()).getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        Message message = ArrayUtil.getMessageFromMessageList(event.getMessageId());

        we.setDescription(":wastebasket: **Message of " + ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsMention() + " in " + event.getTextChannel().getAsMention() + " has been deleted.**\n" +
                (message != null ? message.getContentRaw().length() >= 650 ? "Message is too long to display!" : message.getContentRaw() : ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.getInstance().getSqlConnector().getSqlWorker().getLogWebhook(event.getGuild().getId());
        Main.getInstance().getLoggerQueue().add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.MESSAGE_DELETE));
    }

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        if (event.getInvite().getInviter() != null) {
            InviteContainer inv = new InviteContainer(event.getInvite().getInviter().getId(), event.getGuild().getId(), event.getInvite().getCode(), event.getInvite().getUses());
            InviteContainerManager.addInvite(inv, event.getGuild().getId());
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