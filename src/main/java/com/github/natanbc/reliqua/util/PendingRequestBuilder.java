package com.github.natanbc.reliqua.util;

import com.github.natanbc.reliqua.Reliqua;
import com.github.natanbc.reliqua.limiter.RateLimiter;
import com.github.natanbc.reliqua.request.PendingRequest;
import com.github.natanbc.reliqua.request.RequestContext;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.IntPredicate;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PendingRequestBuilder {
    private final Reliqua api;
    private final Request request;
    private RateLimiter rateLimiter;
    private StatusCodeValidator statusCodeValidator;

    public PendingRequestBuilder(@Nonnull Reliqua api, @Nonnull Request request) {
        this.api = Objects.requireNonNull(api, "API may not be null");
        this.request = Objects.requireNonNull(request, "Request may not be null");
    }

    @Nonnull
    @CheckReturnValue
    public PendingRequestBuilder setRateLimiter(@Nullable RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public PendingRequestBuilder setStatusCodeValidator(@Nullable StatusCodeValidator statusCodeValidator) {
        this.statusCodeValidator = statusCodeValidator;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public PendingRequestBuilder setStatusCodeValidator(@Nullable IntPredicate predicate) {
        return setStatusCodeValidator(StatusCodeValidator.wrap(predicate));
    }

    @Nonnull
    @CheckReturnValue
    public <T>PendingRequest<T> build(@Nonnull ResponseMapper<T> mapper, @Nullable ErrorHandler<T> errorHandler) {
        Objects.requireNonNull(mapper, "Mapper may not be null");
        return new PendingRequest<T>(api, rateLimiter, request, statusCodeValidator) {
            @Nullable
            @Override
            protected T onSuccess(@Nonnull Response response) throws IOException {
                return mapper.apply(response);
            }

            @Override
            protected void onError(@Nonnull RequestContext<T> context) throws IOException {
                if(errorHandler != null) {
                    errorHandler.apply(context);
                } else {
                    super.onError(context);
                }
            }
        };
    }
}
