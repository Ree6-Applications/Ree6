package de.presti.ree6.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import de.presti.ree6.main.Main;

import java.util.Collections;
import java.util.List;

public class YouTubeAPIHandler {

    private YouTube youTube;
    public final YouTubeAPIHandler instance;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public YouTubeAPIHandler() {
        try {
            createYouTube();
        } catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't create a YouTube Instance", e);
        }
        instance = this;
    }

    public String searchYoutube(String search) {
        if(youTube == null) {
            createYouTube();
        }
        try {

            List<SearchResult> results = youTube.search()
                    .list(Collections.singletonList("snippet"))
                    .setQ(search)
                    .setMaxResults(2L)
                    .setKey(Main.getInstance().getConfig().getConfig().getString("youtube.api.key"))
                    .execute()
                    .getItems();

            if(!results.isEmpty()) {
                String videoId = results.get(0).getId().getVideoId();


                if(videoId == null) {
                    createYouTube();
                    return searchYoutube(search);
                } else {
                    return "https://www.youtube.com/watch?v=" + videoId;
                }
            }

        }
        catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't search on YouTube", e);
        }

        return null;
    }

    public void createYouTube() {
        try {
            youTube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    null)
                    .setApplicationName("Ree6")
                    .build();
        }
        catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't create a YouTube Instance", e);
        }
    }
}
