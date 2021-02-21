package de.presti.ree6.utils;


import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import de.presti.ree6.main.Main;

import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;

//Not by DxsSucuk
//Created by Kay-Bilger
public class LinkConverter {

	
	private SpotifyApi spotifyApi;
	private static LinkConverter instance;
	private String id;
	private String type;
	
	public LinkConverter() {
		try {
			initSpotify();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			e.printStackTrace();
		}
		instance = this;
	}
	
	private void initSpotify() throws ParseException, SpotifyWebApiException, IOException {
		this.spotifyApi = new SpotifyApi.Builder()
				  .setClientId(Main.config.getConfig().getString("spotify.client.id"))
				  .setClientSecret(Main.config.getConfig().getString("spotify.client.secret"))
				  .build();
		
		ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
		ClientCredentials creds = request.grant_type("client_credentials").build().execute();
		spotifyApi.setAccessToken(creds.getAccessToken());
	}
	
	public ArrayList<String> convert(String link) throws ParseException, SpotifyWebApiException, IOException {
		String[] firstSplit = link.split("/");
		String[] secondSplit;
		
		if(firstSplit.length > 5) {
			secondSplit = firstSplit[6].split("\\?");
			this.type = firstSplit[5];
		} else {
			secondSplit = firstSplit[4].split("\\?");
			this.type = firstSplit[3];
		}
		this.id = secondSplit[0];
		ArrayList<String> listOfTracks = new ArrayList<>();
		
		if(type.contentEquals("track")) {
			listOfTracks.add(getArtistAndName(id));
			return listOfTracks;
		}
		
		if(type.contentEquals("playlist")) {
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
		
		return null;
	}
	
	private String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
		String artistNameAndTrackName = "";
		GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();
		
		Track track = trackRequest.execute();
		artistNameAndTrackName = track.getName() + " - ";
		
		ArtistSimplified[] artists = track.getArtists();
		for(ArtistSimplified i : artists) {
			artistNameAndTrackName += i.getName() + " ";
		}
	
		return artistNameAndTrackName;
	}
	
	public static LinkConverter getInstance() {
		return instance;
	}
}