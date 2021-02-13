package pw.aru.api.nekos4j.image;

import com.github.natanbc.reliqua.request.PendingRequest;
import org.json.JSONObject;
import pw.aru.api.nekos4j.util.IOUtils;
import pw.aru.api.nekos4j.util.InputStreamFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Image {
    @Nonnull
    @CheckReturnValue
    public static Image fromJSON(@Nonnull ImageProvider provider, @Nonnull JSONObject object) {
        return new Image(provider, object.getString("url"));
    }

    private final ImageProvider provider;
    private final String url;

    private Image(ImageProvider provider, String url) {
        this.provider = provider;
        this.url = url;
    }

    /**
     * Downloads this image and returns it's bytes.
     *
     * @return A request for this image's bytes.
     */
    @Nonnull
    @CheckReturnValue
    public PendingRequest<byte[]> download() {
        return download(IOUtils.READ_FULLY);
    }

    /**
     * Applies a given function to an InputStream of this image's bytes, returning the result.
     *
     * @param function Mapper to convert the InputStream to another form of data. <strong>The input stream is closed after the mapper returns.</strong>
     * @param <T>      Type returned by the mapper.
     * @return A request for this image's bytes.
     */
    public <T> PendingRequest<T> download(InputStreamFunction<T> function) {
        return provider.download(this, function);
    }

    /**
     * Returns the URL of this image.
     *
     * @return this image's URL.
     */
    @Nonnull
    @CheckReturnValue
    public String getUrl() {
        return url;
    }
}
