package com.github.natanbc.reliqua.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntPredicate;

@SuppressWarnings("unused")
@FunctionalInterface
public interface StatusCodeValidator extends IntPredicate {
    StatusCodeValidator ACCEPT_ALL = code->true;
    StatusCodeValidator ACCEPT_2XX = code->code % 100 == 2;
    StatusCodeValidator ACCEPT_200 = code->code == 200;
    StatusCodeValidator ACCEPT_204 = code->code == 204;

    @CheckReturnValue
    @Nonnull
    static StatusCodeValidator acceptAny(int... codes) {
        return c->{
            for(int i : codes) {
                if(c == i) return true;
            }
            return false;
        };
    }

    @CheckReturnValue
    @Nonnull
    static StatusCodeValidator accept(int code) {
        return c->c == code;
    }

    @CheckReturnValue
    @Nonnull
    static StatusCodeValidator wrap(@Nullable IntPredicate predicate) {
        return predicate == null ? ACCEPT_ALL : predicate::test;
    }
}
