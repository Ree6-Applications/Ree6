package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.*;
import de.presti.ree6.invtielogger.InviteContainer;
import de.presti.ree6.invtielogger.InviteContainerManager;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.TimeUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
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
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Logging extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        //Future InviteLogger
        //event.getGuild().retrieveInvites().queue(invites -> invites.get(0).getUses());

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(event.getUser().getAsMention() + " **joined the Server.**\n:timer: Age of the Account:\n``" + event.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "``\n**" + TimeUtil.getFormattedDate(TimeUtil.getDifferenceBetween(event.getUser().getTimeCreated().toLocalDateTime(), LocalDateTime.now())) + "**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);

        WebhookMessageBuilder wm2 = new WebhookMessageBuilder();

        wm2.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm2.setUsername("Ree6InviteLogs");

        InviteContainer inv = InviteContainerManager.getRightInvite(event.getGuild());

        if(inv != null) {
            wm2.append(event.getMember().getAsMention() + " **has beend invited by** " + event.getGuild().getMemberById(inv.getCreatorid()) + " (Code: " + inv.getCode() + ", Uses: " + inv.getUses() + ")");
        } else {
            wm2.append("Couldnt find out how " + event.getMember().getAsMention() + " joined :C");
        }
        Webhook.sendWebhook(wm2.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

        String finalString = "";

        int sex = 0;
        for (Role r : event.getRoles()) {
            if(event.getRoles().size() - 1 == sex) {
                finalString += ":white_check_mark: " + r.getName();
            } else {
                finalString += ":white_check_mark: " + r.getName() + "\n";
            }
            sex++;
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString));
        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
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
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

        we.setDescription(":pencil2: **Message send by** " + event.getMember().getUser().getAsMention() + " **in** " + event.getChannel().getAsMention() + " **has been edited.**\n**Old**\n```" + ArrayUtil.getMessageFromMessageList(event.getMessageId()) + "```\n**New**\n```" + event.getMessage().getContentRaw() + "```");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);

        ArrayUtil.updateMessage(event.getMessageId(), event.getMessage().getContentRaw());

    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

        String finalString = "";

        int sex = 0;
        for (Role r : event.getRoles()) {
            if(event.getRoles().size() - 1 == sex) {
                finalString += ":no_entry: " + r.getName();
            } else {
                finalString += ":no_entry: " + r.getName() + "\n";
            }
            sex++;
        }

        we.setDescription(":writing_hand: " + event.getMember().getUser().getAsMention() + " **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

        if (event.getUser().getName().equals(event.getNewNickname())) {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been reseted.\n**Old Nickname:**\n" + event.getOldNickname());
        } else {
            we.setDescription("The Nickname of " + event.getUser().getAsMention() + " has been changed.\n**New Nickname:**\n" + event.getNewNickname() + "\n**Old Nickname:**\n" + event.getOldNickname());
        }

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGenericVoiceChannel(@Nonnull GenericVoiceChannelEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        boolean gay = true;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();
        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

        if (event instanceof VoiceChannelCreateEvent) {
            we.setDescription(":house: **VoiceChannel created:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof VoiceChannelDeleteEvent) {
            we.setDescription(":house: **VoiceChannel deleted:** ``" + event.getChannel().getName() + "``");
        } else if (event instanceof VoiceChannelUpdateNameEvent) {
            we.setDescription(":house: **VoiceChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", ((VoiceChannelUpdateNameEvent) event).getOldName()));
            we.addField(new WebhookEmbed.EmbedField(true, "**New name**", ((VoiceChannelUpdateNameEvent) event).getNewName()));
        } else if (event instanceof VoiceChannelUpdatePermissionsEvent) {
            String finalString = "";

            for (Role r : ((VoiceChannelUpdatePermissionsEvent) event).getChangedRoles()) {
                finalString += "\n" + r.getAsMention();
            }

            we.setDescription(":house: **Voicechannel Permissions updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Updated**", finalString));
        } else {
            gay = false;
        }

        wm.addEmbeds(we.build());

        if(gay) {
            String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
            Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
        }

    }

    @Override
    public void onGenericTextChannel(@Nonnull GenericTextChannelEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        boolean gay = true;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));

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
            String finalString = "";

            for (Role r : ((TextChannelUpdatePermissionsEvent) event).getChangedRoles()) {
                finalString += "\n" + r.getAsMention();
            }

            we.setDescription(":house: **TextChannel updated:** ``" + event.getChannel().getName() + "``");
            we.addField(new WebhookEmbed.EmbedField(true, "**Updated**", finalString));
        } else {
            gay = false;
        }

        wm.addEmbeds(we.build());

        if(gay) {
            String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
            Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
        }

    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(event.getUser().getAsMention() + " **left the Server.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsTag(), ArrayUtil.getUserFromMessageList(event.getMessageId()).getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":wastebasket: **Message of " + ArrayUtil.getUserFromMessageList(event.getMessageId()).getAsMention() + " in " + event.getTextChannel().getAsMention() + " has been deleted.**\n" + ArrayUtil.getMessageFromMessageList(event.getMessageId()));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(event.getEntity().getUser().getAsMention() + " **joined the Voicechannel** ``" + event.getChannelJoined().getName() + "``");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildInviteCreate(@Nonnull GuildInviteCreateEvent event) {
        InviteContainer inv = new InviteContainer(event.getInvite().getInviter().getId(), event.getGuild().getId(), event.getInvite().getCode(), event.getInvite().getUses());
        InviteContainerManager.addInvite(inv, event.getGuild().getId());
        //Too much Spam
    }

    @Override
    public void onGuildInviteDelete(@Nonnull GuildInviteDeleteEvent event) {
        InviteContainerManager.removeInvite(event.getGuild().getId(), event.getCode());
    }

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been created.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been deleted.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleUpdateName(@Nonnull RoleUpdateNameEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", event.getOldName()));
        we.addField(new WebhookEmbed.EmbedField(true, "**New name**", event.getNewName()));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleUpdateMentionable(@Nonnull RoleUpdateMentionableEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleUpdateHoisted(@Nonnull RoleUpdateHoistedEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", event.getOldValue() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", event.getNewValue() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleUpdatePermissions(@Nonnull RoleUpdatePermissionsEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");

        String finalString = "";

        boolean b = false;
        for (Permission r : event.getNewPermissions()) {
            if(!event.getOldPermissions().contains(r)) {
                if(b) {
                    finalString += "\n:white_check_mark: " + r.getName();
                } else {
                    finalString += ":white_check_mark: " + r.getName();
                    b = true;
                }
            }
        }

        for (Permission r : event.getOldPermissions()) {
            if(!event.getNewPermissions().contains(r)) {
                if(b) {
                    finalString += "\n:no_entry: " + r.getName();
                } else {
                    finalString += ":no_entry: " + r.getName();
                    b = true;
                }
            }
        }

        we.addField(new WebhookEmbed.EmbedField(true, "**New permissions**", finalString));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onRoleUpdateColor(@Nonnull RoleUpdateColorEvent event) {
        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getGuild().getName(), event.getGuild().getIconUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":family_mmb: ``" + event.getRole().getName() + "`` **has been updated.**");
        we.addField(new WebhookEmbed.EmbedField(true, "**Old color**", Objects.requireNonNull(event.getOldColor()).getRGB() + ""));
        we.addField(new WebhookEmbed.EmbedField(true, "**New color**", Objects.requireNonNull(event.getNewColor()).getRGB() + ""));

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(event.getEntity().getUser().getAsMention() + " **left the Voicechannel ** ``" + event.getChannelLeft().getName() + "``");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getEntity().getUser().getAsTag(), event.getEntity().getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(event.getEntity().getUser().getAsMention() + " **switched the Voicechannel from** ``" + event.getChannelLeft().getName() + "`` **to** ``" + event.getChannelJoined().getName() + "``**.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":airplane_departure: " + event.getUser().getAsMention() + " **banned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {

        if (!Main.sqlWorker.hasLogSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wm = new WebhookMessageBuilder();

        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wm.setUsername("Ree6Logs");

        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
        we.setColor(Color.BLACK.getRGB());
        we.setThumbnailUrl(event.getUser().getAvatarUrl());
        we.setAuthor(new WebhookEmbed.EmbedAuthor(event.getUser().getAsTag(), event.getUser().getAvatarUrl(), null));
        we.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName() + " • today at " + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()), event.getGuild().getIconUrl()));
        we.setDescription(":airplane_arriving: " + event.getUser().getAsMention() + " **unbanned.**");

        wm.addEmbeds(we.build());

        String[] infos = Main.sqlWorker.getLogwebhook(event.getGuild().getId());
        Webhook.sendWebhook(wm.build(), Long.parseLong(infos[0]), infos[1]);
    }
}