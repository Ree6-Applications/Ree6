package com.github.natanbc.reliqua.request;

import com.github.natanbc.reliqua.Reliqua;
import com.github.natanbc.reliqua.limiter.RateLimiter;
import com.github.natanbc.reliqua.util.StatusCodeValidator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/**
 * This class represents a request which has not yet been scheduled to execute.
 *
 * <br>The request is only executed when {@link #execute() execute}, {@link #async(Consumer, Consumer) async} or
 * {@link #submit() submit} are called.
 *
 * <br>This request may be executed more than once.
 *
 * This class was inspired by <a href="https://github.com/DV8FromTheWorld/JDA">JDA</a>'s
 * <a href="https://github.com/DV8FromTheWorld/JDA/blob/907f766537a18b610ed8a2cedf95cf6754cf50ee/src/main/java/net/dv8tion/jda/core/requests/RestAction.java">RestAction</a> class.
 *
 * @param <T> The type of object returned by this request.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class PendingRequest<T> {
    private final Reliqua api;
    private final Request httpRequest;
    private final StatusCodeValidator statusCodeValidator;
    private RateLimiter rateLimiter;

    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request httpRequest, @Nullable StatusCodeValidator statusCodeValidator) {
        this.api = Objects.requireNonNull(api, "API may not be null");
        this.rateLimiter = rateLimiter;
        this.httpRequest = Objects.requireNonNull(httpRequest, "HTTP request may not be null");
        this.statusCodeValidator = statusCodeValidator == null ? ignored->true : statusCodeValidator;
    }

    @Deprecated
    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request httpRequest, @Nullable IntPredicate statusCodeValidator) {
        this(api, rateLimiter, httpRequest, StatusCodeValidator.wrap(statusCodeValidator));
    }

    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request httpRequest, @Nullable StatusCodeValidator statusCodeValidator) {
        this(api, null, httpRequest, statusCodeValidator);
    }

    @Deprecated
    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request httpRequest, @Nullable IntPredicate statusCodeValidator) {
        this(api, httpRequest, StatusCodeValidator.wrap(statusCodeValidator));
    }

    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request httpRequest) {
        this(api, rateLimiter, httpRequest, null);
    }

    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request httpRequest) {
        this(api, httpRequest, null);
    }

    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request.Builder httpRequestBuilder, @Nullable StatusCodeValidator statusCodeValidator) {
        this(
                api,
                rateLimiter,
                Objects.requireNonNull(httpRequestBuilder, "HTTP request builder may not be null").build(),
                statusCodeValidator
        );
    }

    @Deprecated
    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request.Builder httpRequestBuilder, @Nullable IntPredicate statusCodeValidator) {
        this(api, rateLimiter, httpRequestBuilder, StatusCodeValidator.wrap(statusCodeValidator));
    }

    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request.Builder httpRequestBuilder, @Nullable StatusCodeValidator statusCodeValidator) {
        this(
                api,
                Objects.requireNonNull(httpRequestBuilder, "HTTP request builder may not be null").build(),
                statusCodeValidator
        );
    }

    @Deprecated
    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request.Builder httpRequestBuilder, @Nullable IntPredicate statusCodeValidator) {
        this(api, httpRequestBuilder, StatusCodeValidator.wrap(statusCodeValidator));
    }

    public PendingRequest(@Nonnull Reliqua api, @Nullable RateLimiter rateLimiter, @Nonnull Request.Builder httpRequestBuilder) {
        this(api, rateLimiter, httpRequestBuilder, null);
    }

    public PendingRequest(@Nonnull Reliqua api, @Nonnull Request.Builder httpRequestBuilder) {
        this(api, httpRequestBuilder, null);
    }

    public Reliqua getApi() {
        return api;
    }

    public Request getHttpRequest() {
        return httpRequest;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public StatusCodeValidator getStatusCodeValidator() {
        return statusCodeValidator;
    }

    @Nullable
    protected abstract T onSuccess(@Nonnull Response response) throws IOException;

    protected void onError(@Nonnull RequestContext<T> context) throws IOException {
        Response response = context.getResponse();
        ResponseBody body = response.body();

        String s = "Server returned unexpected status code " + response.code() + (body == null ? "" : " Body: " + body.string());
        response.close();
        context.getErrorConsumer().accept(new RequestException(s, context.getCallStack()));
    }

    /**
     * Execute this request asynchronously. Cancelling the returned future has no effect.
     *
     * @return A future representing this request.
     */
    @Nonnull
    public CompletionStage<T> submit() {
        CompletableFuture<T> future = new CompletableFuture<>();
        async(future::complete, future::completeExceptionally);
        return future;
    }

    /**
     * Execute this request synchronously. The current thread is blocked until it completes.
     *
     * @return The response received from the API.
     */
    public T execute() {
        try {
            return submit().toCompletableFuture().get();
        } catch(ExecutionException e) {
            throw new RequestException(e.getCause());
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestException(e);
        }
    }

    /**
     * Execute this request asynchronously, calling the appropriate callback when it's done.
     *
     * @param onSuccess Called when this request completes successfully.
     * @param onError Called when there's an error executing the request or parsing the response.
     */
    public void async(@Nullable Consumer<T> onSuccess, @Nullable Consumer<RequestException> onError) {
        StackTraceElement[] callSite = api.isTrackingCallSites() ? Thread.currentThread().getStackTrace() : null;
        if(onSuccess == null) onSuccess = v->{};
        if(onError == null) onError = Throwable::printStackTrace;

        Consumer<T> finalOnSuccess = onSuccess;
        Consumer<RequestException> finalOnError = onError;

        Runnable r = ()->
            api.getClient().newCall(httpRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                    finalOnError.accept(new RequestException(e, callSite));
                }

                @Override
                public void onResponse(@Nonnull Call call, @Nonnull Response response) {
                    try {
                        ResponseBody body = response.body();
                        if(!statusCodeValidator.test(response.code())) {
                            try {
                                onError(new RequestContext<>(
                                        callSite,
                                        finalOnSuccess,
                                        finalOnError,
                                        response
                                ));
                            } finally {
                                if(body != null) {
                                    body.close();
                                }
                            }
                            return;
                        }
                        try {
                            finalOnSuccess.accept(onSuccess(response));
                        } finally {
                            if(body != null) {
                                body.close();
                            }
                        }
                    } catch(RequestException e) {
                        finalOnError.accept(e);
                    } catch(Exception e) {
                        finalOnError.accept(new RequestException(e, callSite));
                    }
                }
            }
        );

        if(rateLimiter == null) {
            r.run();
        } else {
            rateLimiter.queue(r);
        }
    }

    /**
     * Execute this request asynchronously, calling the appropriate callback when it's done.
     *
     * @param onSuccess Called when this request completes successfully.
     */
    public void async(@Nullable Consumer<T> onSuccess) {
        async(onSuccess, null);
    }

    /**
     * Execute this request asynchronously.
     */
    public void async() {
        async(null, null);
    }

    /**
     * Executes all of the given requests, and returns a list with their results. The returned future is guaranteed to execute successfully.
     *
     * @param requests Requests to execute.
     * @param <T> Type returned by the requests.
     *
     * @return List of the results of the requests.
     */
    @CheckReturnValue
    @Nonnull
    @SafeVarargs
    public static <T, R extends PendingRequest<T>> Future<List<Result<T>>> allOf(R... requests) {
        int requestCount = requests.length;
        CompletableFuture<List<Result<T>>> f = new CompletableFuture<>();
        List<Result<T>> list = new ArrayList<>(requestCount);
        if(requestCount == 0) {
            f.complete(Collections.unmodifiableList(list));
            return f;
        }
        for(R request : requests) {
            int idx = list.size();
            //fill list with nulls so we can use List#set(int, Object)
            list.add(null);
            CompletableFuture<T> f2 = new CompletableFuture<>();
            f2.whenComplete((result,error)->{
                list.set(idx, new Result<>(result, error));
                for(Result<T> r : list) {
                    //once none are null we're done
                    if(r == null) return;
                }
                f.complete(list);
            });
            request.async(f2::complete, f2::completeExceptionally);
        }
        return f;
    }

    /**
     * Represents the result of an asynchronous request.
     *
     * @param <T> Type of the request.
     */
    public static class Result<T> {
        private final T value;
        private final Throwable exception;

        private Result(T value, Throwable exception) {
            this.value = value;
            this.exception = exception;
        }

        /**
         * Returns the value resulting from the request. Throws if there was an error in the request.
         *
         * @return The request result.
         *
         * @throws IllegalStateException If the request wasn't successful
         */
        @CheckReturnValue
        public T getValue() {
            if(exception != null) throw new IllegalStateException(exception);
            return value;
        }

        /**
         * Returns the exception that happened while executing the request. Returns null if the request was successful.
         *
         * @return The exception that happened, or null if the request was successful.
         */
        @CheckReturnValue
        @Nullable
        public Throwable getException() {
            return exception;
        }

        /**
         * Returns whether or not the request was successful.
         *
         * @return Whether or not the request was successful.
         */
        @CheckReturnValue
        public boolean isSuccess() {
            return exception == null;
        }
    }
}
