package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.addons.impl.ChatProtector;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotState;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class OtherEvents extends ListenerAdapter {

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotInfo.state = BotState.STARTED;
        Main.getInstance().getLogger().info("Boot up finished!");

        Main.getInstance().getCommandManager().addSlashCommand();

        Main.getInstance().createCheckerThread();

        BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Guilds", Activity.ActivityType.WATCHING);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().createSettings(event.getGuild().getId());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().deleteAllData(event.getGuild().getId());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId()))
            return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");
        wmb.setContent((Main.getInstance().getSqlConnector().getSqlWorker().getMessage(event.getGuild().getId())).replaceAll("%user_name%", event.getMember().getUser().getName()).replaceAll("%user_mention%", event.getMember().getUser().getAsMention()).replaceAll("%guild_name%", event.getGuild().getName()));

        String[] info = Main.getInstance().getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId());

        Webhook.sendWebhook(null, wmb.build(), Long.parseLong(info[0]), info[1]);
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if (!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
        }
        super.onGuildVoiceJoin(event);
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

            int addxp = 0;

            for (int i = 1; i <= min; i++) {
                addxp += RandomUtils.random.nextInt(9) + 1;
            }

            Main.getInstance().getSqlConnector().getSqlWorker().addVoiceXP(event.getGuild().getId(), event.getMember().getUser().getId(), addxp);

            AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());

        }
        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (event.getMember() != event.getGuild().getSelfMember())
            return;

        if (!event.isGuildDeafened()) {
            event.getMember().deafen(true).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromGuild() && event.isFromType(ChannelType.TEXT)) {
            if (!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage());
            }

            if (!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
            }

            if (event.getAuthor().isBot())
                return;

            if (ChatProtector.hasChatProtector(event.getGuild().getId())) {
                if (ChatProtector.checkMessage(event.getGuild().getId(), event.getMessage().getContentRaw())) {
                    Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                    event.getChannel().sendMessage("You can't write that!").queue();
                    return;
                }
            }

            if (!Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getTextChannel(), null)) {

                if (!event.getMessage().getMentionedUsers().isEmpty() && event.getMessage().getMentionedUsers().contains(BotInfo.botInstance.getSelfUser())) {
                    event.getChannel().sendMessage("Usage " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "chatprefix").getStringValue() + "help").queue();
                }

                if (!ArrayUtil.timeout.contains(event.getMember())) {

                    Main.getInstance().getSqlConnector().getSqlWorker().addChatXP(event.getGuild().getId(), event.getAuthor().getId(), (long)RandomUtils.random.nextInt(25) + 1);

                    ArrayUtil.timeout.add(event.getMember());

                    new Thread(() -> {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ignored) {
                            LoggerImpl.log("OtherEvents", "User cool-down Thread interrupted!");
                            Thread.currentThread().interrupt();
                        }

                        ArrayUtil.timeout.remove(event.getMember());

                    }).start();
                }

                AutoRoleHandler.handleChatLevelReward(event.getGuild(), event.getMember());
            }
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;

        event.deferReply(true).complete();

        Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), null, null, event.getTextChannel(), event);
    }
}
