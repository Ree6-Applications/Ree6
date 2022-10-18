package de.presti.ree6.utils.others;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * Utility class for random things.
 */
public final class RandomUtils {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private RandomUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * An instance of the used Random.
     */
    public static final Random random = new Random();

    /**
     * An instance of the used SecureRandom.
     */
    public static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a random number between the given min and the given max.
     * @param startInclusive The minimum number.
     * @param endExclusive The maximum number.
     * @return A random number between the given min and the given max.
     */
    public static int nextInt(int startInclusive, int endExclusive) {
        if (endExclusive - startInclusive <= 0) {
            return startInclusive;
        }
        return startInclusive + RandomUtils.random.nextInt(endExclusive - startInclusive);
    }

    /**
     * Generates a random number between the given min and the given max.
     * @param startInclusive The minimum number.
     * @param endInclusive The maximum number.
     * @return A random number between the given min and the given max.
     */
    public static double nextDouble(double startInclusive, double endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) {
            return startInclusive;
        }
        return startInclusive + (endInclusive - startInclusive) * Math.random();
    }

    /**
     * Generates a random number between the given min and the given max.
     * @param startInclusive The minimum number.
     * @param endInclusive The maximum number.
     * @return A random number between the given min and the given max.
     */
    public static float nextFloat(float startInclusive, float endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f) {
            return startInclusive;
        }
        return (float) (startInclusive + (endInclusive - startInclusive) * Math.random());
    }

    /**
     * Generates a random number with the given amount of digits.
     * @param length The amount of digits.
     * @return A random number with the given amount of digits.
     */
    public static String randomNumber(int length) {
        return RandomUtils.random(length, "123456789");
    }

    /**
     * Generates a string with the given amount of characters.
     * @param length The amount of characters.
     * @return A string with the given amount of characters.
     */
    public static String randomString(int length) {
        return RandomUtils.random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@/+§!$()=?]}{[");
    }

    /**
     * Generates a string with the given length and amount of characters.
     * @param length The amount of characters.
     * @param chars The characters to use.
     * @return A string with the given amount of characters.
     */
    public static String random(int length, String chars) {
        return RandomUtils.random(length, chars.toCharArray());
    }

    /**
     * Generates a string with the given length and amount of characters.
     * @param length The amount of characters.
     * @param chars The characters to use.
     * @return A string with the given amount of characters.
     */
    public static String random(int length, char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            stringBuilder.append(chars[RandomUtils.random.nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }

    /**
     * Create a new Base64 String.
     *
     * @param length The length of the String.
     *
     * @return {@link String} A Base64 String.
     */
    public static String getRandomBase64String(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }
}