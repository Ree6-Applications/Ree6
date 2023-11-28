package de.presti.ree6;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class used to send Request and handle their responses easily.
 */
public class RequestUtility {

    /**
     * HTTP Client used to send the Requests.
     */
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * User-Agent for all the Requests.
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 Ree6/1.0-INSTALLER";

    /**
     * Send a Request.
     *
     * @param url the Request-Url.
     * @return an {@link String}.
     */
    public static String request(String url) {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> httpResponse = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() == 200) {
                return httpResponse.body();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "{}";
    }
}
