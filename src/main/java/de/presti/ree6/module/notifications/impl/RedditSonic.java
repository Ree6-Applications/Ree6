package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import cn.hutool.core.exceptions.ValidateException;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.main.Main;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import masecla.reddit4j.exceptions.AuthenticationException;
import masecla.reddit4j.objects.RedditPost;
import masecla.reddit4j.objects.Sorting;
import masecla.reddit4j.objects.subreddit.RedditSubreddit;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.awt.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.*;

@Slf4j
public class RedditSonic implements ISonic {

    ArrayList<SonicIdentifier> subreddits = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        try {
            channelStats.stream().map(ChannelStats::getSubredditMemberChannelSubredditName).filter(Objects::nonNull).forEach(this::add);
            load();
        } catch (Exception exception) {
            log.error("Error while loading Reddit data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public void load() {
        // Register all Reddit Subreddits.
        SQLSession.getSqlConnector().getSqlWorker().getAllSubreddits().subscribe(channel ->
                channel.forEach(this::add));
    }

    @Override
    public List<SonicIdentifier> getList() {
        return subreddits;
    }

    @Override
    public void run() {
        try {
            for (String subreddit : subreddits.stream().map(SonicIdentifier::getIdentifier).toList()) {
                SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(),
                        "FROM ChannelStats WHERE subredditMemberChannelSubredditName=:name", Map.of("name", subreddit)).subscribe(channelStats -> {
                    if (!channelStats.isEmpty()) {
                        RedditSubreddit subredditEntity;
                        try {
                            subredditEntity = getSubreddit(subreddit);
                        } catch (IOException | InterruptedException e) {
                            return;
                        }

                        for (ChannelStats channelStat : channelStats) {
                            if (channelStat.getSubredditMemberChannelId() != null) {
                                GuildChannel guildChannel = BotWorker.getShardManager().getGuildChannelById(channelStat.getSubredditMemberChannelId());
                                String newName = "Subreddit Members: " + subredditEntity.getActiveUserCount();
                                if (guildChannel != null &&
                                        !guildChannel.getName().equalsIgnoreCase(newName)) {

                                    if (!guildChannel.getGuild().getSelfMember().hasAccess(guildChannel))
                                        continue;

                                    guildChannel.getManager().setName(newName).queue();
                                }
                            }
                        }
                    }
                });

                getSubredditPosts(subreddit, Sorting.NEW, 50).stream().filter(redditPost -> redditPost.getCreated() > (Duration.ofMillis(System.currentTimeMillis()).toSeconds() - Duration.ofMinutes(5).toSeconds())).forEach(redditPost -> SQLSession.getSqlConnector().getSqlWorker().getRedditWebhookBySub(subreddit).subscribe(webhooks -> {
                    if (webhooks.isEmpty()) return;

                    // Create Webhook Message.
                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                    webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
                    webhookMessageBuilder.setUsername(BotConfig.getBotName());

                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                    webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(redditPost.getTitle(), redditPost.getUrl()));
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Reddit Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));


                    if (!redditPost.getThumbnail().equalsIgnoreCase("self"))
                        webhookEmbedBuilder.setImageUrl(redditPost.getThumbnail());

                    // Set rest of the Information.
                    webhookEmbedBuilder.setDescription(URLDecoder.decode(redditPost.getSelftext(), StandardCharsets.UTF_8));
                    webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Author**", redditPost.getAuthor()));
                    webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Subreddit**", redditPost.getSubreddit()));
                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));

                    webhookEmbedBuilder.setColor(Color.ORANGE.getRGB());

                    webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                    webhooks.forEach(webhook -> {
                        String message = webhook.getMessage()
                                .replace("%title%", redditPost.getTitle())
                                .replace("%author%", redditPost.getAuthor())
                                .replace("%name%", redditPost.getSubreddit())
                                .replace("%url%", redditPost.getUrl());
                        webhookMessageBuilder.setContent(message);
                        WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook, WebhookUtil.WebhookTyp.REDDIT);
                    });
                }));
            }
        } catch (Exception exception) {
            log.error("Could not get Reddit Posts!", exception);
            Sentry.captureException(exception);
        }
    }

    /**
     * Used to get a Subreddit.
     *
     * @param subreddit the Name of the Subreddit.
     * @return the Subreddit.
     * @throws IOException          if the Subreddit couldn't be found.
     * @throws InterruptedException if the Thread was interrupted.
     */
    public RedditSubreddit getSubreddit(String subreddit) throws IOException, InterruptedException {
        return Main.getInstance().getNotifier().getRedditClient().getSubreddit(subreddit);
    }


    public List<RedditPost> getSubredditPosts(String subreddit, Sorting sorting, int limit) throws AuthenticationException, IOException, InterruptedException {
        try {
            return Main.getInstance().getNotifier().getRedditClient().getSubredditPosts(subreddit, sorting).limit(limit).submit();
        } catch (ValidateException exception) {
            if (exception.getMessage().startsWith("The parameter")) {
                Main.getInstance().getNotifier().getRedditClient().userlessConnect();
                return Main.getInstance().getNotifier().getRedditClient().getSubredditPosts(subreddit, sorting).limit(limit).submit();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void remove(SonicIdentifier object) {
        if (Main.getInstance().getNotifier().getRedditClient() == null) return;
        if (!contains(object)) return;

        SQLSession.getSqlConnector().getSqlWorker().getRedditWebhookBySub(object.getIdentifier()).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            SQLSession.getSqlConnector().getSqlWorker().getEntity(new ChannelStats(), "FROM ChannelStats WHERE subredditMemberChannelSubredditName=:name",
                    Map.of("name", object.getIdentifier())).subscribe(channelStats -> {
                if (channelStats.isPresent()) return;

                subreddits.remove(object);
            });
        });
    }
}
