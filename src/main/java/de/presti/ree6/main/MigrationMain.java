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
        instance.sqlConnector = new SQLConnector(instance.config.getConfiguration().getString("mysql.user"),
                instance.config.getConfiguration().getString("mysql.db"), instance.config.getConfiguration().getString("mysql.pw"),
                instance.config.getConfiguration().getString("mysql.host"), instance.config.getConfiguration().getInt("mysql.port"));

        new MigrationBuilder().name(args.length != 0 ? args[0] : "NOTGIVEN").build(instance.sqlConnector).storeMigration();
    }

}
