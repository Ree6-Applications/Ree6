package com.github.natanbc.reliqua.util;

import com.github.natanbc.reliqua.request.RequestContext;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ErrorHandler<T> {
    void apply(@Nonnull RequestContext<T> context);
}
