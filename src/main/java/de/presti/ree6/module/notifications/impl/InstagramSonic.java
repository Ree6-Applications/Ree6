package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.github.instagram4j.instagram4j.actions.feed.FeedIterator;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineImageMedia;
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineVideoMedia;
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest;
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class InstagramSonic implements ISonic {

    ArrayList<SonicIdentifier> instagramChannels = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        try {
            channelStats.stream().map(ChannelStats::getInstagramFollowerChannelUsername).filter(Objects::nonNull).forEach(this::add);
            load();
        } catch (Exception exception) {
            log.error("Error while loading Instagram data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public void load() {
        // Register all Instagram Users.
        SQLSession.getSqlConnector().getSqlWorker().getAllInstagramUsers().subscribe(instagramUsers ->
                instagramUsers.forEach(this::add));
    }

    @Override
    public List<SonicIdentifier> getList() {
        return instagramChannels;
    }

    @Override
    public void run() {
        if (!Main.getInstance().getNotifier().getInstagramClient().isLoggedIn()) return;
        for (String username : getList().stream().map(SonicIdentifier::getIdentifier).toList()) {

            Main.getInstance().getNotifier().getInstagramClient().actions().users().findByUsername(username).thenAccept(userAction -> {
                com.github.instagram4j.instagram4j.models.user.User user = userAction.getUser();

                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                        "FROM ChannelStats WHERE instagramFollowerChannelUsername=:name", Map.of("name", username)).subscribe(channelStats -> {
                    if (!channelStats.isEmpty()) {
                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getInstagramFollowerChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getInstagramFollowerChannelId());

                                if (guildChannel == null) continue;

                                LanguageService.getByGuild(guildChannel.getGuild(), "label.instagramCountName", user.getFollower_count()).subscribe(newName -> {
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

                SQLSession.getSqlConnector().getSqlWorker().getInstagramWebhookByName(username).subscribe(webhooks -> {
                    if (webhooks.isEmpty()) return;

                    if (!user.is_private()) {
                        FeedIterator<FeedUserRequest, FeedUserResponse> iterable = new FeedIterator<>(Main.getInstance().getNotifier().getInstagramClient(), new FeedUserRequest(user.getPk()));

                        int limit = 1;
                        while (iterable.hasNext() && limit-- > 0) {
                            FeedUserResponse response = iterable.next();
                            // Actions here
                            response.getItems().stream().filter(post -> post.getTaken_at() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(instagramPost -> {
                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                                webhookMessageBuilder.setUsername(BotConfig.getBotName());

                                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(user.getUsername(), "https://www.instagram.com/" + user.getUsername()));
                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Instagram Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));

                                // Set the rest of the Information.
                                if (instagramPost instanceof TimelineImageMedia timelineImageMedia) {
                                    webhookEmbedBuilder.setImageUrl(timelineImageMedia.getImage_versions2().getCandidates().get(0).getUrl());
                                    webhookEmbedBuilder.setDescription(timelineImageMedia.getCaption().getText());
                                } else if (instagramPost instanceof TimelineVideoMedia timelineVideoMedia) {
                                    webhookEmbedBuilder.setDescription("[Click here to watch the video](" + timelineVideoMedia.getVideo_versions().get(0).getUrl() + ")");
                                } else {
                                    webhookEmbedBuilder.setDescription(user.getUsername() + " just posted something new on Instagram!");
                                }

                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                                webhookEmbedBuilder.setColor(Color.MAGENTA.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                webhooks.forEach(webhook -> WebhookUtil.sendWebhook(webhookMessageBuilder.setContent(webhook.getMessage()
                                        .replace("%username%", user.getUsername())).build(), webhook));
                            });
                        }
                    }
                });
            }).exceptionally(exception -> {
                log.error("Could not get Instagram User!", exception);
                Sentry.captureException(exception);
                return null;
            }).join();
        }
    }

    @Override
    public void remove(SonicIdentifier object) {
        if (Main.getInstance().getNotifier().getInstagramClient() == null) return;
        if (!contains(object)) return;

        SQLSession.getSqlConnector().getSqlWorker().getInstagramWebhookByName(object.getIdentifier()).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE instagramFollowerChannelUsername=:name",
                    Map.of("name", object.getIdentifier())).subscribe(channelStats -> {
                if (channelStats.isPresent()) return;

                instagramChannels.remove(object);
            });
        });
    }
}
