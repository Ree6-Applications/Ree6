package de.presti.ree6.utils.apis;

import de.presti.ree6.main.Main;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
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

/**
 * SpotifyAPIHandler.
 *
 * @author Kay-Bilger
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
    public static SpotifyAPIHandler instance;

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
     * @throws SpotifyWebApiException if the and error occurs.
     * @throws IOException            if there was a network error.
     */
    public void initSpotify() throws ParseException, SpotifyWebApiException, IOException {
        this.spotifyApi = new SpotifyApi.Builder().setClientId(Main.getInstance().getConfig().getConfiguration().getString("spotify.client.id")).setClientSecret(Main.getInstance().getConfig().getConfiguration().getString("spotify.client.secret")).build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds = request.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(creds.getAccessToken());
    }

    /**
     * Convert a Spotify Playlist Link into a List with all Track names.
     *
     * @param link The Spotify Playlist Link.
     * @return A List with all Track names.
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if the and error occurs.
     * @throws IOException            if there was a network error.
     */
    public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = link.split("/");
        String[] secondSplit;

        String type;
        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        String id = secondSplit[0];
        ArrayList<String> listOfTracks = new ArrayList<>();

        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }

        if (type.contentEquals("playlist")) {
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                String trackID = track.getId();
                listOfTracks.add(getArtistAndName(trackID));
            }

            return listOfTracks;
        }

        return new ArrayList<>();
    }

    /**
     * Get the Artist and Track Name of a Track.
     *
     * @param trackID The Track ID.
     * @return The Artist and Track Name.
     * @throws ParseException         if the response is not a Valid JSON.
     * @throws SpotifyWebApiException if the and error occurs.
     * @throws IOException            if there was a network error.
     */
    public String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        StringBuilder artistNameAndTrackName;
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();

        Track track = trackRequest.execute();
        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");

        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName()).append(" ");
        }

        return artistNameAndTrackName.toString();
    }

    /**
     * Get the Spotify API.
     * @return The Spotify API.
     */
    public static SpotifyAPIHandler getInstance() {
        return instance;
    }
}