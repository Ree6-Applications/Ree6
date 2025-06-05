package de.presti.ree6.module.game.impl.musicquiz.util;

import de.presti.ree6.main.Main;
import de.presti.ree6.module.game.impl.musicquiz.entities.MusicQuizEntry;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.utils.others.RandomUtils;
import de.presti.ree6.utils.others.ThreadUtil;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
            if (!SpotifyAPIHandler.getInstance().isSpotifyConnected()) return;

            var playListIds = Main.getInstance().getConfig().getConfiguration().getStringList("bot.misc.game.musicQuiz.playlistIds");

            log.info("Loading {} Spotify Playlists.", playListIds.size());

            for (String playlistId : playListIds) {
                try {
                    var tracks = SpotifyAPIHandler.getInstance().getTracks(playlistId);
                    if (tracks == null || tracks.isEmpty()) {
                        log.warn("No tracks found in Spotify Playlist: {}", playlistId);
                        continue;
                    } else {
                        log.info("Loading {} entries from Spotify Playlist: {}", tracks.size(), playlistId);
                    }
                    tracks.forEach(track -> {
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
                } catch (Exception e) {
                    log.error("Couldn't get tracks from Spotify Playlist: {}", playlistId, e);
                }
            }

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
