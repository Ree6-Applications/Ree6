package de.presti.ree6.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import de.presti.ree6.main.Main;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

            ArrayList<String> list = new ArrayList<>();
            list.add("id");
            list.add("snippet");

            List<SearchResult> results = yt.search()
                    .list(list)
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
