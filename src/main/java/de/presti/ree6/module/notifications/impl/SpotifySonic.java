package de.presti.ree6.module.notifications.impl;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.webhook.WebhookSpotify;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.EpisodeSimplified;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpotifySonic implements ISonic {
    ArrayList<SonicIdentifier> spotifyEntries = new ArrayList<>();

    SimpleDateFormat spotifyTimestamp = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void load(List<ChannelStats> channelStats) {
        load();
        // No need for this.
    }

    @Override
    public void load() {
        // Register all Spotify artists and podcasts.
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new WebhookSpotify(), "FROM WebhookSpotify", Map.of()).subscribe(spotifyNotify -> spotifyNotify.forEach(x -> {
            if (x.getEntityTyp() == 0) {
                add("art:" + x.getEntityId());
            } else {
                add("pot:" + x.getEntityId());
            }
        }));
    }

    @Override
    public List<SonicIdentifier> getList() {
        return spotifyEntries;
    }

    @Override
    public void run() {
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new WebhookSpotify(), "FROM WebhookSpotify", Map.of()).subscribe(spotifyNotify -> {
            Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
            for (String entry : spotifyEntries.stream().map(SonicIdentifier::getIdentifier).toList()) {
                String actualId = entry.substring(entry.lastIndexOf(':') + 1);
                try {
                    if (entry.startsWith("art")) {
                        ArrayList<AlbumSimplified> albums = SpotifyAPIHandler.getInstance().getArtistAlbums(actualId);
                        if (albums.isEmpty()) return;
                        AlbumSimplified album = albums.get(0);
                        if (album == null) return;
                        if (album.getReleaseDatePrecision().precision.equals("year")) continue;
                        if (album.getReleaseDatePrecision().precision.equals("month")) continue;

                        Date date = spotifyTimestamp.parse(album.getReleaseDate());

                        if (date.after(yesterday)) {
                            ArtistSimplified artistSimplified = album.getArtists()[0];
                            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();
                            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(artistSimplified.getName(), artistSimplified.getHref()));
                            webhookEmbedBuilder.setImageUrl(album.getImages()[0].getUrl());

                            sendWebhooks(webhookEmbedBuilder, album.getName(), artistSimplified.getName(), album.getHref(), spotifyNotify);
                        }
                    } else {
                        ArrayList<EpisodeSimplified> episodes = SpotifyAPIHandler.getInstance().getPodcastEpisodes(actualId);
                        if (episodes.isEmpty()) continue;
                        EpisodeSimplified episode = episodes.get(0);
                        if (episode == null) continue;
                        if (episode.getReleaseDatePrecision().precision.equals("year")) continue;
                        if (episode.getReleaseDatePrecision().precision.equals("month")) continue;

                        Date date = spotifyTimestamp.parse(episode.getReleaseDate());

                        if (date.after(yesterday)) {
                            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                            webhookEmbedBuilder.setTitle(new WebhookEmbed.EmbedTitle(episode.getName(), episode.getHref()));
                            webhookEmbedBuilder.setImageUrl(episode.getImages()[0].getUrl());

                            sendWebhooks(webhookEmbedBuilder, episode.getDescription(), episode.getName(), episode.getHref(), spotifyNotify);
                        }
                    }
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                }
            }
        });
    }

    public void sendWebhooks(WebhookEmbedBuilder webhookEmbed, String description, String authorName, String redirect, List<WebhookSpotify> webhooks) {
        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

        webhookMessageBuilder.setAvatarUrl(BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl());
        webhookMessageBuilder.setUsername(BotConfig.getBotName());

        webhookEmbed.setAuthor(new WebhookEmbed.EmbedAuthor("Spotify Notifier", BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl(), null));
        webhookEmbed.setFooter(new WebhookEmbed.EmbedFooter(BotConfig.getAdvertisement(), BotWorker.getShardManager().getShards().get(0).getSelfUser().getAvatarUrl()));
        webhookEmbed.setColor(Color.GREEN.getRGB());

        webhookMessageBuilder.addEmbeds(webhookEmbed.build());

        webhooks.forEach(webhook -> {
            String message = webhook.getMessage()
                    .replace("%description%", description)
                    .replace("%author%", authorName)
                    .replace("%name%", authorName)
                    .replace("%url%", redirect);
            webhookMessageBuilder.setContent(message);
            WebhookUtil.sendWebhook(webhookMessageBuilder.build(), webhook);
        });
    }

    @Override
    public void remove(SonicIdentifier object) {
        if (SpotifyAPIHandler.getInstance() == null) return;
        if (!contains(object)) return;
        String actualId = object.getIdentifier();
        actualId = actualId.substring(actualId.lastIndexOf(':') + 1);

        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new WebhookSpotify(), "FROM WebhookSpotify WHERE entityId=:id", Map.of("id", actualId)).subscribe(webhooks -> {
            if (!webhooks.isEmpty()) return;

            spotifyEntries.remove(object);
        });
    }
}
