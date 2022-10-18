package de.presti.ree6.main;

import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.migrations.MigrationBuilder;
import de.presti.ree6.utils.data.Config;

/**
 * Class used as start method to start a Migration.
 */
public class MigrationMain {

    /**
     * Instance.
     */
    private static MigrationMain instance;
    /**
     * SQLConnector.
     */
    SQLConnector sqlConnector;
    /**
     * Config.
     */
    Config config;

    /**
     * Constructor.
     */
    public MigrationMain() {
        instance = this;
    }

    /**
     * Main method.
     * @param args Arguments.
     */
    public static void main(String[] args) {

        instance = new MigrationMain();

        // Create the Config System Instance.
        instance.config = new Config();

        // Initialize the Config.
        instance.config.init();

        // Create a new connection between the Application and the SQL-Server.
        instance.sqlConnector = new SQLConnector(instance.config.getConfiguration().getString("hikari.sql.user"),
                instance.config.getConfiguration().getString("hikari.sql.db"), instance.config.getConfiguration().getString("hikari.sql.pw"),
                instance.config.getConfiguration().getString("hikari.sql.host"), instance.config.getConfiguration().getInt("hikari.sql.port"));

        new MigrationBuilder().name(args.length != 0 ? args[0] : "NOTGIVEN").build(instance.sqlConnector).storeMigration();
    }
}
