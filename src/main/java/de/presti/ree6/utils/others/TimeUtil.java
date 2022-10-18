package de.presti.ree6.utils.others;

import java.time.LocalDateTime;
import java.time.Period;

/**
 * Utility class for the Time.
 */
public class TimeUtil {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private TimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the difference between the two given LocalDateTime.
     *
     * @param start The start LocalDateTime.
     * @param end   The end LocalDateTime.
     * @return The difference between the two given LocalDateTime.
     */
    public static Period getDifferenceBetween(LocalDateTime start, LocalDateTime end) {
        return Period.between(start.toLocalDate(), end.toLocalDate());
    }

    /**
     * Get a formatted String of a Time-period.
     *
     * @param period The Time-period.
     * @return A formatted String of a Time-period.
     */
    public static String getFormattedDate(Period period) {
        String end = "";

        if (period.getYears() != 0) {
            end += period.getYears() + " years ago";
        }

        if (period.getMonths() != 0 && period.getYears() == 0) {
            end += period.getMonths() + " months ago";
        }

        if (period.getDays() != 0 && period.getMonths() == 0 && period.getYears() == 0) {
            end += period.getDays() + " days ago";
        }

        return end;
    }

    /**
     * Get a specific time amount that is presented as milliseconds as seconds.
     *
     * @param time The time amount.
     * @return The time amount as seconds.
     */
    public static int getTimeinSec(long time) {
        long current = System.currentTimeMillis();
        long dif = current - time;

        int s = 0;

        while (dif >= 1000) {
            dif -= 1000;
            ++s;
        }
        return s;
    }

    /**
     * Get a specific time amount that is presented as seconds as minutes.
     *
     * @param s The time amount.
     * @return The time amount as minutes.
     */
    public static int getTimeinMin(int s) {

        int m = 0;

        while (s >= 60) {
            s -= 60;
            ++m;
        }

        return m;

    }

    /**
     * Get a formatted String of the difference between the given time and the current Time.
     *
     * @param t The time.
     * @return A formatted String of the difference between the given time and the current Time.
     */
    public static String getTime(long t) {
        String end = "";

        long current = System.currentTimeMillis();
        long dif = current - t;

        if (dif == 0) {
            return "0 Seconds";
        }

        int s = 0;
        int m = 0;
        int h = 0;
        int d = 0;

        while (dif >= 1000) {
            dif -= 1000;
            ++s;
        }
        while (s >= 60) {
            s -= 60;
            ++m;
        }
        while (m >= 60) {
            m -= 60;
            ++h;
        }
        while (h >= 24) {
            h -= 24;
            ++d;
        }

        if (d != 0) {
            end += d + " Days" + ((h != 0 || m != 0 || s != 0) ? ", " : "");
        }

        if (h != 0) {
            end += h + " Hours" + ((m != 0 || s != 0) ? ", " : "");
        }

        if (m != 0) {
            end += m + " Minutes" + (s != 0 ? ", " : "");
        }

        if (s != 0) {
            end += s + " Seconds";
        }

        return end;
    }
}