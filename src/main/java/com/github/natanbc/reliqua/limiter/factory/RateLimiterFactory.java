package com.github.natanbc.reliqua.limiter.factory;

import com.github.natanbc.reliqua.limiter.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates rate limiters for a given key.
 */
public abstract class RateLimiterFactory {
    private final Map<String, RateLimiter> rateLimiterMap;

    /**
     * Create a new rate limiter factory with a given map used to keep existing rate limiters.
     *
     * @param rateLimiterMap Map used to store created rate limiters.
     */
    protected RateLimiterFactory(Map<String, RateLimiter> rateLimiterMap) {
        this.rateLimiterMap = rateLimiterMap;
    }

    /**
     * Creates a new thread safe rate limiter factory.
     */
    protected RateLimiterFactory() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Returns the rate limiter for the given key, creating one and adding to the map if needed.
     *
     * @param key Key identifying the rate limiter.
     *
     * @return The rate limiter for the given key.
     */
    public RateLimiter getRateLimiter(String key) {
        return rateLimiterMap.computeIfAbsent(key, this::createRateLimiter);
    }

    /**
     * Creates a new rate limiter for the given key.
     *
     * @param key Key identifying the rate limiter.
     *
     * @return A rate limiter for the given key.
     */
    protected abstract RateLimiter createRateLimiter(String key);

    /**
     * Returns a rate limiter factory whose rate limiters directly handle all requests, with no throttling.
     *
     * @return A direct rate limiter factory.
     *
     *
     */
    public static RateLimiterFactory directFactory() {
        return DirectFactory.INSTANCE;
    }

    private static class DirectFactory extends RateLimiterFactory {
        static final DirectFactory INSTANCE = new DirectFactory();

        private DirectFactory() {
            super(null);
        }

        @Override
        public RateLimiter getRateLimiter(String key) {
            return RateLimiter.directLimiter();
        }

        @Override
        protected RateLimiter createRateLimiter(String key) {
            throw new UnsupportedOperationException();
        }
    }
}
