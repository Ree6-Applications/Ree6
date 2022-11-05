package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.ImageCreationUtility;
import de.presti.ree6.utils.others.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Event Handler for no categorized Events.
 */
@Slf4j
public class OtherEvents extends ListenerAdapter {

    /**
     * @inheritDoc
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotWorker.setState(BotState.STARTED);
        log.info("Boot up finished!");

        Main.getInstance().getCommandManager().addSlashCommand(event.getJDA());

        BotWorker.setActivity(event.getJDA(), "ree6.de | %guilds% Servers. (%shard%)", Activity.ActivityType.PLAYING);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().createSettings(event.getGuild().getId());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().deleteAllData(event.getGuild().getId());
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
        if (channelStats != null) {
            if (channelStats.getMemberStatsChannelId() != null) {
                GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                if (guildChannel != null) {
                    guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.overallMembers") + ": " + event.getGuild().getMemberCount()).queue();
                }
            }

            event.getGuild().loadMembers().onSuccess(members -> {
                if (channelStats.getRealMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.realMembers") + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                    }
                }

                if (channelStats.getBotMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.botMembers") + ": "+ members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                    }
                }
            });
        }

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId())) return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");

        String messageContent = Main.getInstance().getSqlConnector().getSqlWorker().getMessage(event.getGuild().getId())
                .replace("%user_name%", event.getMember().getUser().getName())
                .replace("%guild_name%", event.getGuild().getName())
                .replace("%guild_member_count%", String.valueOf(event.getGuild().getMemberCount()));
        if (!Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "message_join_image").getStringValue().isBlank()) {
            try {
                messageContent = messageContent.replace("%user_mention%", event.getMember().getUser().getName());
                wmb.addFile("welcome.png", ImageCreationUtility.createJoinImage(event.getUser(),
                        Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "message_join_image").getStringValue(), messageContent));
            } catch (IOException e) {
                wmb.setContent(messageContent);
                log.error("Error while creating join image!", e);
            }
        } else {
            messageContent = messageContent.replace("%user_mention%", event.getMember().getUser().getAsMention());
            wmb.setContent(messageContent);
        }

        WebhookUtil.sendWebhook(null, wmb.build(), Main.getInstance().getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId()), false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        super.onGuildMemberRemove(event);

        ChannelStats channelStats = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "SELECT * FROM ChannelStats WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));
        if (channelStats != null) {
            if (channelStats.getMemberStatsChannelId() != null) {
                GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                if (guildChannel != null) {
                    guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.overallMembers") + ": " + event.getGuild().getMemberCount()).queue();
                }
            }

            event.getGuild().loadMembers().onSuccess(members -> {
                if (channelStats.getRealMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.realMembers") + ": " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                    }
                }

                if (channelStats.getBotMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName(LanguageService.getByGuild(event.getGuild(),"label.botMembers") + ": " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                    }
                }
            });
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
            if (!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
                ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
            }

            TemporalVoicechannel temporalVoicechannel = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));

            if (temporalVoicechannel != null) {
                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

                if (voiceChannel == null)
                    return;

                if (!temporalVoicechannel.getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
                    return;
                }

                if (voiceChannel.getParentCategory() != null) {
                    voiceChannel.getParentCategory().createVoiceChannel(LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName",
                            event.getGuild().getVoiceChannels().stream().filter(c -> c.getName().startsWith(LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName", 0)
                                    .substring(0, LanguageService.getByGuild(event.getGuild(), "label.temporalVoiceName", 0).length() - 3))).toList().size() + 1)).queue(channel -> {
                        event.getGuild().moveVoiceMember(event.getMember(), channel).queue();
                        ArrayUtil.temporalVoicechannel.add(channel.getId());
                    });
                }
            }
        } else if (event.getChannelJoined() == null) {
            if (ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
                int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

                int addXP = 0;

                for (int i = 1; i <= min; i++) {
                    addXP += RandomUtils.random.nextInt(5, 11);
                }

                VoiceUserLevel newUserLevel = Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelData(event.getGuild().getId(), event.getMember().getId());
                newUserLevel.setUser(event.getMember().getUser());
                newUserLevel.addExperience(addXP);

                Main.getInstance().getSqlConnector().getSqlWorker().addVoiceLevelData(event.getGuild().getId(), newUserLevel);

                AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());

            }

            if (ArrayUtil.isTemporalVoicechannel(event.getChannelLeft())
                    && event.getChannelLeft().getMembers().isEmpty()) {
                event.getChannelLeft().delete().queue(c -> ArrayUtil.temporalVoicechannel.remove(event.getChannelLeft().getId()));
                ArrayUtil.temporalVoicechannel.remove(event.getChannelLeft().getId());
            }
        } else {

            TemporalVoicechannel temporalVoicechannel = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(new TemporalVoicechannel(), "SELECT * FROM TemporalVoicechannel WHERE GID=:gid", Map.of("gid", event.getGuild().getId()));

            if (temporalVoicechannel != null) {
                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

                if (voiceChannel == null)
                    return;

                if (!temporalVoicechannel.getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
                    return;
                }

                if (voiceChannel.getParentCategory() != null) {
                    voiceChannel.getParentCategory().createVoiceChannel("Temporal VC #" +
                            event.getGuild().getVoiceChannels().stream().filter(c -> c.getName().startsWith("Temporal VC")).toList().size() + 1).queue(channel -> {
                        event.getGuild().moveVoiceMember(event.getMember(), channel).queue();
                        ArrayUtil.temporalVoicechannel.add(channel.getId());
                    });
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (event.getMember() != event.getGuild().getSelfMember()) return;

        if (!event.isGuildDeafened()) {
            event.getGuild().getSelfMember().deafen(true).queue();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromGuild() && (event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.VOICE)) && event.getMember() != null) {
            if (ModerationUtil.shouldModerate(event.getGuild().getId())) {
                if (ModerationUtil.checkMessage(event.getGuild().getId(), event.getMessage().getContentRaw())) {
                    Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                    Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.blacklisted"), event.getChannel(), null);
                    return;
                } /* else if (!event.getMessage().getAttachments().isEmpty()) {
                    for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                        if (attachment.isImage()) {
                            if (ModerationUtil.checkImage(event.getGuild().getId(), attachment.getUrl())) {
                                Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                                Main.getInstance().getCommandManager().sendMessage("The Image contained blacklisted words!", event.getChannel(), null);
                                return;
                            }
                        }
                    }
                } else {
                    String messageContent = event.getMessage().getContentRaw();
                    String extractedUrl = ModerationUtil.extractUrl(messageContent);

                    if (extractedUrl != null &&!extractedUrl.isEmpty()) {
                        if (ModerationUtil.checkImage(event.getGuild().getId(), extractedUrl)) {
                            Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                            Main.getInstance().getCommandManager().sendMessage("The Image contained blacklisted words!", event.getChannel(), null);
                            return;
                        }
                    }
                } */
            }

            if (event.getAuthor().isBot()) return;

            if (!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage());
            }

            if (!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
            }


            if (!Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getChannel(), null)) {

                if (!event.getMessage().getMentions().getUsers().isEmpty() && event.getMessage().getMentions().getUsers().contains(event.getJDA().getSelfUser())) {
                    event.getChannel().sendMessage(LanguageService.getByGuild(event.getGuild(), "message.default.usage", "help")).queue();
                }

                if (!ArrayUtil.timeout.contains(event.getMember())) {

                    ChatUserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getId(), event.getMember().getId());
                    userLevel.setUser(event.getMember().getUser());

                    if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26)) && Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "level_message").getBooleanValue()) {
                        Main.getInstance().getCommandManager().sendMessage(LanguageService.getByGuild(event.getGuild(),
                                "message.levelUp", userLevel.getLevel(), LanguageService.getByGuild(event.getGuild(),"label.chat")
                                , event.getMember().getAsMention()), event.getChannel());
                    }

                    Main.getInstance().getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getId(), userLevel);

                    ArrayUtil.timeout.add(event.getMember());

                    ThreadUtil.createThread(x -> ArrayUtil.timeout.remove(event.getMember()), null, Duration.ofSeconds(30), false, false);
                }

                AutoRoleHandler.handleChatLevelReward(event.getGuild(), event.getMember());
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (!event.isFromGuild() && event.getMember() != null) return;

        event.deferReply(true).queue();

        Main.getInstance().getCommandManager().perform(Objects.requireNonNull(event.getMember()), event.getGuild(), null, null, event.getChannel(), event);
    }
}
