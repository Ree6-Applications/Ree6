package pw.aru.api.nekos4j.text;

import org.json.JSONObject;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class Neko8Ball {
    @Nonnull
    @CheckReturnValue
    public static Neko8Ball fromJSON(@Nonnull JSONObject object) {
        return new Neko8Ball(
            object.getString("response"),
            object.getString("url")
        );
    }

    private final String response;
    private final String url;

    public Neko8Ball(String response, String url) {
        this.response = response;
        this.url = url;
    }

    public String getResponse() {
        return response;
    }

    public String getURL() {
        return url;
    }
}
