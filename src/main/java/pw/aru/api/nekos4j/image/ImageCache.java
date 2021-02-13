package pw.aru.api.nekos4j.image;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Caches images to improve response times, lowering amount of requests required.
 */
public interface ImageCache {
    /**
     * Creates a new file based image cache.
     *
     * @param directory  Directory to store images in.
     * @param bufferSize Size of the buffer used when writing files.
     * @return A file based cache.
     * @throws IOException If the directory can't be used for the cache, due to
     *                     missing permissions or being a file.
     */
    @CheckReturnValue
    @Nonnull
    static ImageCache directory(@Nonnull File directory, @Nonnegative int bufferSize) throws IOException {
        return new FileImageCache(directory, bufferSize);
    }

    /**
     * Creates a new file based image cache.
     *
     * @param directory Directory to store images in.
     * @return A file based cache.
     * @throws IOException If the directory can't be used for the cache, due to
     *                     missing permissions or being a file.
     */
    @CheckReturnValue
    @Nonnull
    static ImageCache directory(@Nonnull File directory) throws IOException {
        return directory(directory, 4096);
    }

    /**
     * Returns an image cache that does no caching at all.
     *
     * @return A no op image cache.
     */
    @CheckReturnValue
    @Nonnull
    static ImageCache noop() {
        return NoopImageCache.INSTANCE;
    }

    /**
     * Removes an image from the cache.
     *
     * @param name Name of the image.
     * @throws IOException If an I/O error occurs.
     */
    void purge(@Nonnull String name) throws IOException;

    /**
     * Retrieves an image from the cache. If null, the image will be requested from weeb.sh.
     *
     * @param name Image name.
     * @return An input stream to the image if it's cached, or null if it isn't.
     * @throws IOException If an I/O error occurs.
     */
    @CheckReturnValue
    @Nullable
    InputStream retrieve(@Nonnull String name) throws IOException;

    /**
     * Saves an image to the cache.
     *
     * @param name Name to save.
     * @param in   Input stream to the image.
     * @throws IOException If an I/O error occurs.
     */
    void save(@Nonnull String name, @Nonnull InputStream in) throws IOException;

    /**
     * Retrieves an image from the cache if it exists, or caches it if it doesn't.
     *
     * @param name    Image name.
     * @param request Returns an input stream to the image for saving.
     * @return An input stream to the image.
     * @throws IOException If an I/O error occurs.
     */
    @CheckReturnValue
    @Nonnull
    default InputStream retrieveOrCache(@Nonnull String name, @Nonnull Supplier<InputStream> request) throws IOException {
        InputStream cached = retrieve(name);
        if (cached == null) {
            save(name, request.get());
            cached = retrieve(name);
            if (cached == null) {
                cached = request.get();
            }
        }
        return cached;
    }
}
