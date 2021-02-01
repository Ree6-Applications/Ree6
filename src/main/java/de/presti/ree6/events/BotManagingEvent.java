package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotState;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Random;

public class BotManagingEvent extends ListenerAdapter {

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotInfo.state = BotState.STARTED;
        System.out.println("Boot up finished!");

        Main.insance.createCheckerThread();

        BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Server", Activity.ActivityType.WATCHING);
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        if(!Main.sqlWorker.hasWeclomeSetuped(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");
        wmb.setContent("Welcome " + event.getUser().getAsMention() + "!\nWe wish you a great time on " + event.getGuild().getName());

        String[] info = Main.sqlWorker.getWelcomewebhook(event.getGuild().getId());

        Webhook.sendWebhook(wmb.build(), Long.parseLong(info[0]), info[1]);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        if(!Main.cm.perform(event.getMember(), event.getMessage().getContentRaw(), event.getMessage(), event.getChannel())) {
            if(!ArrayUtil.timeout.contains(event.getMember())) {


                try {
                    Main.sqlWorker.addXP(event.getGuild().getId(), event.getAuthor().getId(), new Random().nextInt(25) + 1);
                } catch (SQLException throwables) {}

                ArrayUtil.timeout.add(event.getMember());

                new Thread(() -> {

                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {}

                    ArrayUtil.timeout.remove(event.getMember());

                }).start();

            }
        }

        if(!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
            ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage().getContentRaw());
        }

        if(!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
            ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
        }
    }
}
