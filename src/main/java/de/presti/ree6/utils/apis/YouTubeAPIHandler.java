package de.presti.ree6.utils.apis;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import de.presti.ree6.main.Main;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * YouTubeAPIHandler.
 */
@Slf4j
public class YouTubeAPIHandler {

    /**
     * The YouTube API.
     */
    private YouTube youTube;

    /**
     * The YouTube API-Handler.
     */
    public static YouTubeAPIHandler instance;

    /**
     * Instance of the JsonFactory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Constructor.
     */
    public YouTubeAPIHandler() {
        try {
            createYouTube();
        } catch (Exception e) {
            log.error("Couldn't create a YouTube Instance", e);
        }
        instance = this;
    }

    /**
     * Search on YouTube a specific query.
     *
     * @param search The query.
     * @return A link to the first Video result.
     * @throws Exception if there was a search problem.
     */
    public String searchYoutube(String search) throws Exception {
        if (youTube == null) {
            createYouTube();
        }
        List<SearchResult> results = youTube.search()
                .list(Collections.singletonList("snippet"))
                .setQ(search)
                .setMaxResults(2L)
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"))
                .execute()
                .getItems();

        if (!results.isEmpty()) {
            String videoId = results.get(0).getId().getVideoId();


            if (videoId == null) {
                createYouTube();
                return searchYoutube(search);
            } else {
                return "https://www.youtube.com/watch?v=" + videoId;
            }
        }

        return null;
    }

    /**
     * Get the YouTube uploads of a specific user.
     *
     * @param channelId The channel id.
     * @return A list of all Video ids.
     * @throws Exception if something went wrong.
     */
    public List<PlaylistItem> getYouTubeUploads(String channelId) throws Exception {
        List<PlaylistItem> playlistItemList = new ArrayList<>();

        Channel channel = isValidChannelId(channelId) ?
                getYouTubeChannelById(channelId, "snippet, contentDetails") :
                getYouTubeChannelByName(channelId, "snippet, contentDetails");

        if (channel != null) {
            YouTube.PlaylistItems.List playlistItemRequest =
                    youTube.playlistItems().list(Collections.singletonList("id,contentDetails,snippet"));
            playlistItemRequest.setPlaylistId(channel.getContentDetails().getRelatedPlaylists().getUploads());
            playlistItemRequest.setFields(
                    "items(contentDetails/videoId,snippet/title,snippet/description,snippet/thumbnails,snippet/publishedAt,snippet/channelTitle),nextPageToken,pageInfo");
            playlistItemRequest.setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));

            String nextToken = "";
            while (nextToken != null) {
                playlistItemRequest.setPageToken(nextToken);
                PlaylistItemListResponse playlistItemListResponse = playlistItemRequest.execute();

                playlistItemList.addAll(playlistItemListResponse.getItems());
                nextToken = playlistItemListResponse.getNextPageToken();
            }
        }

        return playlistItemList;
    }

    /**
     * Get an YouTube channel by id.
     * @param channelName The channel name.
     * @param listValues The values to get.
     * @return The channel.
     * @throws IOException if something went wrong.
     */
    public Channel getYouTubeChannelBySearch(String channelName, String listValues) throws IOException {
        YouTube.Search.List request = youTube.search().list(Collections.singletonList("snippet"))
                .setQ(channelName)
                .setType(Collections.singletonList("channel"))
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));
        SearchListResponse channelListResponse = request.execute();

        if (channelListResponse != null &&
                channelListResponse.getItems() != null &&
                !channelListResponse.getItems().isEmpty()) {
            SearchResult searchResult = channelListResponse.getItems().get(0);

            if (searchResult.getId().getKind().equals("youtube#channel")) {
                return getYouTubeChannelById(searchResult.getId().getChannelId(), listValues);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get an YouTube channel by id.
     * @param channelName The channel name.
     * @param listValues The values to get.
     * @return The channel.
     * @throws IOException if something went wrong.
     */
    public Channel getYouTubeChannelByName(String channelName, String listValues) throws IOException {
        YouTube.Channels.List request = youTube.channels()
                .list(Collections.singletonList(listValues))
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));
        ChannelListResponse channelListResponse = request.setForUsername(channelName).execute();

        if (channelListResponse != null &&
                channelListResponse.getItems() != null &&
                !channelListResponse.getItems().isEmpty()) {
            return channelListResponse.getItems().get(0);
        } else {
            return null;
        }
    }

    /**
     * Get an YouTube channel by id.
     * @param channelId The channel id.
     * @param listValues The values to get.
     * @return The channel.
     * @throws IOException if something went wrong.
     */
    public Channel getYouTubeChannelById(String channelId, String listValues) throws IOException {
        YouTube.Channels.List request = youTube.channels()
                .list(Collections.singletonList(listValues))
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));
        ChannelListResponse channelListResponse = request.setId(Collections.singletonList(channelId)).execute();

        if (channelListResponse != null &&
                channelListResponse.getItems() != null &&
                !channelListResponse.getItems().isEmpty()) {
            return channelListResponse.getItems().get(0);
        } else {
            return null;
        }
    }

    /**
     * Check if a given channel ID matches the pattern of a YouTube channel ID.
     * @param channelId The channel ID.
     * @return True if it matches, false if not.
     */
    public boolean isValidChannelId(String channelId) {
        return channelId.matches("^UC[\\w-]{21}[AQgw]$");
    }

    /**
     * Create a YouTube instance.
     */
    public void createYouTube() {
        try {
            youTube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    null)
                    .setApplicationName("Ree6")
                    .build();
        } catch (Exception e) {
            log.error("Couldn't create a YouTube Instance", e);
        }
    }

    /**
     * Method used to return an instance of the handler.
     * @return instance of the handler.
     */
    public static YouTubeAPIHandler getInstance() {
        if (instance == null) {
            return instance = new YouTubeAPIHandler();
        }
        return instance;
    }
}
