package com.github.natanbc.reliqua.limiter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public abstract class RateLimiter {
    /**
     * Callback to be notified about rate limits.
     *
     * <br>The routes given to the callbacks might be null in case of a global rate limit.
     */
    public interface Callback {
        /**
         * Called when an attempted request is rate limited. Might be called more than once per request
         */
        void requestRateLimited();

        /**
         * Called when the rate limit is reset.
         */
        void rateLimitReset();
    }

    /**
     * Queue a task to be handled at a future time, respecting rate limits.
     *
     * @param task Task to be executed.
     */
    public abstract void queue(@Nonnull Runnable task);

    /**
     * Get how many requests may still be done before the rate limit is hit and no more requests can be made.
     *
     * @return Remaining requests.
     */
    @Nonnegative
    @CheckReturnValue
    public abstract int getRemainingRequests();

    /**
     * Get how much time, in milliseconds, is left until the rate limit is reset.
     *
     * @return Remaining cooldown time.
     */
    @CheckReturnValue
    public abstract long getTimeUntilReset();

    /**
     * Creates a child rate limiter, whose requests increase this limiter's counter, but have a separate cooldown.
     *
     * @param requests Requests that can be done before needing a cooldown.
     * @param cooldown Cooldown time, in milliseconds.
     * @param callback Callback to be notified about rate limits.
     *
     * @return A new child rate limiter.
     */
    @CheckReturnValue
    @Nonnull
    public RateLimiter createChildLimiter(int requests, long cooldown, Callback callback) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a child rate limiter, whose requests increase this limiter's counter, but have a separate cooldown.
     *
     * @param requests Requests that can be done before needing a cooldown.
     * @param cooldown Cooldown time, in milliseconds.
     *
     * @return A new child rate limiter.
     */
    @CheckReturnValue
    @Nonnull
    public RateLimiter createChildLimiter(int requests, long cooldown) {
        return createChildLimiter(requests, cooldown, null);
    }

    /**
     * Creates a new rate limiter that does no handling of rate limits, useful for situations where few requests are made.
     *
     * <br>When using this method, you are responsible for handling rate limits.
     *
     * @return A direct rate limiter.
     */
    @Nonnull
    @CheckReturnValue
    public static RateLimiter directLimiter() {
        return DirectLimiter.INSTANCE;
    }

    private static class DirectLimiter extends RateLimiter {
        static final DirectLimiter INSTANCE = new DirectLimiter();

        private DirectLimiter() {}

        @Override
        public void queue(@Nonnull Runnable task) {
            task.run();
        }

        @Override
        public int getRemainingRequests() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long getTimeUntilReset() {
            return 0;
        }

        @Nonnull
        @Override
        public RateLimiter createChildLimiter(int requests, long cooldown) {
            return this;
        }
    }
}
