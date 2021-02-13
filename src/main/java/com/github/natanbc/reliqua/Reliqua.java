package com.github.natanbc.reliqua;

import com.github.natanbc.reliqua.limiter.RateLimiter;
import com.github.natanbc.reliqua.limiter.factory.RateLimiterFactory;
import com.github.natanbc.reliqua.util.PendingRequestBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to create REST API wrappers, providing a rate limiter and easy way to have both synchronous and asynchronous
 * requests with a common return type, leaving it up to the user to choose the method.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Reliqua {
    private final RateLimiterFactory rateLimiterFactory;
    private final OkHttpClient client;
    private boolean trackCallSites;

    /**
     * Creates a new reliqua instance.
     *
     * @param client The OkHttpClient used to make HTTP requests. May not be null.
     * @param rateLimiterFactory Factory used to create rate limiters. May be null.
     * @param trackCallSites Whether or not call sites should be tracked for async requests.
     */
    protected Reliqua(OkHttpClient client, RateLimiterFactory rateLimiterFactory, boolean trackCallSites) {
        if(client == null) {
            throw new IllegalArgumentException("Client is null");
        }
        if(rateLimiterFactory == null) {
            rateLimiterFactory = RateLimiterFactory.directFactory();
        }
        this.rateLimiterFactory = rateLimiterFactory;
        this.client = client;
        this.trackCallSites = trackCallSites;
    }

    /**
     * Creates a new reliqua with no rate limiter and with call site tracking disabled.
     *
     * @param client The OkHttpClient used to make HTTP requests. May not be null.
     */
    protected Reliqua(OkHttpClient client) {
        this(client, null, false);
    }

    /**
     * Creates a new reliqua with no rate limiter and with call site tracking disabled.
     */
    protected Reliqua() {
        this(new OkHttpClient());
    }

    /**
     * Returns the {@link OkHttpClient http client} used for making requests
     *
     * @return the client
     */
    @CheckReturnValue
    @Nonnull
    public OkHttpClient getClient() {
        return client;
    }

    /**
     * Enable or disable call site tracking.
     *
     * @param trackCallSites true to track call sites
     */
    public void setTrackCallSites(boolean trackCallSites) {
        this.trackCallSites = trackCallSites;
    }

    /**
     * Returns whether or not async requests track call site
     *
     * @return true if call site tracking is enabled
     */
    @CheckReturnValue
    public boolean isTrackingCallSites() {
        return trackCallSites;
    }

    /**
     * Returns the rate limiter factory used to create limiters.
     *
     * @return The rate limiter factory.
     */
    public RateLimiterFactory getRateLimiterFactory() {
        return rateLimiterFactory;
    }

    /**
     * Returns the rate limiter used to throttle requests to the given identifier, creating one if needed.
     *
     * @param key Key identifying the rate limiter.
     *
     * @return The rate limiter for the given key.
     */
    @CheckReturnValue
    public RateLimiter getRateLimiter(String key) {
        return rateLimiterFactory.getRateLimiter(key);
    }

    protected PendingRequestBuilder createRequest(Request request) {
        return new PendingRequestBuilder(this, request);
    }

    protected PendingRequestBuilder createRequest(Request.Builder builder) {
        return createRequest(Objects.requireNonNull(builder, "Builder may not be null").build());
    }
}
