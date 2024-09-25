package de.presti.ree6.module.notifications.impl;

import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.webhook.WebhookSpotify;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.EpisodeSimplified;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpotifySonic implements ISonic {
    ArrayList<SonicIdentifier> spotifyEntries = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        // No need for this.
    }

    @Override
    public void load() {
        // Register all YouTube channels.
        SQLSession.getSqlConnector().getSqlWorker().getEntityList(new WebhookSpotify(), "FROM WebhookSpotify", Map.of()).subscribe(spotifyNotify -> {
            spotifyNotify.forEach(x -> {
                if (x.getEntityTyp() == 0) {
                    add("art:" + x.getEntityId());
                } else {
                    add("pot:" + x.getEntityId());
                }
            });
        });
    }

    @Override
    public List<SonicIdentifier> getList() {
        return spotifyEntries;
    }

    @Override
    public void run() {
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
                    if (album.getReleaseDate().equals("d")) {
                        // TODO:: add actual check and webhook send
                    }
                } else {
                    ArrayList<EpisodeSimplified> episodes = SpotifyAPIHandler.getInstance().getPodcastEpisodes(actualId);
                    if (episodes.isEmpty()) continue;
                    EpisodeSimplified episode = episodes.get(0);
                    if (episode == null) continue;
                    if (episode.getReleaseDatePrecision().precision.equals("year")) continue;
                    if (episode.getReleaseDatePrecision().precision.equals("month")) continue;
                    if (episode.getReleaseDate().equals("d")) {
                        // TODO:: add actual check and webhook send
                    }
                }
            } catch (Exception exception) {
                // TODO:: handle this shit
            }
            // SpotifyAPIHandler.getInstance().getArtistAlbums(artistId);
        }
    }

    @Override
    public void unload() {

    }

    @Override
    public void remove(SonicIdentifier object) {
        if (SpotifyAPIHandler.getInstance() == null) return;
        if (!contains(object)) return;

    }
}
