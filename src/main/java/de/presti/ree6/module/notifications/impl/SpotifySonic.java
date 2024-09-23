package de.presti.ree6.module.notifications.impl;

import de.presti.ree6.module.notifications.ISonic;
import de.presti.ree6.module.notifications.SonicIdentifier;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.*;

@Slf4j
public class SpotifySonic implements ISonic {
    ArrayList<SonicIdentifier> spotifyArtist = new ArrayList<>();
    ArrayList<SonicIdentifier> spotifyPodcasts = new ArrayList<>();

    @Override
    public void load(List<ChannelStats> channelStats) {
        // No need for this.
    }

    @Override
    public void load() {
        // Register all YouTube channels.
        SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeChannels().subscribe(channel ->
                channel.forEach(this::add));
    }

    @Override
    public List<SonicIdentifier> getList() {
        return spotifyArtist;
    }

    @Override
    public void run() {
        for (String artistId : spotifyArtist.stream().map(SonicIdentifier::getIdentifier).toList()) {
            // SpotifyAPIHandler.getInstance().getArtistAlbums(artistId);
        }

        for (String podcastId : spotifyPodcasts.stream().map(SonicIdentifier::getIdentifier).toList()) {
           // SpotifyAPIHandler.getInstance().getPodcastEpisodes(podcastId);
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
