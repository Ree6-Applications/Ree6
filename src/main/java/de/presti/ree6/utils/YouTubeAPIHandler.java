package de.presti.ree6.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import de.presti.ree6.main.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTubeAPIHandler {

    private YouTube yt;
    public YouTubeAPIHandler instance;

    public YouTubeAPIHandler() {
        try {
            createYouTube();
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = this;
    }

    public String searchYoutube(String search) {
        if(yt == null) {
            createYouTube();
        }
        try {

            List<SearchResult> results = yt.search()
                    .list(Arrays.asList("id, snippet"))
                    .setQ(search)
                    .setMaxResults(2L)
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setKey(Main.config.getConfig().getString("youtube.api.key"))
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
            e.printStackTrace();
        }

        return null;
    }

    public void createYouTube() {
        try {
            yt = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("Ree6")
                    .build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
