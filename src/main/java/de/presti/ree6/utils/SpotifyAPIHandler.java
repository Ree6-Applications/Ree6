package de.presti.ree6.utils;

import de.presti.ree6.main.Main;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.*;
import se.michaelthelin.spotify.exceptions.*;
import se.michaelthelin.spotify.model_objects.credentials.*;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.*;
import se.michaelthelin.spotify.requests.data.playlists.*;
import se.michaelthelin.spotify.requests.data.tracks.*;

import java.io.IOException;
import java.util.ArrayList;

//Not by DxsSucuk
//Credits: Kay-Bilger
public class SpotifyAPIHandler {


    private SpotifyApi spotifyApi;
    public static SpotifyAPIHandler instance;

    public SpotifyAPIHandler() {
        try {
            initSpotify();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            Main.getInstance().getLogger().error("Couldn't create a Spotify Instance", e);
        }
        instance = this;
    }

    public void initSpotify() throws ParseException, SpotifyWebApiException, IOException {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(Main.getInstance().getConfig().getConfig().getString("spotify.client.id"))
                .setClientSecret(Main.getInstance().getConfig().getConfig().getString("spotify.client.secret"))
                .build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds = request.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(creds.getAccessToken());
    }

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

    public static SpotifyAPIHandler getInstance() {
        return instance;
    }
}