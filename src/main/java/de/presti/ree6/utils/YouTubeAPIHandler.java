package de.presti.ree6.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import de.presti.ree6.main.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public class YouTubeAPIHandler {

    private YouTube youTube;
    public final YouTubeAPIHandler instance;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public YouTubeAPIHandler() {
        try {
            createYouTube();
        } catch (Exception e) {
            e.printStackTrace();
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
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize(httpTransport);
            youTube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    credential)
                    .setApplicationName("Ree6")
                    .build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException If there is an error while creating a Auth-Session.
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new StringReader(Main.config.getConfig().getString("youtube.api.key")));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singletonList("snippet"))
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
