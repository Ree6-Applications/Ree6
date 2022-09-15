package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.entities.SQLResponse;
import de.presti.ree6.sql.base.utils.SQLUtil;
import de.presti.ree6.sql.entities.TemporalVoicechannel;
import de.presti.ree6.sql.entities.level.ChatUserLevel;
import de.presti.ree6.sql.entities.level.VoiceUserLevel;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.*;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.jetbrains.annotations.NotNull;
import twitter4j.TwitterException;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OtherEvents extends ListenerAdapter {

    /**
     * @inheritDoc
     */
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotWorker.setState(BotState.STARTED);
        Main.getInstance().getLogger().info("Boot up finished!");

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

        SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
        if (sqlResponse.isSuccess()) {
            ChannelStats channelStats = (ChannelStats) sqlResponse.getEntity();
            if (channelStats.getMemberStatsChannelId() != null) {
                GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                if (guildChannel != null) {
                    guildChannel.getManager().setName("Overall Members: " + event.getGuild().getMemberCount()).queue();
                }
            }

            event.getGuild().loadMembers().onSuccess(members -> {
                if (channelStats.getRealMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName("Real Members: " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                    }
                }

                if (channelStats.getBotMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName("Bot Members: " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                    }
                }
            });
        }

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId())) return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");
        wmb.setContent((Main.getInstance().getSqlConnector().getSqlWorker().getMessage(event.getGuild().getId())).replace("%user_name%", event.getMember().getUser().getName()).replace("%user_mention%", event.getMember().getUser().getAsMention()).replace("%guild_name%", event.getGuild().getName()));

        WebhookUtil.sendWebhook(null, wmb.build(), Main.getInstance().getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId()), false);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        super.onGuildMemberRemove(event);

        SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
        if (sqlResponse.isSuccess()) {
            ChannelStats channelStats = (ChannelStats) sqlResponse.getEntity();
            if (channelStats.getMemberStatsChannelId() != null) {
                GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getMemberStatsChannelId());
                if (guildChannel != null) {
                    guildChannel.getManager().setName("Overall Members: " + event.getGuild().getMemberCount()).queue();
                }
            }

            event.getGuild().loadMembers().onSuccess(members -> {
                if (channelStats.getRealMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getRealMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName("Real Members: " + members.stream().filter(member -> !member.getUser().isBot()).count()).queue();
                    }
                }

                if (channelStats.getBotMemberStatsChannelId() != null) {
                    GuildChannel guildChannel = event.getGuild().getGuildChannelById(channelStats.getBotMemberStatsChannelId());
                    if (guildChannel != null) {
                        guildChannel.getManager().setName("Bot Members: " + members.stream().filter(member -> member.getUser().isBot()).count()).queue();
                    }
                }
            });
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        super.onGuildVoiceJoin(event);
        if (!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
        }

        SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(TemporalVoicechannel.class, "SELECT * FROM TemporalVoicechannel WHERE GID = ? AND VID = ?", event.getGuild().getId(), event.getChannelJoined().getId());

        if (sqlResponse.isSuccess()) {
            VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

            if (voiceChannel == null)
                return;

            if (!((TemporalVoicechannel) sqlResponse.getEntity()).getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
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

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        super.onGuildVoiceMove(event);

        SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(TemporalVoicechannel.class, "SELECT * FROM TemporalVoicechannel WHERE GID = ? AND VID = ?", event.getGuild().getId(), event.getChannelJoined().getId());

        if (sqlResponse.isSuccess()) {
            VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getChannelJoined().getId());

            if (voiceChannel == null)
                return;

            if (!((TemporalVoicechannel) sqlResponse.getEntity()).getVoiceChannelId().equalsIgnoreCase(voiceChannel.getId())) {
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

    /**
     * @inheritDoc
     */
    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

            int addxp = 0;

            for (int i = 1; i <= min; i++) {
                addxp += RandomUtils.random.nextInt(5, 11);
            }

            VoiceUserLevel newUserLevel = Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelData(event.getGuild().getId(), event.getMember().getId());
            VoiceUserLevel oldUserLevel = (VoiceUserLevel) SQLUtil.cloneEntity(VoiceUserLevel.class, newUserLevel);
            newUserLevel.setUser(event.getMember().getUser());
            newUserLevel.addExperience(addxp);

            Main.getInstance().getSqlConnector().getSqlWorker().addVoiceLevelData(event.getGuild().getId(), oldUserLevel, newUserLevel);

            AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());

        }

        if (ArrayUtil.isTemporalVoicechannel(event.getChannelLeft())
                && event.getChannelLeft().getMembers().isEmpty()) {
            event.getChannelLeft().delete().queue();
            ArrayUtil.temporalVoicechannel.remove(event.getChannelLeft().getId());
        }
        super.onGuildVoiceLeave(event);
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
                    Main.getInstance().getCommandManager().sendMessage("Your message contains blacklisted words!", event.getChannel(), null);
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
                    event.getChannel().sendMessage("Usage " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "chatprefix").getStringValue() + "help").queue();
                }

                if (!ArrayUtil.timeout.contains(event.getMember())) {

                    ChatUserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getId(), event.getMember().getId());
                    ChatUserLevel oldUserLevel = (ChatUserLevel) SQLUtil.cloneEntity(ChatUserLevel.class, userLevel);
                    userLevel.setUser(event.getMember().getUser());

                    if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26)) && Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "level_message").getBooleanValue()) {
                        Main.getInstance().getCommandManager().sendMessage("You just leveled up to Chat Level " + userLevel.getLevel() + " " + event.getMember().getAsMention() + " !", event.getChannel());
                    }

                    Main.getInstance().getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getId(), oldUserLevel, userLevel);

                    ArrayUtil.timeout.add(event.getMember());

                    ThreadUtil.createNewThread(x -> ArrayUtil.timeout.remove(event.getMember()), null, Duration.ofSeconds(30), false, false);
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

    /**
     * @inheritDoc
     */
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        super.onModalInteraction(event);

        switch(event.getModalId()) {
            case "statisticsSetupTwitchModal" -> {
                ModalMapping modalMapping = event.getValue("twitchChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String twitchUsername = modalMapping.getAsString();

                List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                String channelId = Main.getInstance().getNotifier().getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(twitchUsername)).execute().getUsers().get(0).getId();
                event.getGuild().createVoiceChannel("Twitch Follower: " + Main.getInstance().getNotifier().getTwitchClient().getHelix().getFollowers(null, null, channelId, null, 20).execute().getTotal(), category).queue(voiceChannel -> {
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                    ChannelStats channelStats;

                    if (sqlResponse.isSuccess()) {
                        channelStats = (ChannelStats) sqlResponse.getEntity();
                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);

                        if (channelStats.getTwitchFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getTwitchFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setTwitchFollowerChannelId(voiceChannel.getId());
                        channelStats.setTwitchFollowerChannelUsername(twitchUsername);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                        Main.getInstance().getNotifier().registerTwitchChannel(twitchUsername);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                twitchUsername,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitchChannel(twitchUsername);
                    }
                });

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitch statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupYouTubeModal" -> {
                ModalMapping modalMapping = event.getValue("youtubeChannelName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String youtubeChannelName = modalMapping.getAsString();

                List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for YouTube statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                com.google.api.services.youtube.model.Channel youTubeChannel;
                try {
                    if (YouTubeAPIHandler.getInstance().isValidChannelId(youtubeChannelName)) {
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelById(youtubeChannelName, "statistics");
                    } else {
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelBySearch(youtubeChannelName, "statistics");
                    }
                } catch (IOException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Channel data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                if (youTubeChannel == null) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("We could not find the given channel! You sure the name/id is correct?");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("YouTube Subscribers: " + (youTubeChannel.getStatistics().getHiddenSubscriberCount() ? "HIDDEN" : youTubeChannel.getStatistics().getSubscriberCount()), category).queue(voiceChannel -> {
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                    ChannelStats channelStats;

                    if (sqlResponse.isSuccess()) {
                        channelStats = (ChannelStats) sqlResponse.getEntity();
                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);

                        if (channelStats.getYoutubeSubscribersChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getYoutubeSubscribersChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setYoutubeSubscribersChannelId(voiceChannel.getId());
                        channelStats.setYoutubeSubscribersChannelUsername(youtubeChannelName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                        Main.getInstance().getNotifier().registerYouTubeChannel(youtubeChannelName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                youtubeChannelName,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerYouTubeChannel(youtubeChannelName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for YouTube statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupRedditModal" -> {
                ModalMapping modalMapping = event.getValue("subredditName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String subredditName = modalMapping.getAsString();

                List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Reddit statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                RedditSubreddit subreddit;
                try {
                    subreddit = Main.getInstance().getNotifier().getSubreddit(subredditName);
                } catch (IOException | InterruptedException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Subreddit data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Subreddit Members: " + subreddit.getActiveUserCount(), category).queue(voiceChannel -> {
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                    ChannelStats channelStats;

                    if (sqlResponse.isSuccess()) {
                        channelStats = (ChannelStats) sqlResponse.getEntity();
                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);

                        if (channelStats.getSubredditMemberChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getSubredditMemberChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setSubredditMemberChannelId(voiceChannel.getId());
                        channelStats.setSubredditMemberChannelSubredditName(subredditName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                        Main.getInstance().getNotifier().registerSubreddit(subredditName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                subredditName);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerSubreddit(subredditName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Reddit statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupTwitterModal" -> {
                ModalMapping modalMapping = event.getValue("twitterName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String twitterName = modalMapping.getAsString();

                List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitter statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                twitter4j.User twitterUser;
                try {
                    twitterUser = Main.getInstance().getNotifier().getTwitterClient().showUser(twitterName);
                } catch (TwitterException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Twitter User data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Twitter Follower: " + twitterUser.getFollowersCount(), category).queue(voiceChannel -> {
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                    ChannelStats channelStats;

                    if (sqlResponse.isSuccess()) {
                        channelStats = (ChannelStats) sqlResponse.getEntity();
                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);

                        if (channelStats.getTwitterFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getTwitterFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setTwitterFollowerChannelId(voiceChannel.getId());
                        channelStats.setTwitterFollowerChannelUsername(twitterName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                        Main.getInstance().getNotifier().registerTwitterUser(twitterName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                twitterName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerTwitterUser(twitterName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Twitter statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            case "statisticsSetupInstagramModal" -> {
                ModalMapping modalMapping = event.getValue("instagramName");

                if (modalMapping == null) return;

                if (event.getGuild() == null) return;

                String instagramName = modalMapping.getAsString();

                List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                Category category;

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Instagram statistics!");

                if (categories.isEmpty()) {
                    category = event.getGuild().createCategory("Statistics").complete();
                } else {
                    category = categories.get(0);
                }

                com.github.instagram4j.instagram4j.models.user.User instagramUser;
                try {
                    instagramUser = Main.getInstance().getNotifier().getInstagramClient().getActions().users().findByUsername(instagramName).get().getUser();
                } catch (ExecutionException | InterruptedException e) {
                    embedBuilder = embedBuilder
                            .setTitle("Setup Menu")
                            .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                            .setColor(Color.RED)
                            .setDescription("There was an error while trying to access the Instagram User data!");
                    event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    return;
                }

                event.getGuild().createVoiceChannel("Instagram Follower: " + instagramUser.getFollower_count(), category).queue(voiceChannel -> {
                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                    ChannelStats channelStats;

                    if (sqlResponse.isSuccess()) {
                        channelStats = (ChannelStats) sqlResponse.getEntity();
                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);

                        if (channelStats.getInstagramFollowerChannelId() != null) {
                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getInstagramFollowerChannelId());

                            if (voiceChannel3 != null)
                                voiceChannel3.delete().queue();
                        }

                        channelStats.setInstagramFollowerChannelId(voiceChannel.getId());
                        channelStats.setInstagramFollowerChannelUsername(instagramName);
                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                        Main.getInstance().getNotifier().registerInstagramUser(instagramName);
                    } else {
                        channelStats = new ChannelStats(event.getGuild().getId(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                voiceChannel.getId(),
                                instagramName,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                        Main.getInstance().getNotifier().registerInstagramUser(instagramName);
                    }
                });

                embedBuilder = embedBuilder
                        .setTitle("Setup Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Successfully setup the statics channels for Instagram statistics!");
                event.deferEdit().setEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
            }

            default -> {
                event.deferEdit().setEmbeds(new EmbedBuilder()
                        .setTitle("Unknown Menu")
                        .setFooter(event.getGuild().getName() + " - " + Data.ADVERTISEMENT, event.getGuild().getIconUrl())
                        .setColor(Color.RED)
                        .setDescription("There was an error while resolving the modal!")
                        .build()).setComponents(new ArrayList<>()).queue();
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        super.onSelectMenuInteraction(event);

        if (event.getInteraction().getComponent().getId() == null ||
                event.getGuild() == null)
            return;

        if (event.getMessage().getEmbeds().isEmpty() ||
                event.getMessage().getEmbeds().get(0) == null ||
                event.getInteraction().getSelectedOptions().isEmpty())
            return;

        if (event.getInteraction().getValues().isEmpty())
            return;

        switch (event.getInteraction().getComponent().getId()) {
            case "setupActionMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {
                    case "log" -> {
                        optionList.add(SelectOption.of("Setup", "logSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "logDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Audit-Logging which provides all the Information over and Webhook into the Channel of your desire! " + "But ours is not the same as the default Auditions, ours gives your the ability to set what you want to be logged and what not! " + "We also allow you to log Voice Events!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupLogMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "welcome" -> {
                        optionList.add(SelectOption.of("Setup", "welcomeSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "welcomeDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Welcome-Messages!\nYou can choice the Welcome-Channel by your own and even configure the Message!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupWelcomeMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "news" -> {
                        optionList.add(SelectOption.of("Setup", "newsSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isNewsSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "newsDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Ree6-News!\nBy setting up Ree6-News on a specific channel you will get a Message in the given Channel, when ever Ree6 gets an update!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupNewsMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "autorole" -> {
                        embedBuilder.setDescription("You can set up our own Autorole-System!\nYou can select Roles that Users should get upon joining the Server!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(Button.link("https://cp.ree6.de", "Webinterface")).queue();
                    }

                    case "tempvoice" -> {
                        optionList.add(SelectOption.of("Setup", "tempVoiceSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isNewsSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "tempVoiceDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up your own Temporal Voicechannel!\nBy setting up Temporal Voicechannel on a specific channel which will be used to create a new Voicechannel when ever someones joins into it!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTempVoiceMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    case "statistics" -> {
                        optionList.add(SelectOption.of("Setup Member Statistics", "statisticsSetupMember"));
                        optionList.add(SelectOption.of("Setup Twitch Statistics", "statisticsSetupTwitch"));
                        optionList.add(SelectOption.of("Setup YouTube Statistics", "statisticsSetupYouTube"));
                        optionList.add(SelectOption.of("Setup Reddit Statistics", "statisticsSetupReddit"));
                        optionList.add(SelectOption.of("Setup Twitter Statistics", "statisticsSetupTwitter"));
                        optionList.add(SelectOption.of("Setup Instagram Statistics", "statisticsSetupInstagram"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up your own Statistic-channels!\nBy setting up Statistic-channels Ree6 will create new channels for each Statistic-Type that you setup!\nIf you want to get rid of a Statistic-Channel, just delete it!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupStatisticsMenu", "Select your Action", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupStatisticsMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "statisticsSetupMember" -> {
                        embedBuilder.setDescription("Successfully setup the statistics channels for Member statistics!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                        List<Category> categories = event.getGuild().getCategoriesByName("Statistics", true);

                        Category category;

                        if (categories.isEmpty()) {
                            category = event.getGuild().createCategory("Statistics").complete();
                        } else {
                            category = categories.get(0);
                        }

                        event.getGuild().loadMembers().onSuccess(members -> event.getGuild().createVoiceChannel("Overall Members: " + event.getGuild().getMemberCount(), category).queue(voiceChannel -> {
                            voiceChannel.getManager().setUserLimit(0).queue();
                            event.getGuild().createVoiceChannel("Real Members: " + members.stream().filter(member -> !member.getUser().isBot()).count(), category).queue(voiceChannel1 -> {
                                voiceChannel1.getManager().setUserLimit(0).queue();
                                event.getGuild().createVoiceChannel("Bot Members: " + members.stream().filter(member -> member.getUser().isBot()).count(), category).queue(voiceChannel2 -> {
                                    voiceChannel2.getManager().setUserLimit(0).queue();
                                    SQLResponse sqlResponse = Main.getInstance().getSqlConnector().getSqlWorker().getEntity(ChannelStats.class, "SELECT * FROM ChannelStats WHERE GID=?", event.getGuild().getId());
                                    ChannelStats channelStats;

                                    if (sqlResponse.isSuccess()) {
                                        channelStats = (ChannelStats) sqlResponse.getEntity();
                                        ChannelStats oldChannelStats = (ChannelStats) SQLUtil.cloneEntity(ChannelStats.class, channelStats);
                                        if (channelStats.getMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        if (channelStats.getRealMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getRealMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        if (channelStats.getBotMemberStatsChannelId() != null) {
                                            VoiceChannel voiceChannel3 = event.getGuild().getVoiceChannelById(channelStats.getBotMemberStatsChannelId());

                                            if (voiceChannel3 != null)
                                                voiceChannel3.delete().queue();
                                        }
                                        channelStats.setMemberStatsChannelId(voiceChannel.getId());
                                        channelStats.setRealMemberStatsChannelId(voiceChannel1.getId());
                                        channelStats.setBotMemberStatsChannelId(voiceChannel2.getId());
                                        Main.getInstance().getSqlConnector().getSqlWorker().updateEntity(oldChannelStats, channelStats, false);
                                    } else {
                                        channelStats = new ChannelStats(event.getGuild().getId(),
                                                voiceChannel.getId(),
                                                voiceChannel1.getId(),
                                                voiceChannel2.getId(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null);
                                        Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(channelStats);
                                    }
                                });
                            });
                        }));
                    }

                    case "statisticsSetupTwitch" -> {
                        TextInput input = TextInput.create("twitchChannelName", "Twitch Channel Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitch Channel name here!").build();

                        Modal modal = Modal.create("statisticsSetupTwitchModal", "Twitch Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupYouTube" -> {
                        TextInput input = TextInput.create("youtubeChannelName", "YouTube Channel Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the YouTube Channel name here!").build();

                        Modal modal = Modal.create("statisticsSetupYouTubeModal", "YouTube Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupReddit" -> {
                        TextInput input = TextInput.create("subredditName", "Subreddit Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Subreddit name here!").build();

                        Modal modal = Modal.create("statisticsSetupRedditModal", "Reddit Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupTwitter" -> {
                        TextInput input = TextInput.create("twitterName", "Twitter Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Twitter name here!").build();

                        Modal modal = Modal.create("statisticsSetupTwitterModal", "Twitter Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    case "statisticsSetupInstagram" -> {
                        TextInput input = TextInput.create("instagramName", "Instagram Name", TextInputStyle.SHORT).setMinLength(1).setMaxLength(50).setRequired(true).setPlaceholder("Enter the Instagram name here!").build();

                        Modal modal = Modal.create("statisticsSetupInstagramModal", "Instagram Statistic Channel").addActionRow(input).build();

                        event.replyModal(modal).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupTempVoicechannel" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                VoiceChannel voiceChannel = event.getGuild().getVoiceChannelById(event.getInteraction().getValues().get(0));

                if (voiceChannel != null) {
                    Main.getInstance().getSqlConnector().getSqlWorker().saveEntity(new TemporalVoicechannel(event.getGuild().getId(), voiceChannel.getId()));
                    embedBuilder.setDescription("Successfully changed the Temporal Voicechannel, nice work!");
                    embedBuilder.setColor(Color.GREEN);
                    event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            case "setupTempVoiceMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "tempVoiceSetup" -> {
                        for (VoiceChannel channel : event.getGuild().getVoiceChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Temporal-Voicechannel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupTempVoicechannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "logSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Logging-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupLogChannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Logs").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Logging Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            case "setupWelcomeMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "welcomeSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Welcome-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupWelcomeChannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupWelcomeChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Welcome").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Welcome-Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            case "setupNewsMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getValues().get(0)) {

                    case "backToSetupMenu" -> sendDefaultChoice(event);

                    case "newsSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as News-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupNewsChannel", "Select a Channel!", 1, 1, false, optionList)).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupNewsChannel" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getValues().get(0));

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-News").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setNewsWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the News-Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setComponents(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            default -> {
                if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null) return;

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                event.editMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    /**
     * Called when the default choices should be sent.
     *
     * @param event The InteractionEvent of the SelectMenu.
     */
    public void sendDefaultChoice(SelectMenuInteractionEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

        List<SelectOption> optionList = new ArrayList<>();
        optionList.add(SelectOption.of("Audit-Logging", "log"));
        optionList.add(SelectOption.of("Welcome-channel", "welcome"));
        optionList.add(SelectOption.of("News-channel", "news"));
        optionList.add(SelectOption.of("Autorole", "autorole"));
        optionList.add(SelectOption.of("Temporal-Voice", "tempvoice"));
        optionList.add(SelectOption.of("Statistics", "statistics"));

        embedBuilder.setDescription("Which configuration do you want to check out?");

        event.editMessageEmbeds(embedBuilder.build()).setActionRow(new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList)).queue();
    }

    /**
     * Checks if the user has the required Permissions to use the Command.
     *
     * @param member  The Member who should be checked.
     * @param channel The Channel used.
     * @return True if the user has the required Permissions, false if not.
     */
    private boolean checkPerms(Member member, MessageChannel channel) {
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("You do not have enough Permissions").queue();
            return true;
        }

        if (!member.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            channel.sendMessage("I do not have enough Permissions to create Webhooks").queue();
            return true;
        }

        return false;
    }
}
