package de.presti.ree6.utils.apis;

import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.RegExUtil;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Handler class used to work quicker and easier with the Spotify API..<br>
 *
 * Original Author: <a href="https://github.com/sedmelluq/lavaplayer/issues/519#issuecomment-691219176">Kay-Bilger</a>
 */
@Slf4j
public class SpotifyAPIHandler {

    /**
     * The Spotify API.
     */
    private SpotifyApi spotifyApi;

    /**
     * The Spotify API-Handler.
     */
    @Getter
    public static SpotifyAPIHandler instance;
    
    /**
     * The Pattern for Spotify Links.
     */
    private static final Pattern pattern = Pattern.compile(RegExUtil.SPOTIFY_REGEX);

    /**
     * If spotify is connected.
     */
    @Getter
    private boolean isSpotifyConnected = false;

    /**
     * The retries for each track id.
     */
    Map<String, Integer> retries = new HashMap<>();

    /**
     * Constructor.
     */
    public SpotifyAPIHandler() {
        try {
            initSpotify();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            log.error("Couldn't create a Spotify Instance", e);
        }
        instance = this;
    }

    /**
     * Initialize the Spotify API.
     *
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if an error occurs.
     * @throws IOException            if there was a network error.
     */
    public void initSpotify() throws ParseException, SpotifyWebApiException, IOException {
        if (isSpotifyConnected) return;

        this.spotifyApi = new SpotifyApi.Builder().setClientId(Main.getInstance().getConfig().getConfiguration().getString("spotify.client.id")).setClientSecret(Main.getInstance().getConfig().getConfiguration().getString("spotify.client.secret")).build();

        try {
            ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
            ClientCredentials credentials = request.grant_type("client_credentials").build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            isSpotifyConnected = true;
        } catch (Exception exception) {
            if (exception.getMessage().equalsIgnoreCase("Invalid client")) {
                log.warn("Spotify Credentials are invalid, you can ignore this if you don't use Spotify.");
                isSpotifyConnected = false;
            } else {
                throw exception;
            }
        }

    }

    /**
     * Get the Track.
     *
     * @param trackId The Track ID.
     * @return a {@link Track} Object.
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if an error occurs.
     * @throws IOException            if there was a network error.
     */
    public Track getTrack(String trackId) throws ParseException, SpotifyWebApiException, IOException {
        if (!isSpotifyConnected) return null;
        if (retries.getOrDefault(trackId, 0) >= 3) return null;

        try {
            return spotifyApi.getTrack(trackId).build().execute();
        } catch (UnauthorizedException unauthorizedException) {
            if (spotifyApi.getClientId() != null) {
                retries.put(trackId, retries.getOrDefault(trackId, 0) + 1);

                initSpotify();
                return getTrack(trackId);
            } else {
                throw unauthorizedException;
            }
        }
    }

    /**
     * Get the Tracks on a Playlist.
     *
     * @param playlistId The Playlist ID.
     * @return a {@link java.util.List} of {@link Track} Objects.
     */
    public ArrayList<Track> getTracks(String playlistId) {
        if (!isSpotifyConnected) return new ArrayList<>();
        ArrayList<Track> tracks = new ArrayList<>();
        GetPlaylistRequest request = spotifyApi.getPlaylist(playlistId).build();
        try {
            Playlist playlist = request.execute();
            Paging<PlaylistTrack> playlistTracks = playlist.getTracks();

            for (PlaylistTrack track : playlistTracks.getItems()) {
                Track track1 = getTrack(track.getTrack().getId());
                if (track1 == null) continue;

                tracks.add(track1);
            }
        } catch (UnauthorizedException unauthorizedException) {
            if (spotifyApi.getClientId() != null) {

                try {
                    initSpotify();
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                }

                return getTracks(playlistId);
            } else {
                log.error("Couldn't get Tracks from Playlist", unauthorizedException);
            }
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            log.error("Couldn't get Tracks from Playlist", e);
        }
        return tracks;
    }

    /**
     * Get the Artist and Track Name of a Track.
     *
     * @param trackID The Track ID.
     * @return The Artist and Track Name.
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if an error occurs.
     * @throws IOException            if there was a network error.
     */
    public String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        if (!isSpotifyConnected) return "";
        StringBuilder artistNameAndTrackName;
        Track track = getTrack(trackID);
        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");

        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName()).append(" ");
        }

        return artistNameAndTrackName.toString();
    }

    /**
     * Convert a Spotify Playlist Link into a List with all Track names.
     *
     * @param link The Spotify Playlist Link.
     * @return A List with all Track names.
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if an error occurs.
     * @throws IOException            if there was a network error.
     */
    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        if (!isSpotifyConnected) return new ArrayList<>();

        String[] extraction = parseSpotifyURL(link);

        String type = extraction[0];
        String id = extraction[1];

        ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            ArrayList<Track> tracks = getTracks(id);

            tracks.stream().map(Track::getId).forEach(s -> {
                try {
                    listOfTracks.add(getArtistAndName(s));
                } catch (ParseException | SpotifyWebApiException | IOException e) {
                    log.error("Couldn't get Tracks from ID", e);
                }
            });

            return listOfTracks;
        }

        return new ArrayList<>();
    }

    /**
     * Parse a Spotify URL.
     * @param spotifyURL The Spotify URL.
     * @return The Type and ID of the URL.
     */
    public static String[] parseSpotifyURL(String spotifyURL) {
        Matcher matcher = pattern.matcher(spotifyURL);

        if (matcher.matches()) {
            return new String[]{matcher.group(2), matcher.group(3)};
        }

        return new String[]{"None", "None"};
    }
}