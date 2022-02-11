package de.presti.ree6.utils;

import java.util.Random;

public final class RandomUtils {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private RandomUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final Random random = new Random();

    public static int nextInt(int startInclusive, int endExclusive) {
        if (endExclusive - startInclusive <= 0) {
            return startInclusive;
        }
        return startInclusive + RandomUtils.random.nextInt(endExclusive - startInclusive);
    }

    public static double nextDouble(double startInclusive, double endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) {
            return startInclusive;
        }
        return startInclusive + (endInclusive - startInclusive) * Math.random();
    }

    public static float nextFloat(float startInclusive, float endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f) {
            return startInclusive;
        }
        return (float) (startInclusive + (endInclusive - startInclusive) * Math.random());
    }

    public static String randomNumber(int length) {
        return RandomUtils.random(length, "123456789");
    }

    public static String randomString(int length) {
        return RandomUtils.random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@/+§!$()=?]}{[");
    }

    public static String random(int length, String chars) {
        return RandomUtils.random(length, chars.toCharArray());
    }

    public static String random(int length, char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            stringBuilder.append(chars[RandomUtils.random.nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }
}