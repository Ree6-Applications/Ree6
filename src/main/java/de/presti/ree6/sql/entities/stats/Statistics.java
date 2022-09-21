package de.presti.ree6.sql.entities.stats;

import com.google.gson.JsonObject;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.entities.SQLEntity;

/**
 * SQL Entity for statistics.
 */
public class Statistics extends SQLEntity {

    /**
     * The day of the statistic.
     */
    @Property(name = "day")
    int day;

    /**
     * The month of the statistic.
     */
    @Property(name = "month")
    int month;

    /**
     * The year of the statistic.
     */
    @Property(name = "year")
    int year;

    /**
     * The {@link JsonObject} with statistics.
     */
    @Property(name = "stats", keepOriginalValue = false)
    JsonObject statsObject;

    /**
     * Constructor.
     */
    public Statistics() {
    }

    /**
     * Getter for the day of the statistic.
     * @return the day.
     */
    public int getDay() {
        return day;
    }

    /**
     * Getter for the month of the statistic.
     * @return the month.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Getter for the year of the statistic.
     * @return the year.
     */
    public int getYear() {
        return year;
    }

    /**
     * Getter for the {@link JsonObject} with statistics.
     * @return the {@link JsonObject}
     */
    public JsonObject getStatsObject() {
        return statsObject;
    }
}
