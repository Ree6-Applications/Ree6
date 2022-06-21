package de.presti.ree6.utils.external;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.main.Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// TODO rework.

/**
 * Utility used to work with HTTP Requests.
 */
public class RequestUtility {

    // HTTP Client used to send the Requests.
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    // User-Agent for all the Requests.
    private static final String USER_AGENT ="Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.52 Safari/537.36 Ree6/" + BotWorker.getBuild();

    /**
     * Send a Request.
     *
     * @param request the Request.
     * @return an {@link JsonElement}.
     */
    public static JsonElement request(Request request) {

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(request.getUri())
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json-rpc");

        if (request.bearerAuth != null) {
            httpRequestBuilder = httpRequestBuilder.header("Authorization", request.getBearerAuth());
        }

        if (request.getMethod() == Method.GET) {
            httpRequestBuilder = httpRequestBuilder.GET();
        } else if (request.getMethod() == Method.POST) {
            httpRequestBuilder = httpRequestBuilder.POST(request.bodyPublisher);
        } else if (request.getMethod() == Method.PUT) {
            httpRequestBuilder = httpRequestBuilder.PUT(request.bodyPublisher);
        }

        JsonElement jsonObject = new JsonObject();

        if (httpRequestBuilder == null) {
            jsonObject.getAsJsonObject().addProperty("success", false);
            return jsonObject;
        }

        HttpRequest httpRequest = httpRequestBuilder.build();

        if (httpRequest == null) {
            jsonObject.getAsJsonObject().addProperty("success", false);
            return jsonObject;
        }

        try {
            HttpResponse<String> httpResponse = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() == 200) {
                jsonObject = new GsonBuilder().create().fromJson(httpResponse.body(), JsonElement.class);
            }

        } catch (Exception ex) {
            Main.getInstance().getLogger().error("Couldn't send a Request!", ex);
        }

        return jsonObject;
    }

    /**
     * Utility class for Requests.
     */
    public static class Request {
        // The URL and Auth Token for the Request.
        String url, bearerAuth;

        // The Request Method.
        Method method;

        // The Body Publisher used for PUT and POST Requests.
        HttpRequest.BodyPublisher bodyPublisher;

        /**
         * Create a simple HTTP GET Requests.
         *
         * @param url the Request URL.
         */
        public Request(String url) {
            this.url = url;
            method = Method.GET;
        }

        /**
         * Create a simple HTTP GET Requests with a AuthToken.
         *
         * @param url        the Request URL.
         * @param bearerAuth the AuthToken.
         */
        public Request(String url, String bearerAuth) {
            this.url = url;
            this.bearerAuth = bearerAuth;
            method = Method.GET;
        }

        /**
         * Create a simple HTTP Requests with a AuthToken.
         *
         * @param url        the Request URL.
         * @param bearerAuth the AuthToken.
         * @param method     the wanted Method.
         */
        public Request(String url, String bearerAuth, Method method) {
            this.url = url;
            this.bearerAuth = bearerAuth;
            this.method = method;
        }

        /**
         * Create a simple HTTP Requests.
         *
         * @param url           the Request URL.
         * @param method        the wanted Method.
         * @param bodyPublisher the Body Publisher used for PUT and POST Requests.
         */
        public Request(String url, Method method, HttpRequest.BodyPublisher bodyPublisher) {
            this.url = url;
            this.method = method;
            this.bodyPublisher = bodyPublisher;
        }


        /**
         * Create a simple HTTP Requests with a AuthToken.
         *
         * @param url           the Request URL.
         * @param bearerAuth    the AuthToken.
         * @param method        the wanted Method.
         * @param bodyPublisher the Body Publisher used for PUT and POST Requests.
         */
        public Request(String url, String bearerAuth, Method method, HttpRequest.BodyPublisher bodyPublisher) {
            this.url = url;
            this.bearerAuth = bearerAuth;
            this.method = method;
            this.bodyPublisher = bodyPublisher;
        }

        /**
         * Get the Request URL.
         *
         * @return the URL.
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the URL as URI.
         *
         * @return the URL as {@link URI}
         */
        public URI getUri() {
            return URI.create(getUrl());
        }

        /**
         * Get the AuthToken.
         *
         * @return authToken.
         */
        public String getBearerAuth() {
            return bearerAuth;
        }

        /**
         * Get the HTTP Method.
         *
         * @return the Method.
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Get the Body Publisher.
         *
         * @return the Body Publisher.
         */
        public HttpRequest.BodyPublisher getBodyPublisher() {
            return bodyPublisher;
        }
    }

    /**
     * Supported Methods.
     */
    public enum Method {
        GET, PUT, POST
    }

}
