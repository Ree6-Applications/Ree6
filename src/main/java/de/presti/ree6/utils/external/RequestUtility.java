package de.presti.ree6.utils.external;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import de.presti.ree6.bot.BotWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility used to work with HTTP Requests.
 */
@Slf4j
public class RequestUtility {

    /**
     * HTTP Client used to send the Requests.
     */
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * User-Agent for all the Requests.
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36  Ree6/" + BotWorker.getBuild();

    /**
     * Send a Request.
     *
     * @param request the Request.
     * @return an {@link InputStream}.
     */
    public static InputStream request(Request request) {

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(request.getUri())
                .header("User-Agent", USER_AGENT);

        if (request.getHeaders().isEmpty()) {
            httpRequestBuilder = httpRequestBuilder.header("Content-Type", "application/json-rpc");
        } else {
            for (String[] header : request.getHeaders()) {
                if (header.length == 2) {
                    httpRequestBuilder = httpRequestBuilder.header(header[0], header[1]);
                }
            }
        }

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

        if (httpRequestBuilder == null) {
            return null;
        }

        HttpRequest httpRequest = httpRequestBuilder.build();

        if (httpRequest == null) {
            return null;
        }

        try {
            HttpResponse<InputStream> httpResponse = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (httpResponse.statusCode() == 200) {
                return httpResponse.body();
            }

        } catch (Exception ex) {
            log.error("Couldn't send a Request!", ex);
        }

        return null;
    }

    /**
     * Send a Request.
     *
     * @param request the Request.
     * @return an {@link JsonElement}.
     */
    public static JsonElement requestJson(Request request) {
        JsonElement jsonObject = new JsonObject();
        try (InputStream httpResponse = request(request)) {

            if (httpResponse == null) {
                jsonObject.getAsJsonObject().addProperty("success", false);
                return jsonObject;
            }

            String content = IOUtils.toString(httpResponse, StandardCharsets.UTF_8);

            if (content.isEmpty()) {
                jsonObject.getAsJsonObject().addProperty("success", false);
                return jsonObject;
            }

            try {
                JsonStreamParser jsonStreamParser = new JsonStreamParser(content);

                if (jsonStreamParser.hasNext()) {
                    jsonObject = jsonStreamParser.next();
                } else {
                    jsonObject.getAsJsonObject().addProperty("success", false);
                }

                return jsonObject;
            } catch (Exception ex) {
                log.error("Couldn't send a Request!", ex);
                log.error("Content: " + content);
            }
        } catch (IOException e) {
            log.error("Couldn't send a Request!", e);
            jsonObject.getAsJsonObject().addProperty("success", false);
            return jsonObject;
        }

        return jsonObject;
    }

    /**
     * Send a Request.
     * @param request the Request.
     * @return a {@link byte[]}
     */
    public static byte[] requestBytes(Request request) {
        try (InputStream httpResponse = request(request)) {

            if (httpResponse == null) {
                return new byte[0];
            }

            try {
                return httpResponse.readAllBytes();
            } catch (Exception ex) {
                log.error("Couldn't send a Request!", ex);
            }
        } catch (IOException e) {
            log.error("Couldn't send a Request!", e);
            return new byte[0];
        }

        return new byte[0];
    }

    /**
     * Send a Request.
     * @param request the Request.
     * @return a {@link String}
     */
    public static String requestString(Request request) {
        try (InputStream httpResponse = request(request)) {

            if (httpResponse == null) {
                return "";
            }

            try {
                Scanner scanner = new Scanner(httpResponse);
                StringBuilder stringBuilder = new StringBuilder();

                while(scanner.hasNext()){
                    stringBuilder.append(scanner.nextLine());
                }

                return stringBuilder.toString();
            } catch (Exception ex) {
                log.error("Couldn't send a Request!", ex);
            }
        } catch (IOException e) {
            log.error("Couldn't send a Request!", e);
            return "";
        }

        return "";
    }

    /**
     * Utility class for Requests.
     */
    public static class Request {

        /**
         * The URL for the Request.
         */
        String url;

        /**
         * The Bearer Auth Token for the Request.
         */
        String bearerAuth;

        /**
         * The Request Method.
         */
        Method method = Method.GET;

        /**
         * The Body Publisher used for PUT and POST Requests.
         */
        HttpRequest.BodyPublisher bodyPublisher;

        /**
         * Custom Headers.
         */
        List<String[]> headers = new ArrayList<>();

        /**
         * Create a new Request builder.
         *
         * @return a new Request builder.
         */
        public static RequestBuilder builder() {
            return new RequestBuilder();
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

        /**
         * Get the Headers.
         *
         * @return the Headers.
         */
        public List<String[]> getHeaders() {
            return headers;
        }

        /**
         * Builder class for a Request class.
         */
        public static class RequestBuilder {

            /**
             * The URL for the Request.
             */
            String url;

            /**
             * The Bearer Auth Token for the Request.
             */
            String bearerAuth;

            /**
             * The Request Method.
             */
            Method method = Method.GET;

            /**
             * The Body Publisher used for PUT and POST Requests.
             */
            HttpRequest.BodyPublisher bodyPublisher;

            /**
             * Custom Headers.
             */
            List<String[]> headers = new ArrayList<>();

            /**
             * Change the Url of the Request.
             *
             * @param url the new Url.
             * @return the Request.
             */
            public RequestBuilder url(String url) {
                this.url = url;
                return this;
            }

            /**
             * Change the Bearer Auth Token.
             *
             * @param bearerAuth the new Auth Token.
             * @return the Request.
             */
            public RequestBuilder bearerAuth(String bearerAuth) {
                this.bearerAuth = bearerAuth;
                return this;
            }

            /**
             * Change the Request method.
             *
             * @param method the new Method.
             * @return the Request.
             */
            public RequestBuilder method(Method method) {
                this.method = method;
                return this;
            }

            /**
             * Change the Request method to GET.
             * @return the RequestBuilder.
             */
            public RequestBuilder GET() {
                this.method = Method.GET;
                return this;
            }

            /**
             * Change the Request method to POST.
             * @return the RequestBuilder.
             */
            public RequestBuilder POST() {
                this.method = Method.POST;
                return this;
            }

            /**
             * Change the Request method to PUT.
             * @return the RequestBuilder.
             */
            public RequestBuilder PUT() {
                this.method = Method.PUT;
                return this;
            }

            /**
             * Change the Body publisher used.
             *
             * @param bodyPublisher the Body Publisher used for PUT and POST Requests.
             * @return the Request.
             */
            public RequestBuilder bodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
                this.bodyPublisher = bodyPublisher;
                return this;
            }

            /**
             * Change the Headers.
             *
             * @param header the new Header.
             * @return the Request.
             */
            public RequestBuilder header(String[] header) {
                this.headers.add(header);
                return this;
            }

            /**
             * Build the Request.
             *
             * @return the Request.
             */
            public Request build() {
                Request request = new Request();
                request.url = this.url;
                request.method = this.method;
                request.bodyPublisher = this.bodyPublisher;
                request.headers = this.headers;
                request.bearerAuth = this.bearerAuth;
                return request;
            }
        }
    }

    /**
     * Supported Methods.
     */
    public enum Method {
        /**
         * The GET Method.
         */
        GET,

        /**
         * The POST Method.
         */
        PUT,

        /**
         * The POST Method.
         */
        POST
    }

}
