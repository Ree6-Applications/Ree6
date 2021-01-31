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

}