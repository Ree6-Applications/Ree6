package de.presti.ree6.game.impl.musicquiz.util;

import de.presti.ree6.game.impl.musicquiz.entities.MusicQuizEntry;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MusicQuizUtil {

    List<MusicQuizEntry> entries = new ArrayList<>();
    private static MusicQuizUtil instance;

    public MusicQuizUtil() {
        instance = this;

        // Spotify "just hits" Playlist.
        SpotifyAPIHandler.getInstance().getTracks("37i9dQZF1DXcRXFNfZr7Tp").forEach(track -> {
            ArtistSimplified[] artistSimplified = track.getArtists();

            String url = null;

            try {
                url = SpotifyAPIHandler.getInstance().convert("https://open.spotify.com/track/" + track.getId()).get(0);
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                log.error("Couldn't get Track from ID", e);
            }

            if (url == null) return;

            MusicQuizEntry musicQuizEntry = new MusicQuizEntry(artistSimplified[0].getName(), track.getName(),
                    Arrays.stream(artistSimplified).skip(1).map(ArtistSimplified::getName).toArray(String[]::new), url);

            entries.add(musicQuizEntry);
        });

        // Spotify "Today's Top Hits" Playlist.
        SpotifyAPIHandler.getInstance().getTracks("37i9dQZF1DXcBWIGoYBM5M").forEach(track -> {
            ArtistSimplified[] artistSimplified = track.getArtists();

            String url = null;

            try {
                url = SpotifyAPIHandler.getInstance().convert("https://open.spotify.com/track/" + track.getId()).get(0);
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                log.error("Couldn't get Track from ID", e);
            }
            
            if (url == null) return;
            
            MusicQuizEntry musicQuizEntry = new MusicQuizEntry(artistSimplified[0].getName(), track.getName(),
                    Arrays.stream(artistSimplified).skip(1).map(ArtistSimplified::getName).toArray(String[]::new), url);

            entries.add(musicQuizEntry);
        });

        // Spotify "Rap Caviar" Playlist.
        SpotifyAPIHandler.getInstance().getTracks("37i9dQZF1DX0XUsuxWHRQd").forEach(track -> {
            ArtistSimplified[] artistSimplified = track.getArtists();

            String url = null;

            try {
                url = SpotifyAPIHandler.getInstance().convert("https://open.spotify.com/track/" + track.getId()).get(0);
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                log.error("Couldn't get Track from ID", e);
            }

            if (url == null) return;

            MusicQuizEntry musicQuizEntry = new MusicQuizEntry(artistSimplified[0].getName(), track.getName(),
                    Arrays.stream(artistSimplified).skip(1).map(ArtistSimplified::getName).toArray(String[]::new), url);

            entries.add(musicQuizEntry);
        });
    }

    public static MusicQuizEntry getRandomEntry() {
        return new MusicQuizEntry(instance.entries.get((int) (Math.random() * instance.entries.size())));
    }

}
