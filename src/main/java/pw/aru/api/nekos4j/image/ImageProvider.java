package pw.aru.api.nekos4j.image;

import com.github.natanbc.reliqua.request.PendingRequest;
import pw.aru.api.nekos4j.Nekos4J;
import pw.aru.api.nekos4j.util.InputStreamFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public interface ImageProvider {
    /**
     * Downloads a given image.
     *
     * @param image  Image to download.
     * @param mapper Maps the download input stream
     * @param <T>    Type returned by the mapper
     * @return The downloaded data.
     */
    @CheckReturnValue
    @Nonnull
    <T> PendingRequest<T> download(Image image, InputStreamFunction<T> mapper);

    /**
     * Returns the Weeb4J instance associated with this object.
     *
     * @return The Weeb4J instance associated with this object.
     */
    @CheckReturnValue
    @Nonnull
    Nekos4J getApi();

    /**
     * Returns the currently used image cache for this provider.
     * If no implementation was specified, a {@link ImageCache#noop() noop}
     * instance is returned.
     *
     * @return The currently used image cache.
     */
    @CheckReturnValue
    @Nonnull
    ImageCache getImageCache();

    /**
     * Sets the image cache for this provider. If null, a {@link ImageCache#noop() noop}
     * instance is used.
     *
     * @param cache Cache to use.
     */
    void setImageCache(@Nullable ImageCache cache);

    /**
     * Retrieve a random image matching the specified filters.
     *
     * @param type Image type.
     * @return A random image matching the filters.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<Image> getRandomImage(@Nonnull String type);
}
