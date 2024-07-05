package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.wrapper.entities.VideoResult;
import de.presti.wrapper.entities.channel.ChannelResult;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class YouTubeSonic implements ISonic {
    ArrayList<SonicIdentifier> youtubeChannels = new ArrayList<>();

    @Override
    public void load() {
        try {
            // Register all YouTube channels.
            SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeChannels().subscribe(channel ->
                    channel.forEach(youtubeChannel ->
                            add(new SonicIdentifier(youtubeChannel))));
        } catch (Exception exception) {
            log.error("Error while loading YouTube data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public List<SonicIdentifier> getList() {
        return youtubeChannels;
    }

    @Override
    public void run() {
        for (String channel : getList().stream().map(SonicIdentifier::getIdentifier).toList()) {
            SQLSession.getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(channel).subscribe(webhooks -> {
                if (!webhooks.isEmpty()) {
                    try {
                        List<VideoResult> playlistItemList = YouTubeAPIHandler.getInstance().getYouTubeUploads(channel);
                        if (!playlistItemList.isEmpty()) {
                            for (VideoResult playlistItem : playlistItemList) {

                                Main.getInstance().logAnalytic("Video: " + playlistItem.getTitle() + " | " + playlistItem.getUploadDate() + " | " + playlistItem.getActualUploadDate() + " | " + playlistItem.getTimeAgo());
                                Main.getInstance().logAnalytic("Current: " + System.currentTimeMillis() + " | " + (playlistItem.getUploadDate() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()) + " | "
                                        + (playlistItem.getActualUploadDate() != null && playlistItem.getActualUploadDate().before(new Date(System.currentTimeMillis() - Duration.ofDays(2).toMillis()))) + " | " + (playlistItem.getTimeAgo() > 0 && Duration.ofMinutes(5).toMillis() >= playlistItem.getTimeAgo()));

                                if (playlistItem.getUploadDate() != -1 && (playlistItem.getUploadDate() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis() ||
                                        (playlistItem.getTimeAgo() > 0 && Duration.ofMinutes(5).toMillis() >= playlistItem.getTimeAgo())) &&
                                        playlistItem.getActualUploadDate() != null && !playlistItem.getActualUploadDate().before(new Date(System.currentTimeMillis() - Duration.ofDays(2).toMillis()))) {

                                    Main.getInstance().logAnalytic("Passed! -> " + playlistItem.getTitle() + " | " + playlistItem.getUploadDate() + " | " + playlistItem.getActualUploadDate());
                                    // Create a Webhook Message.
                                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                    webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                                    webhookMessageBuilder.setUsername(BotConfig.getBotName());

                                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                    webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(playlistItem.getOwnerName(), null));
                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("YouTube Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                                    webhookEmbedBuilder.setImageUrl(playlistItem.getThumbnail());

                                    webhookEmbedBuilder.setDescription("[**" + playlistItem.getTitle() + "**](https://www.youtube.com/watch?v=" + playlistItem.getId() + ")");

                                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
                                    webhookEmbedBuilder.setColor(Color.RED.getRGB());

                                    webhooks.forEach(webhook -> {
                                        String message = webhook.getMessage().replace("%name%", playlistItem.getOwnerName())
                                                .replace("%title%", playlistItem.getTitle())
                                                .replace("%description%", playlistItem.getDescriptionSnippet() != null ? "No Description" : playlistItem.getDescriptionSnippet())
                                                .replace("%url%", "https://www.youtube.com/watch?v=" + playlistItem.getId());

                                        webhookMessageBuilder.setContent(message);
                                        webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());
                                        WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                                    });

                                    break;
                                }
                            }
                        }
                    } catch (Exception exception) {
                        Sentry.captureException(exception);
                        log.error("Couldn't get user data of " + channel + "!", exception);
                    }
                }
            });

            SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                    "FROM ChannelStats WHERE youtubeSubscribersChannelUsername=:name", Map.of("name", channel)).subscribe(channelStats -> {
                if (!channelStats.isEmpty()) {
                    ChannelResult youTubeChannel;
                    try {
                        // TODO:: change YT Tracker to use the ID instead of username.
                        youTubeChannel = YouTubeAPIHandler.getInstance().getYouTubeChannelById(channel);
                    } catch (Exception e) {
                        Sentry.captureException(e);
                        return;
                    }

                    if (youTubeChannel == null) return;

                    for (ChannelStats channelStat : channelStats) {
                        if (channelStat.getYoutubeSubscribersChannelId() != null) {
                            GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getYoutubeSubscribersChannelId());

                            if (guildChannel == null) continue;

                            LanguageService.getByGuild(guildChannel.getGuild(), "label.youtubeCountName", youTubeChannel.getSubscriberCountText()).subscribe(newName -> {
                                if (!guildChannel.getName().equalsIgnoreCase(newName)) {
                                    if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                        return;

                                    guildChannel.getManager().setName(newName).queue();
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public void remove(SonicIdentifier object) {
        if (YouTubeAPIHandler.getInstance() == null) return;
        if (!contains(object)) return;

        SQLSession.getSqlConnector().getSqlWorker().getYouTubeWebhooksByName(object.getIdentifier()).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE youtubeSubscribersChannelUsername=:name",
                    Map.of("name", object.getIdentifier())).subscribe(channelStats -> {
                if (channelStats.isPresent()) return;

                youtubeChannels.remove(object);
            });
        });
    }
}
