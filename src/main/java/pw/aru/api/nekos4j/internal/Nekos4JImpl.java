package pw.aru.api.nekos4j.internal;

import com.github.natanbc.reliqua.Reliqua;
import com.github.natanbc.reliqua.limiter.factory.RateLimiterFactory;
import com.github.natanbc.reliqua.request.PendingRequest;
import com.github.natanbc.reliqua.request.RequestException;
import com.github.natanbc.reliqua.util.StatusCodeValidator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.api.nekos4j.Nekos4J;
import pw.aru.api.nekos4j.image.Image;
import pw.aru.api.nekos4j.image.ImageCache;
import pw.aru.api.nekos4j.image.ImageProvider;
import pw.aru.api.nekos4j.text.Neko8Ball;
import pw.aru.api.nekos4j.text.TextProvider;
import pw.aru.api.nekos4j.util.InputStreamFunction;
import pw.aru.api.nekos4j.util.RequestUtils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Nekos4JImpl extends Reliqua implements Nekos4J {
    public static abstract class AbstractManager extends Reliqua {
        public final Nekos4JImpl api;

        public AbstractManager(Nekos4JImpl api) {
            super(api.getClient(), api.getRateLimiterFactory(), api.isTrackingCallSites());
            this.api = api;
        }

        @CheckReturnValue
        @Nonnull
        public Nekos4J getApi() {
            return api;
        }
    }

    public static class ImageProviderImpl extends AbstractManager implements ImageProvider {
        private volatile ImageCache cache = ImageCache.noop();

        public ImageProviderImpl(Nekos4JImpl api) {
            super(api);
        }

        @Nonnull
        @Override
        public <T> PendingRequest<T> download(Image image, InputStreamFunction<T> mapper) {
            return new PendingRequest<T>(api, null, new Request.Builder().url(image.getUrl()).build(), null) {
                @Nullable
                @Override
                protected T onSuccess(@Nonnull Response response) {
                    return null;
                }

                @Override
                public void async(@Nullable Consumer<T> onSuccess, @Nullable Consumer<RequestException> onError) {
                    if (onSuccess == null) return;
                    api.getClient().dispatcher().executorService().submit(() -> {
                        try {
                            String id = new URL(image.getUrl()).getQuery().replace("/", "_");
                            InputStream in = cache.retrieve(id);
                            if (in != null) {
                                try (InputStream i = in) {
                                    onSuccess.accept(mapper.accept(i));
                                }
                                return;
                            }
                            api.download(image.getUrl(), is -> {
                                cache.save(id, is);
                                return cache.retrieve(id);
                            }).async(is -> {
                                try (InputStream i = is) {
                                    onSuccess.accept(mapper.accept(i));
                                } catch (IOException e) {
                                    if (onError != null) {
                                        onError.accept(new RequestException(e));
                                    }
                                }
                            }, onError);
                        } catch (IOException e) {
                            if (onError != null) {
                                onError.accept(new RequestException(e));
                            }
                        }
                    });
                }
            };
        }

        @Nonnull
        @Override
        public ImageCache getImageCache() {
            return cache;
        }

        @Override
        public void setImageCache(@Nullable ImageCache cache) {
            this.cache = cache == null ? ImageCache.noop() : cache;
        }

        @CheckReturnValue
        @Nonnull
        @Override
        public PendingRequest<Image> getRandomImage(@Nonnull String type) {
            String endpoint = "/img/" + type;
            return createRequest(api.newRequestBuilder(API_BASE + endpoint))
                .setRateLimiter(getRateLimiter(endpoint))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> Image.fromJSON(this, RequestUtils.toJSONObject(response)), RequestUtils::handleError);
        }
    }

    public static class TextProviderImpl extends AbstractManager implements TextProvider {
        public TextProviderImpl(Nekos4JImpl api) {
            super(api);
        }

        @Nonnull
        @Override
        public PendingRequest<String> generateAnswer(@Nonnull String text) {
            return createRequest(api.newRequestBuilder(API_BASE + "/chat?text=" + encode(text)))
                .setRateLimiter(getRateLimiter("/chat"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> RequestUtils.toJSONObject(response).getString("response"), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<String> owoifyText(@Nonnull String text) {
            return createRequest(api.newRequestBuilder(API_BASE + "/owoify?text=" + encode(text)))
                .setRateLimiter(getRateLimiter("/owoify"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> RequestUtils.toJSONObject(response).getString("owo"), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<Neko8Ball> getRandom8Ball() {
            return createRequest(api.newRequestBuilder(API_BASE + "/8ball"))
                .setRateLimiter(getRateLimiter("/8ball"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> Neko8Ball.fromJSON(RequestUtils.toJSONObject(response)), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<String> getRandomCat() {
            return createRequest(api.newRequestBuilder(API_BASE + "/cat"))
                .setRateLimiter(getRateLimiter("/cat"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> RequestUtils.toJSONObject(response).getString("cat"), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<String> getRandomQuestion() {
            return createRequest(api.newRequestBuilder(API_BASE + "/why"))
                .setRateLimiter(getRateLimiter("/why"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> RequestUtils.toJSONObject(response).getString("fact"), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<String> getRandomFact() {
            return createRequest(api.newRequestBuilder(API_BASE + "/fact"))
                .setRateLimiter(getRateLimiter("/fact"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(response -> RequestUtils.toJSONObject(response).getString("fact"), RequestUtils::handleError);
        }

        @Nonnull
        @Override
        public PendingRequest<List<String>> getEndpoints() {
            return createRequest(api.newRequestBuilder(API_BASE + "/endpoints"))
                .setRateLimiter(getRateLimiter("/endpoints"))
                .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
                .build(
                    response -> StreamSupport.stream(
                        RequestUtils.toJSONArray(response).spliterator(), false
                    ).map(Object::toString).collect(Collectors.toList()),
                    RequestUtils::handleError
                );
        }

        private String encode(String s) {
            try {
                return URLEncoder.encode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final Logger LOGGER = LoggerFactory.getLogger("NekosJ");
    private static final String API_BASE = "https://nekos.life/api/v2";
    private final ImageProviderImpl imageProvider;
    private final TextProviderImpl textProvider;
    private final String userAgent;

    public Nekos4JImpl(OkHttpClient client, RateLimiterFactory factory, boolean trackCallSites, String userAgent, ImageCache imageCache) {
        super(client, factory, trackCallSites);
        this.userAgent = userAgent;
        this.imageProvider = new ImageProviderImpl(this);
        this.imageProvider.setImageCache(imageCache);
        this.textProvider = new TextProviderImpl(this);
    }

    @CheckReturnValue
    @Nonnull
    @Override
    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    @CheckReturnValue
    @Nonnull
    @Override
    public TextProvider getTextProvider() {
        return textProvider;
    }

    @CheckReturnValue
    @Nonnull
    @Override
    public <T> PendingRequest<T> download(String url, InputStreamFunction<T> function) {
        Objects.requireNonNull(url, "URL may not be null");
        Objects.requireNonNull(function, "Function may not be null");
        return createRequest(newRequestBuilder(url))
            .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
            .build(response -> function.accept(RequestUtils.getInputStream(response)), RequestUtils::handleError);
    }

    @Override
    public void setTrackCallSites(boolean trackCallSites) {
        super.setTrackCallSites(trackCallSites);
        imageProvider.setTrackCallSites(trackCallSites);
    }

    @CheckReturnValue
    @Nonnull
    public Request.Builder newRequestBuilder(@Nonnull String url) {
        return new Request.Builder()
            .header("User-Agent", userAgent)
            .header("Accept-Encoding", "gzip, deflate") //we can handle gzip data
            .url(url)
            .get();
    }

}
