package de.presti.ree6.utils.apis;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.ThreadUtil;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * YouTubeAPIHandler.
 */
public class YouTubeAPIHandler {

    /**
     * The YouTube API.
     */
    private YouTube youTube;
    public static YouTubeAPIHandler instance;

    /**
     * The YouTube API-Handler.
     */
    public static YouTubeAPIHandler instance;

    /**
     * Instance of the JsonFactory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * List with all channels and consumers for notifications.
     */
    private final HashMap<String, Consumer<PlaylistItem>> listenerChannelId = new HashMap<>();

    /**
     * Constructor.
     */
    public YouTubeAPIHandler() {
        try {
            createYouTube();
            createUploadStream();
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

        YouTube.Channels.List request = youTube.channels()
                .list(Collections.singletonList("snippet, contentDetails"))
                .setKey(Main.getInstance().getConfig().getConfiguration().getString("youtube.api.key"));
        ChannelListResponse channelListResponse = request.setId(Collections.singletonList(channelId)).execute();

        if (!channelListResponse.getItems().isEmpty()) {
            Channel channel = channelListResponse.getItems().get(0);
            YouTube.PlaylistItems.List playlistItemRequest =
                    youTube.playlistItems().list(Collections.singletonList("id,contentDetails,snippet"));
            playlistItemRequest.setPlaylistId(channel.getContentDetails().getRelatedPlaylists().getUploads());
            playlistItemRequest.setFields(
                    "items(snippet/title,snippet/description,snippet/thumbnails,snippet/publishedAt),nextPageToken,pageInfo");
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

    /**
     * Creates an Thread for the Upload-Notifier.
     */
    public void createUploadStream() {
        ThreadUtil.createNewASyncThread(x -> {
            try {
                for (Map.Entry<?, ?> channelListener : listenerChannelId.entrySet()) {
                    String channelId = (String) channelListener.getKey();
                    Consumer<PlaylistItem> listener = (Consumer<PlaylistItem>) channelListener.getValue();
                    List<PlaylistItem> playlistItemList = getYouTubeUploads(channelId);
                    if (!playlistItemList.isEmpty()) {
                        for (PlaylistItem playlistItem : playlistItemList) {
                            PlaylistItemSnippet snippet = playlistItem.getSnippet();
                            DateTime dateTime = snippet.getPublishedAt();
                            if (dateTime != null &&
                                    dateTime.getValue() > System.currentTimeMillis() - Duration.ofMinutes(5).toMillis()) {
                                listener.accept(playlistItem);
                            }
                        }
                    }
                }
                System.out.println("Finished Listening");
            } catch (Exception e) {
                Main.getInstance().getLogger().error("Couldn't get Upload data!", e);

            }
        }, x -> {
            Main.getInstance().getLogger().error("Couldn't start Upload Stream!");
        }, Duration.ofMinutes(5));
    }

    /**
     * Check if the notifier is listening for this specific channel.
     *
     * @param channelId the ChannelId.
     */
    public boolean isListening(String channelId) {
        return listenerChannelId.containsKey(channelId);
    }

    /**
     * Add an specific channel to the notifier.
     *
     * @param channelId the ChannelId.
     * @param success   the success consumer.
     */
    public void addChannelToListener(String channelId, Consumer<PlaylistItem> success) {
        if (isListening(channelId)) {
            return;
        }

        listenerChannelId.put(channelId, success);
    }

    /**
     * Remove an channel from the notifier.
     * @param channelId the ChannelId.
     */
    public void removeChannelFromListener(String channelId) {
        if (!isListening(channelId)) {
            return;
        }

        listenerChannelId.remove(channelId);
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
