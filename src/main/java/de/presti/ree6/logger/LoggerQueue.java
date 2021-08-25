package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LoggerQueue {
    final ArrayList<LoggerMessage> logs = new ArrayList<>();

    public void add(LoggerMessage lm) {
        if(!logs.contains(lm)) {
            logs.add(lm);

            if(lm.getType() == LoggerMessage.LogTyp.VC_JOIN) {
                if(lm.getM() != null) {
                    if (getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE).count() > 0) {
                        getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        wm.setUsername("Ree6Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(lm.getM().getGuild().getName(), lm.getM().getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(lm.getM().getUser().getAsMention() + " **rejoined the Voicechannel** ``" + lm.getVc().getName() + "``");

                        wm.addEmbeds(we.build());

                        lm.setWem(wm.build());
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.VC_MOVE) {
                if(lm.getM() != null) {
                    if (getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_MOVE).count() > 0) {
                        getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_MOVE).filter(loggerMessage -> loggerMessage != lm).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        wm.setUsername("Ree6Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(lm.getM().getGuild().getName(), lm.getM().getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(lm.getM().getUser().getAsMention() + " **moved through many Voicechannels and is now in** ``" + lm.getVc().getName() + "``");

                        wm.addEmbeds(we.build());

                        lm.setWem(wm.build());
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.VC_LEAVE) {
                if(lm.getM() != null) {
                    if (getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN).count() > 0) {
                        getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        wm.setUsername("Ree6Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(lm.getM().getGuild().getName(), lm.getM().getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription(lm.getM().getUser().getAsMention() + " **joined and left the Voicechannel** ``" + lm.getVc().getName() + "``");

                        wm.addEmbeds(we.build());

                        lm.setWem(wm.build());
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE) {
                if(lm.getM() != null) {
                    if (getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE).count() > 1) {
                        String oldName = ((LoggerMessage)(getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE).toArray()[0])).getNickname2();
                        getLogsByMember(lm.getM()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        lm.setNickname2(oldName);

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        wm.setUsername("Ree6Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(lm.getM().getGuild().getName(), lm.getM().getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());
                        we.setDescription("The Nickname of " + lm.getM().getAsMention() + " has been changed.\n**New Nickname:**\n" + lm.getM().getNickname() + "\n**Old Nickname:**\n" + (oldName != null ? oldName : lm.getM().getUser().getName()));

                        wm.addEmbeds(we.build());

                        lm.setWem(wm.build());
                    }
                }
            }

            new Thread(() ->{
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {}

                if(!lm.isCancel()) {
                    Webhook.sendWebhook(lm.getWem(), lm.getId(), lm.getAuthCode());
                }

                logs.remove(lm);

            }).start();

        }
    }

    public ArrayList<LoggerMessage> getLogsByMember(Member m) {
        ArrayList<LoggerMessage> sheesh = new ArrayList<>();

        for(LoggerMessage lm : logs) {
            if(lm.getM() == m) {
                sheesh.add(lm);
            }
        }

        return sheesh;
    }



}
