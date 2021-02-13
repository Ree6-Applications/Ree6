package com.github.natanbc.reliqua.util;

import okhttp3.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

@FunctionalInterface
public interface ResponseMapper<T> {
    @Nullable
    T apply(@Nonnull Response body) throws IOException;
}
