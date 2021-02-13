package pw.aru.api.nekos4j;

import com.github.natanbc.reliqua.limiter.RateLimiter;
import com.github.natanbc.reliqua.limiter.factory.RateLimiterFactory;
import com.github.natanbc.reliqua.request.PendingRequest;
import okhttp3.OkHttpClient;
import pw.aru.api.nekos4j.image.ImageCache;
import pw.aru.api.nekos4j.image.ImageProvider;
import pw.aru.api.nekos4j.internal.Nekos4JImpl;
import pw.aru.api.nekos4j.text.TextProvider;
import pw.aru.api.nekos4j.util.InputStreamFunction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({"unused", "WeakerAccess"})
public interface Nekos4J {
    class Builder {
        private OkHttpClient client;
        private ImageCache imageCache;
        private RateLimiterFactory rateLimiterFactory;
        private boolean trackCallSites;

        @CheckReturnValue
        @Nonnull
        public Nekos4J build() {
            return new Nekos4JImpl(
                client == null ? new OkHttpClient() : client,
                rateLimiterFactory,
                trackCallSites,
                String.format("Nekos4J/%s/%s)", NekosInfo.VERSION, NekosInfo.COMMIT),
                imageCache
            );
        }

        @CheckReturnValue
        @Nonnull
        public Builder setCallSiteTrackingEnabled(boolean enabled) {
            this.trackCallSites = enabled;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        public Builder setHttpClient(@Nullable OkHttpClient client) {
            this.client = client;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        public Builder setImageCache(@Nullable ImageCache imageCache) {
            this.imageCache = imageCache;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        @Deprecated
        public Builder setRateLimiter(RateLimiter limiter) {
            return setRateLimiterFactory(new RateLimiterFactory() {
                @Override
                protected RateLimiter createRateLimiter(String key) {
                    return limiter;
                }
            });
        }

        @CheckReturnValue
        @Nonnull
        public Builder setRateLimiterFactory(@Nullable RateLimiterFactory factory) {
            this.rateLimiterFactory = factory;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        @Deprecated
        public Builder setUserAgent(@Nullable String userAgent) {
            return this;
        }
    }

    /**
     * Downloads a given url.
     *
     * @param url    URL to download.
     * @param mapper Maps the download input stream
     * @param <T>    Type returned by the mapper
     * @return The downloaded data.
     */
    @CheckReturnValue
    @Nonnull
    <T> PendingRequest<T> download(String url, InputStreamFunction<T> mapper);

    /**
     * Returns an image provider, used to request images from the api.
     *
     * @return An image provider.
     */
    @CheckReturnValue
    @Nonnull
    ImageProvider getImageProvider();

    /**
     * Returns a text provider, used to request text and generate answers from the api.
     *
     * @return A text provider.
     */
    @CheckReturnValue
    @Nonnull
    TextProvider getTextProvider();
}
