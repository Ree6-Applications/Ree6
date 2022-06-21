package de.presti.ree6.utils.apis;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import de.presti.ree6.main.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * YouTubeAPIHandler.
 */
public class YouTubeAPIHandler {

    /**
     * The YouTube API.
     */
    private YouTube youTube;

    /**
     * The YouTube API-Handler.
     */
    public final YouTubeAPIHandler instance;

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
            Main.getInstance().getLogger().error("Couldn't create a YouTube Instance", e);
        }
        instance = this;
    }

    /**
     * Search on YouTube a specific query.
     *
     * @param search The query.
     * @return A link to the first Video result.
     */
    public String searchYoutube(String search) {
        if (youTube == null) {
            createYouTube();
        }
        try {

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

        } catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't search on YouTube", e);
        }

        return null;
    }

    /**
     * Get the YouTube uploads of a specific user.
     *
     * @param channelId The channel id.
     * @return A list of all Video ids.
     */
    public List<PlaylistItem> getYouTubeUploads(String channelId) {
        List<PlaylistItem> playlistItemList = new ArrayList<>();
        try {
            YouTube.Channels.List request = youTube.channels().list(Collections.singletonList("snippet, contentDetails"));
            ChannelListResponse channelListResponse = request.setId(Collections.singletonList(channelId)).execute();

            if (!channelListResponse.getItems().isEmpty()) {
                Channel channel = channelListResponse.getItems().get(0);
                YouTube.PlaylistItems.List playlistItemRequest =
                        youTube.playlistItems().list(Collections.singletonList("id,contentDetails,snippet"));
                playlistItemRequest.setPlaylistId(channel.getContentDetails().getRelatedPlaylists().getUploads());
                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

                String nexToken = "";
                while (nexToken != null) {
                    playlistItemRequest.setPageToken(nexToken);
                    PlaylistItemListResponse playlistItemListResponse = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemListResponse.getItems());
                    nexToken = playlistItemListResponse.getNextPageToken();
                }
            }
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't search on YouTube", exception);
        }

        return playlistItemList;
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
            Main.getInstance().getLogger().error("Couldn't create a YouTube Instance", e);
        }
    }
}
