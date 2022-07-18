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

public class YouTubeAPIHandler {

    private YouTube youTube;
    public static YouTubeAPIHandler instance;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public YouTubeAPIHandler() {
        try {
            createYouTube();
        } catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't create a YouTube Instance", e);
        }
        instance = this;
    }

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

    public List<PlaylistItem> getYouTubeUploads(String channelId) throws Exception {
        List<PlaylistItem> playlistItemList = new ArrayList<>();

        YouTube.Channels.List request = youTube.channels()
                .list(Collections.singletonList("snippet, contentDetails"))
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));
        ChannelListResponse channelListResponse = request.setId(Collections.singletonList(channelId)).execute();

        if (channelListResponse != null &&
                channelListResponse.getItems() != null &&
                !channelListResponse.getItems().isEmpty()) {
            Channel channel = channelListResponse.getItems().get(0);
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

    public static YouTubeAPIHandler getInstance() {
        if (instance == null) {
            return instance = new YouTubeAPIHandler();
        }
        return instance;
    }
}
