package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.invitelogger.InviteContainer;
import de.presti.ree6.invitelogger.InviteContainerManager;
import de.presti.ree6.logger.LoggerMemberData;
import de.presti.ree6.logger.LoggerMessage;
import de.presti.ree6.logger.LoggerRoleData;
import de.presti.ree6.logger.LoggerVoiceData;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.TimeUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNSFWEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNewsEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdatePermissionsEvent;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@SuppressWarnings("CommentedOutCode")
public class LoggingEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {


        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());

        if (Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_memberjoin").getBooleanValue()) {
            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setThumbnailUrl(event.getUser().getAvatarUrl());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());
            we.setDescription(event.getUser().getAsMention() + " **joined the Server.**\n:timer: Age of the Account:\n``" + event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "``\n**" + TimeUtil.getFormattedDate(TimeUtil.getDifferenceBetween(event.getUser().getTimeCreated().toLocalDateTime(), LocalDateTime.now())) + "**");

            wm.addEmbeds(we.build());
            Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.SERVER_JOIN));
        }

        if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER) && Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_invite").getBooleanValue()) {

            WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

            wm2.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm2.setUsername("Ree6InviteLogs");

            InviteContainer inviteContainer = InviteContainerManager.getRightInvite(event.getGuild());

            if (inviteContainer != null) {
                inviteContainer.setUses(inviteContainer.getUses() + 1);
                wm2.append(event.getUser().getAsMention() + " **has been invited by** <@" + inviteContainer.getCreatorid() + "> (Code: " + inviteContainer.getCode() + ", Uses: " + inviteContainer.getUses() + ")");
                InviteContainerManager.addInvite(inviteContainer, event.getGuild().getId());
            } else {
                wm2.append("Couldn't find out how " + event.getMember().getAsMention() + " joined :C");
            }

            Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm2.build(), LoggerMessage.LogTyp.SERVER_INVITE));
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_memberleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getUser().getAsMention() + " **left the Server.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.SERVER_LEAVE));
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {


        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_memberban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_departure: " + event.getUser().getAsMention() + " **banned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.USER_BAN));
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_memberunban").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":airplane_arriving: " + event.getUser().getAsMention() + " **unbanned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.USER_UNBAN));
    }


    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_nickname").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event.getNewNickname() == null) {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been reseted.\n**Old Nickname:**\n" + event.getOldNickname());
        } else {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been changed.\n**New Nickname:**\n" + event.getNewNickname() + "\n**Old Nickname:**\n" + (event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName()));
        }

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getEntity(), event.getOldNickname(), event.getNewNickname()),LoggerMessage.LogTyp.NICKNAME_CHANGE));
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_voicejoin").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **joined the Voicechannel** ``" + event.getChannelJoined().getName() + "``");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelJoined(), LoggerVoiceData.LoggerVoiceTyp.JOIN), LoggerMessage.LogTyp.VC_JOIN));
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_voicemove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **switched the Voicechannel from** ``" + event.getChannelLeft().getName() + "`` **to** ``" + event.getChannelJoined().getName() + "``**.**");

        wm.addEmbeds(we.build());

        String[] info = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(info[0]), info[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelLeft(), event.getChannelJoined(), LoggerVoiceData.LoggerVoiceTyp.MOVE), LoggerMessage.LogTyp.VC_MOVE));
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_voiceleave").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(event.getEntity().getUser().getAsMention() + " **left the Voicechannel ** ``" + event.getChannelLeft().getName() + "``");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerVoiceData(event.getEntity(), event.getChannelLeft(), LoggerVoiceData.LoggerVoiceTyp.LEAVE), LoggerMessage.LogTyp.VC_LEAVE));
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_roleadd").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();

        for (Role r : event.getRoles()) {
            finalString.append(":white_check_mark: ").append(r.getName()).append("\n");
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString.toString()));
        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getMember(), null, new ArrayList<Role>(event.getRoles())), LoggerMessage.LogTyp.MEMBERROLE_CHANGE));
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_roleremove").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        StringBuilder finalString = new StringBuilder();
        for (Role r : event.getRoles()) {
            finalString.append(":no_entry: ").append(r.getName()).append("\n");
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString.toString()));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerMemberData(event.getMember(), new ArrayList<Role>(event.getRoles()), null), LoggerMessage.LogTyp.MEMBERROLE_CHANGE));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onGenericVoiceChannel(@Nonnull GenericVoiceChannelEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_voicechannel").getBooleanValue())
            return;

        boolean gay = true;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();
        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event instanceof VoiceChannelCreateEvent) {
            we.setDescription(":house: **VoiceChannel created:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof VoiceChannelDeleteEvent) {
            we.setDescription(":house: **VoiceChannel deleted:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof VoiceChannelUpdateNameEvent) {
            we.setDescription(":house: **VoiceChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", ((VoiceChannelUpdateNameEvent) event).getOldName()));
            we.addField(new WebhookEmbed.EmbedField(true, "**New name**", ((VoiceChannelUpdateNameEvent) event).getNewName()));
        } else if (event instanceof VoiceChannelUpdatePermissionsEvent) {
            StringBuilder finalString = new StringBuilder();

            for (Role r : ((VoiceChannelUpdatePermissionsEvent) event).getChangedRoles()) {
                finalString.append("\n").append(r.getAsMention());
            }

            we.setDescription(":house: **Voicechannel Permissions updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Updated**", finalString.toString()));
        } else {
            gay = false;
        }

        wm.addEmbeds(we.build());

        if (gay) {
            String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
            Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.CHANNELDATA_CHANGE));
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onGenericTextChannel(@Nonnull GenericTextChannelEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_textchannel").getBooleanValue())
            return;

        boolean gay = true;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());

        if (event instanceof TextChannelCreateEvent) {
            we.setDescription(":house: **TextChannel created:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof TextChannelDeleteEvent) {
            we.setDescription(":house: **TextChannel deleted:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof TextChannelUpdateNameEvent) {
            we.setDescription(":house: **TextChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", ((TextChannelUpdateNameEvent) event).getOldName()));
            we.addField(new WebhookEmbed.EmbedField(true, "**New name**", ((TextChannelUpdateNameEvent) event).getNewName()));
        } else if (event instanceof TextChannelUpdateNewsEvent) {
            we.setDescription(":house: **TextChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**News**", ((TextChannelUpdateNewsEvent) event).getNewValue() + ""));
        } else if (event instanceof TextChannelUpdateNSFWEvent) {
            we.setDescription(":house: **TextChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**NSFW**", ((TextChannelUpdateNSFWEvent) event).getNewValue() + ""));
        } else if (event instanceof TextChannelUpdatePermissionsEvent) {
            StringBuilder finalString = new StringBuilder();

            for (Role r : ((TextChannelUpdatePermissionsEvent) event).getChangedRoles()) {
                finalString.append("\n").append(r.getAsMention());
            }

            we.setDescription(":house: **TextChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Updated**", finalString.toString()));
        } else {
            gay = false;
        }

        wm.addEmbeds(we.build());

        if (gay) {
            String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
            Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.CHANNELDATA_CHANGE));
        }

    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolecreate").getBooleanValue())
            return;


        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been created.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), true, false, false, false),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_roledelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been deleted.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, true, false, false),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolename").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", event.getOldName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**New name**", event.getNewName()));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getOldName(), event.getNewName()),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolemention").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, false, false, true),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) && !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolehoisted").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getRole().getName(), false, false, true, false),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) && !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolepermission").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName()+ "`` **has been updated.**");

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

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), event.getOldPermissions(), event.getNewPermissions()),LoggerMessage.LogTyp.ROLEDATA_CHANGE));
    }

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_rolecolor").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (event.getOldColor() != null ? event.getOldColor() : Color.gray).getRGB() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New color**", (event.getNewColor() != null ? event.getNewColor() : Color.gray).getRGB() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), new LoggerRoleData(event.getRole().getIdLong(), (event.getOldColor() != null ? event.getOldColor() : Color.gray), (event.getNewColor() != null ? event.getNewColor() : Color.gray)), LoggerMessage.LogTyp.ROLEDATA_CHANGE));
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

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()) || !Main.sqlWorker.getSetting(event.getGuild().getId(), "logging_messagedelete").getBooleanValue())
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsTag(), ArrayUtil.getUserFromMessageList(event.getMessageId()).getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " - " + Data.advertisement, event.getGuild().getIconUrl()));
        we.setTimestamp(Instant.now());
        we.setDescription(":wastebasket: **Message of " + ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsMention() + " in " + event.getTextChannel().getAsMention() + " has been deleted.**\n" + ((ArrayUtil.getMessageFromMessageList(event.getMessageId()).length() > 700) ? "Too long to display!" : ArrayUtil.getMessageFromMessageList(event.getMessageId())));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogWebhook(event.getGuild().getId());
        Main.loggerQueue.add(new LoggerMessage(event.getGuild(), Long.parseLong(infos[0]), infos[1], wm.build(), LoggerMessage.LogTyp.MESSAGE_DELETE));
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
        //Too much Spam
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {

        if (!event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        InviteContainerManager.removeInvite(event.getGuild().getId(), event.getCode());
    }

}