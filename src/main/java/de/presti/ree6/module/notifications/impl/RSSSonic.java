package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Image;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.module.itunes.ItunesItem;
import com.apptasticsoftware.rssreader.module.itunes.ItunesRssReader;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RSSSonic implements ISonic {

    ArrayList<SonicIdentifier> rssUrls = new ArrayList<>();

    @Override
    public void load() {
        try {
            // Register all RSS-Feeds.
            SQLSession.getSqlConnector().getSqlWorker().getAllRSSUrls().subscribe(rssUrl ->
                    rssUrl.forEach(this::add));
        } catch (Exception exception) {
            log.error("Error while loading RSS data: {}", exception.getMessage());
            Sentry.captureException(exception);
        }
    }

    @Override
    public List<SonicIdentifier> getList() {
        return rssUrls;
    }

    @Override
    public void run() {
        Collection<String> urls = getList().stream().map(SonicIdentifier::getIdentifier).toList();

        /*
         *  Either switch to RSSHub, YouTubes RSS or stay on API based.
         *  Issue with RSSHub is that it takes 2 hours to update, because of caching.
         *  Issue with YouTube is RSS that it takes over 30 minutes to update, because of idk random internal stuff.
         *
         *  ////Collection<String> urls = new ArrayList<>(registeredYouTubeChannels.stream().map(c -> "https://rsshub.app/youtube/channel/" + c).toList());
         *
         *  Wait till Nitter has fixed their RSS Feeds. Or Twitter finally gets the stick out of their ass and stop limiting simple scraping. 05.07: Twitter still has a stick up their ass and Nitter died because of it. WoW.
         *  ////urls.addAll(registeredTwitterUsers.stream().map(c -> "https://nitter.net/" + c + "/rss").toList());
         *
         *  ////urls.addAll(registeredRSSFeeds);
         */


        List<String> checkedIds = new ArrayList<>();

        // To support Podcast RSS.
        new ItunesRssReader()
                .addItemExtension("media:description", Item::setDescription)
                .addItemExtension("media:thumbnail", "url", (item, element) -> {
                    Image image = item.getChannel().getImage().orElse(new Image());
                    image.setUrl(element);
                    item.getChannel().setImage(image);
                }).addItemExtension("media:thumbnail", "width", (item, element) -> {
                    Image image = item.getChannel().getImage().orElse(new Image());
                    image.setWidth(Integer.valueOf(element));
                    item.getChannel().setImage(image);
                }).addItemExtension("media:thumbnail", "height", (item, element) -> {
                    Image image = item.getChannel().getImage().orElse(new Image());
                    image.setHeight(Integer.valueOf(element));
                    item.getChannel().setImage(image);
                }).addChannelExtension("published", Channel::setPubDate)
                .addItemExtension("dc:creator", Item::setAuthor)
                .addItemExtension("dc:date", Item::setPubDate)
                .addItemExtension("yt:channelId", Item::setAuthor)
                .setUserAgent("Ree6Bot/" + BotWorker.getBuild() + " (by Presti)")
                .read(urls)
                .sorted()
                .forEach(item -> {
                    if (item.getPubDate().isEmpty()) return;

                    String typ = "other";

                    if (item.getGuid().isPresent()) {
                        String guid = item.getGuid().get();

                        if (guid.contains("nitter")) {
                            typ = "tw";
                        } else {
                            typ = "other";
                        }
                    }


                    OffsetDateTime dateTime = OffsetDateTime.parse(item.getPubDate().orElse(""), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                    OffsetDateTime now = OffsetDateTime.now();
                    OffsetDateTime threeMinuteAgo = now.minusMinutes(3);

                    if (dateTime.isBefore(threeMinuteAgo)) return;

                    if (item.getChannel() != null) {

                        String id = "";

                        if (typ.equals("tw")) {
                            id = item.getChannel().getLink().replace("https://nitter.net/", "");
                        } else {
                            id = item.getChannel().getLink();
                        }

                        if (checkedIds.contains(id)) {
                            return;
                        }


                        if (typ.equals("tw")) {
                            SQLSession.getSqlConnector().getSqlWorker().getTwitterWebhooksByName(item.getChannel().getLink().replace("https://nitter.net/", "")).subscribe(webhooks -> {

                                if (webhooks.isEmpty()) return;

                                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                webhookMessageBuilder.setUsername(BotConfig.getBotName());
                                webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());

                                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                item.getChannel().getImage().ifPresentOrElse(image ->
                                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(),
                                                        URLDecoder.decode(image.getUrl().replace("nitter.net/pic/", ""), StandardCharsets.UTF_8), null)),
                                        () -> webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(), null, null)));


                                webhookEmbedBuilder.setDescription(item.getTitle() + "\n");

                                item.getDescription().ifPresent(description -> {
                                    if (description.contains("<img src=")) {
                                        String imageUrl = description.split("<img src=\"")[1].split("\"")[0];
                                        webhookEmbedBuilder.setImageUrl(imageUrl);
                                    }
                                });

                                webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(item.getChannel().getTitle(), null));
                                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("Twitter Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

                                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
                                webhookEmbedBuilder.setTimestamp(Instant.now());
                                webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                webhooks.forEach(webhook -> {
                                    String message = webhook.getMessage()
                                            .replace("%name%", item.getChannel().getTitle());

                                    if (item.getLink().isPresent()) {
                                        message = message.replace("%url%", item.getLink().get()
                                                        .replace("nitter.net", "twitter.com"))
                                                .replace("#m", "");
                                    }
                                    webhookMessageBuilder.setContent(message);
                                    WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
                                });
                            });
                        } else {
                            try {
                                SQLSession.getSqlConnector().getSqlWorker().getRSSWebhooksByUrl(id).subscribe(webhooks -> {
                                    if (webhooks.isEmpty()) return;

                                    WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                                    webhookMessageBuilder.setUsername(BotConfig.getBotName());
                                    webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl());

                                    WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                                    item.getChannel().getImage().ifPresentOrElse(image ->
                                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(),
                                                            URLDecoder.decode(image.getUrl(), StandardCharsets.UTF_8), null)),
                                            () -> webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(item.getChannel().getTitle(), null, null)));

                                    item.getDescription().ifPresent(description -> webhookEmbedBuilder.setDescription(description + "\n"));

                                    if (item instanceof ItunesItem itunesItem) {
                                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(itunesItem.getItunesTitle().orElse("No Title"), item.getLink().orElse("No Link")));
                                        itunesItem.getItunesImage().ifPresent(webhookEmbedBuilder::setThumbnailUrl);
                                    } else {

                                        webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(item.getTitle().orElse("No Title"), item.getLink().orElse("No Link")));
                                    }

                                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor("RSS Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl(), null));

                                    webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getEffectiveAvatarUrl()));
                                    webhookEmbedBuilder.setTimestamp(Instant.now());
                                    webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                                    webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                                    webhooks.forEach(webhook -> WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook));
                                });
                            } catch (Exception exception) {
                                Sentry.captureException(exception);
                            }
                        }

                        checkedIds.add(id);
                    }
                });
    }

    @Override
    public void unload() {

    }
}
