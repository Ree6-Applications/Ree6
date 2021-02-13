package com.github.natanbc.reliqua.request;

import okhttp3.Response;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Represents the context of a request when an error happens.
 *
 * @param <T> The type the request is supposed to return.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RequestContext<T> {
    private final StackTraceElement[] callStack;
    private final Consumer<T> successConsumer;
    private final Consumer<RequestException> errorConsumer;
    private final Response response;

    public RequestContext(StackTraceElement[] callStack, Consumer<T> successConsumer, Consumer<RequestException> errorConsumer, Response response) {
        this.callStack = callStack;
        this.successConsumer = successConsumer;
        this.errorConsumer = errorConsumer;
        this.response = response;
    }

    /**
     * Returns the response received from the server.
     *
     * @return The response received.
     */
    @Nonnull
    @CheckReturnValue
    public Response getResponse() {
        return response;
    }

    /**
     * Returns the success consumer for this request. Should only be used <strong>once</strong>.
     *
     * @return The success consumer for this request
     */
    @Nonnull
    @CheckReturnValue
    public Consumer<T> getSuccessConsumer() {
        return successConsumer;
    }

    /**
     * Returns the error consumer for this request. Should only be used <strong>once</strong>.
     *
     * @return The error consumer for this request
     */
    @Nonnull
    @CheckReturnValue
    public Consumer<RequestException> getErrorConsumer() {
        return errorConsumer;
    }

    /**
     * Returns the call stack at the time this request was created. Useful for debugging errors on requests.
     *
     * @return The call stack at the time this request was created.
     */
    @Nullable
    @CheckReturnValue
    public StackTraceElement[] getCallStack() {
        return callStack == null ? null : callStack.clone();
    }
}
