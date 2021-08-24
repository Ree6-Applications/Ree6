package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;

import de.presti.ree6.addons.impl.ChatProtector;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotState;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.AutoRoleHandler;
import de.presti.ree6.utils.TimeUtil;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nonnull;
import java.util.*;

public class OtherEvents extends ListenerAdapter {

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotInfo.state = BotState.STARTED;
        System.out.println("Boot up finished!");

        Main.instance.createCheckerThread();

        BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Guilds", Activity.ActivityType.WATCHING);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Main.sqlWorker.createSettings(event.getGuild().getId());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        Main.sqlWorker.deleteAllMyData(event.getGuild().getId());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if(!Main.sqlWorker.hasWelcomeSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");
        wmb.setContent((Main.sqlWorker.getMessage(event.getGuild().getId())).replaceAll("%user_name%", event.getMember().getUser().getName()).replaceAll("%user_mention%", event.getMember().getUser().getAsMention()).replaceAll("%guild_name%", event.getGuild().getName()));

        String[] info = Main.sqlWorker.getWelcomeWebhook(event.getGuild().getId());

        Webhook.sendWebhook(wmb.build(), Long.parseLong(info[0]), info[1]);
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if(!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
        }
        super.onGuildVoiceJoin(event);
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if(ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

            int addxp = 0;

            for(int i = 1; i <= min; i++) {
                addxp += new Random().nextInt(9) + 1;
            }

            Main.sqlWorker.addXPVC(event.getGuild().getId(), event.getMember().getUser().getId(), addxp);

            AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());

        }
        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        if(!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
            ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage().getContentRaw());
        }

        if(!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
            ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
        }

        if(event.getAuthor().isBot())
            return;

        if(ChatProtector.hasChatProtector(event.getGuild().getId())) {
            if(ChatProtector.checkMessage(event.getGuild().getId(), event.getMessage().getContentRaw())) {
                event.getMessage().delete().queue();
                event.getChannel().sendMessage("You can't write that!").queue();
                return;
            }
        }

        if(!Main.commandManager.perform(event.getMember(), event.getMessage().getContentRaw(), event.getMessage(), event.getChannel(), null)) {

            if(!event.getMessage().getMentionedUsers().isEmpty() && event.getMessage().getMentionedUsers().contains(BotInfo.botInstance.getSelfUser())) {
                event.getChannel().sendMessage("Usage ree!help").queue();
            }

            if(!ArrayUtil.timeout.contains(event.getMember())) {

                Main.sqlWorker.addXP(event.getGuild().getId(), event.getAuthor().getId(), new Random().nextInt(25) + 1);

                ArrayUtil.timeout.add(event.getMember());

                    new Thread(() -> {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ignored) {}

                        ArrayUtil.timeout.remove(event.getMember());

                    }).start();
                }

                AutoRoleHandler.handleChatLevelReward(event.getGuild(), event.getMember());
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event)
    {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;

        event.deferReply(true).queue();

        MessageBuilder messageBuilder = new MessageBuilder();

        if (event.getOption("target") != null) messageBuilder.mentionUsers(event.getOption("user").getAsUser().getId());

        messageBuilder.setContent("ree!" + event.getName() + " " + (event.getOption("target") != null ? event.getOption("user").getAsMember().getAsMention() : event.getOption("name") != null ? event.getOption("name").getAsString() : ""));

        Message message = messageBuilder.build();

        Main.commandManager.perform(event.getMember(), message.getContentRaw(), message, event.getTextChannel(), event.getHook().setEphemeral(true));
    }
}
