package de.presti.ree6.sql.migrations;

import de.presti.ree6.sql.SQLConnector;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Migration for Database changes.
 */
public abstract class Migration {

    /**
     * Get the name of the Migration.
     *
     * @return the name of the Migration.
     */
    public abstract String getName();

    /**
     * The Query that is being called on Migration load.
     * @return The Query.
     */
    public abstract String[] getUpQuery();

    /**
     * The Query being called on migration deletion.
     * @return The Query.
     */
    public abstract String[] getDownQuery();


    /**
     * Call when the Migration is being loaded.
     *
     * @param sqlConnector the current Instance of the {@link SQLConnector}.
     */
    public void up(SQLConnector sqlConnector) {
        Arrays.stream(getUpQuery()).filter(s -> !s.isEmpty() && !s.isBlank()).forEach(sqlConnector::querySQL);
        sqlConnector.querySQL("INSERT INTO Migrations (NAME, DATE) VALUES (?, ?)", getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * Call when the Migration is being removed.
     *
     * @param sqlConnector the current Instance of the {@link SQLConnector}.
     */
    public void down(SQLConnector sqlConnector){
        Arrays.stream(getDownQuery()).filter(s -> !s.isEmpty() && !s.isBlank()).forEach(sqlConnector::querySQL);
        sqlConnector.querySQL("DELETE FROM Migrations WHERE NAME = ?", getName());
    }

}
