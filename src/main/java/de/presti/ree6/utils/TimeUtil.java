package de.presti.ree6.utils;

import java.time.LocalDateTime;
import java.time.Period;

public class TimeUtil {

    public static Period getDifferenceBetween(LocalDateTime start, LocalDateTime end) {
        return Period.between(start.toLocalDate(), end.toLocalDate());
    }

    public static String getFormattedDate(Period period) {
        String end = "";

        if(period.getYears() != 0) {
            end+= period.getYears() + " years ago";
        }

        if(period.getMonths() != 0 && period.getYears() == 0) {
            end+= period.getMonths() + " months ago";
        }

        if(period.getDays() != 0 && period.getMonths() == 0 && period.getYears() == 0) {
            end+= period.getDays() + " days ago";
        }

        return end;
    }

    public static String getTime(long t) {
        String end = "";

        long current = System.currentTimeMillis();
        long dif = current - t;

        if(dif == 0) {
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

        if(d != 0) {
            end += d + " Days" + (((h != 0 || m != 0 || s != 0) ? ", " : ""));
        }

        if(h != 0) {
            end += h + " Hours" + (((m != 0 || s != 0) ? ", " : ""));
        }

        if(m != 0) {
            end += m + " Minutes" + ((s != 0 ? ", " : ""));
        }

        if(s != 0) {
            end += s + " Seconds";
        }

        return end;
    }

}