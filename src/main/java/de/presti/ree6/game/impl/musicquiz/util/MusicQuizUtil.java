package de.presti.ree6.game.impl.musicquiz.util;

import de.presti.ree6.game.impl.musicquiz.entities.MusicQuizEntry;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.utils.others.RandomUtils;
import de.presti.ree6.utils.others.ThreadUtil;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class used to handle MusicQuiz related Utilities.
 */
@Slf4j
public class MusicQuizUtil {

    /**
     * List with songs from Spotify.
     */
    List<MusicQuizEntry> entries = new ArrayList<>();

    /**
     * Instance of the MusicQuizUtil.
     */
    private static MusicQuizUtil instance;

    /**
     * Constructor. <br>
     * This constructor will load all songs from the Spotify API.
     * <p> Reason why Spotify is being used is because it stores direct information about artists and the normal song title.
     * YouTube version will most likely have a different title and artist names like "Mr. HumanRealRaper VEVO".
     * Features are most "ft. Artist" and "feat. Artist", so it is easier just using spotify instead of making a
     * complicated regex to get the correct artist name/parser with low precision.</p>
     */
    public MusicQuizUtil() {
        instance = this;

        ThreadUtil.createThread(x -> {
            // Spotify "just hits" Playlist.
            SpotifyAPIHandler.getInstance().getTracks("37i9dQZF1DXcRXFNfZr7Tp").forEach(track -> {
                ArtistSimplified[] artistSimplified = track.getArtists();

                String url = null;

                try {
                    url = YouTubeAPIHandler.getInstance().searchYoutube(track.getName() + " - " + artistSimplified[0].getName());
                } catch (Exception e) {
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
                    url = YouTubeAPIHandler.getInstance().searchYoutube(track.getName() + " - " + artistSimplified[0].getName());
                } catch (Exception e) {
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
                    url = YouTubeAPIHandler.getInstance().searchYoutube(track.getName() + " - " + artistSimplified[0].getName());
                } catch (Exception e) {
                    log.error("Couldn't get Track from ID", e);
                }

                if (url == null) return;

                MusicQuizEntry musicQuizEntry = new MusicQuizEntry(artistSimplified[0].getName(), track.getName(),
                        Arrays.stream(artistSimplified).skip(1).map(ArtistSimplified::getName).toArray(String[]::new), url);

                entries.add(musicQuizEntry);
            });

            log.info("Loaded {} entries from Spotify.", entries.size());
        }, Sentry::captureException);
    }


    /**
     * Retrieve a random entry from this list.
     * @return A random entry.
     */
    public static MusicQuizEntry getRandomEntry() {
        return new MusicQuizEntry(instance.entries.get(RandomUtils.secureRandom.nextInt(instance.entries.size())));
    }

}
