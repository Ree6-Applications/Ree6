package de.presti.ree6.sql.migrations;

import de.presti.ree6.sql.SQLConnector;

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
    public abstract String getUpQuery();

    /**
     * The Query being called on migration deletion.
     * @return The Query.
     */
    public abstract String getDownQuery();


    /**
     * Call when the Migration is being loaded.
     *
     * @param sqlConnector the current Instance of the {@link SQLConnector}.
     */
    public void up(SQLConnector sqlConnector) {
        sqlConnector.querySQL(getUpQuery());
    }

    /**
     * Call when the Migration is being removed.
     *
     * @param sqlConnector the current Instance of the {@link SQLConnector}.
     */
    public void down(SQLConnector sqlConnector){
        sqlConnector.querySQL(getDownQuery());
    }

}
