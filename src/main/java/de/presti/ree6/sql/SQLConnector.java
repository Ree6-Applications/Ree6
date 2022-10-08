package de.presti.ree6.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.migrations.MigrationUtil;
import de.presti.ree6.sql.seed.SeedManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * A "Connector" Class which connect with the used Database Server.
 * Used to manage the connection between Server and Client.
 */
@Slf4j
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class SQLConnector {

    // Various String that keep connection information to use for a connection.
    private final String databaseUser,
            databaseName,
            databasePassword,
            databaseServerIP;

    // The port of the Server.
    private final int databaseServerPort;

    // An Instance of the actual Java SQL Connection.
    private HikariDataSource dataSource;

    // An Instance of the SQL-Worker which works with the Data in the Database.
    private final SQLWorker sqlWorker;

    // A boolean to keep track if there was at least one valid connection.
    private boolean connectedOnce = false;

    // A HashMap with every Table Name as key and the values as value.
    private final Map<String, String> tables = new HashMap<>();

    /**
     * Constructor with the needed data to open an SQL connection.
     *
     * @param databaseUser       the Database Username
     * @param databaseName       the Database name
     * @param databasePassword   the Database User password
     * @param databaseServerIP   the Address of the Database Server.
     * @param databaseServerPort the Port of the Database Server.
     */
    public SQLConnector(String databaseUser, String databaseName, String databasePassword, String databaseServerIP, int databaseServerPort) {
        this.databaseUser = databaseUser;
        this.databaseName = databaseName;
        this.databasePassword = databasePassword;
        this.databaseServerIP = databaseServerIP;
        this.databaseServerPort = databaseServerPort;

        sqlWorker = new SQLWorker(this);

        connectToSQLServer();
        createTables();
        try {
            MigrationUtil.runAllMigrations(this);
        } catch (Exception exception) {
            log.error("Error while running Migrations!", exception);
        }

        SeedManager.runAllSeeds(this);
    }

    /**
     * Try to open a connection to the SQL Server with the given data.
     */
    public void connectToSQLServer() {
        log.info("Connecting to SQl-Service (SQL).");
        // Check if there is already an open Connection.
        if (isConnected()) {
            try {
                // Close if there is and notify.
                getDataSource().close();
                log.info("Service (SQL) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                log.error("Service (SQL) couldn't be stopped.");
            }
        }

        try {
            HikariConfig hConfig = new HikariConfig();

            String jdbcUrl;

            switch (Main.getInstance().getConfig().getConfiguration().getString("hikari.misc.storage").toLowerCase()) {
                case "mariadb" -> {
                    jdbcUrl = "jdbc:mariadb://%s:%s/%s?user=%s&password=%s";
                    jdbcUrl = jdbcUrl.formatted(databaseServerIP,
                            databaseServerPort,
                            databaseName,
                            databaseUser,
                            databasePassword);
                }

                case "sqlite" -> {
                    jdbcUrl = "jdbc:sqlite:%s";
                    jdbcUrl = jdbcUrl.formatted("storage/Ree6.db");
                }

                default -> {
                    jdbcUrl = "jdbc:h2:%s";
                    jdbcUrl = jdbcUrl.formatted("./storage/Ree6.db");
                }
            }

            hConfig.setJdbcUrl(jdbcUrl);
            hConfig.setMaximumPoolSize(Main.getInstance().getConfig().getConfiguration().getInt("hikari.misc.poolSize"));
            dataSource = new HikariDataSource(hConfig);
            log.info("Service (SQL) has been started. Connection was successful.");
            connectedOnce = true;
        } catch (Exception exception) {
            // Notify if there was an error.
            log.error("Service (SQL) couldn't be started. Connection was unsuccessful.", exception);
        }
    }

    /**
     * Create Tables in the Database if they aren't already set.
     */
    public void createTables() {

        // Check if there is an open Connection if not, skip.
        if (!isConnected()) return;

        // Registering the tables and values.
        tables.putIfAbsent("Migrations", "(NAME VARCHAR(100), DATE VARCHAR(100))");
        tables.putIfAbsent("Seeds", "(VERSION VARCHAR(100), DATE VARCHAR(100))");

        // Iterating through all table presets.
        for (Map.Entry<String, String> entry : tables.entrySet()) {
            //querySQL("CREATE TABLE IF NOT EXISTS " + entry.getKey() + entry.getValue());
            // TODO:: move all of these into entity classes.
        }
    }

    //region Utility

    /**
     * Send an SQL-Query to SQL-Server and get the response.
     *
     * @param sqlQuery    the SQL-Query.
     * @param parameters a list with all parameters that should be considered.
     * @return The Result from the SQL-Server.
     */
    public <R> Query<R> querySQL(R r, String sqlQuery, Map<String, Object> parameters) {

        if (!isConnected()) {
            if (connectedOnce()) {
                connectToSQLServer();
                return querySQL(r, sqlQuery, parameters);
            } else {
                return null;
            }
        }

        try (SessionFactory sessionFactory = SQLSession.buildSessionFactory()) {
            Session session = sessionFactory.getCurrentSession();

            session.beginTransaction();

            Query<R> query = (Query<R>) session.createQuery(sqlQuery);

            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            session.getTransaction().commit();

            return query;
        }
    }

    //endregion

    /**
     * Check if there is an open connection to the Database Server.
     *
     * @return boolean If the connection is opened.
     */
    public boolean isConnected() {
        try {
            return getDataSource() != null && !getDataSource().isClosed();
        } catch (Exception ignore) {
        }

        return false;
    }

    /**
     * Call to close the current Connection.
     */
    public void close() {
        // Check if there is already an open Connection.
        if (isConnected()) {
            try {
                // Close if there is and notify.
                getDataSource().close();
                log.info("Service (SQL) has been stopped.");
            } catch (Exception ignore) {
                // Notify if there was an error.
                log.error("Service (SQL) couldn't be stopped.");
            }
        }
    }

    /**
     * Retrieve an Instance of the SQL-Connection.
     *
     * @return DataSource Instance of te SQL-Connection.
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Retrieve an Instance of the SQL-Worker to work with the Data.
     *
     * @return {@link SQLWorker} the Instance saved in this SQL-Connector.
     */
    public SQLWorker getSqlWorker() {
        return sqlWorker;
    }

    /**
     * Check if there was at least one successful Connection to the Database Server.
     *
     * @return boolean If there was at least one successful Connection.
     */
    public boolean connectedOnce() {
        return connectedOnce;
    }
}