package de.presti.ree6.sql;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.main.Main;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import java.util.Properties;
import java.util.Set;

/**
 * Classed used as a Bridge between Hibernate and our SQL-Base.
 */
@Slf4j
public class SQLSession {

    /**
     * The JDBC URL to connect to the Database.1
     */
    static String jdbcURL;

    /**
     * The max amount of connections allowed by Hikari.
     */
    static int maxPoolSize;

    /**
     * The SessionFactory used to create Sessions.
     */
    static SessionFactory sessionFactory;

    /**
     * Build a new SessionFactory or return the current one.
     *
     * @param username the username.
     * @param password the password.
     *
     * @return The SessionFactory.
     */
    public static SessionFactory buildSessionFactory(String username, String password) {
        if (sessionFactory != null) return getSessionFactory();

        try {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            properties.put("hibernate.connection.datasource", "com.zaxxer.hikari.HikariDataSource");
            properties.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            properties.put("hibernate.connection.url", jdbcURL);
            properties.put("hibernate.connection.username", username);
            properties.put("hibernate.connection.password", password);
            properties.put("hibernate.hikari.maximumPoolSize", String.valueOf(maxPoolSize));
            properties.put("hibernate.dialect", Main.getInstance().getSqlConnector().getDatabaseTyp().getHibernateDialect());
            if (BotWorker.getVersion().isDebug()) {
                properties.put("hibernate.show_sql", true);
                properties.put("hibernate.format_sql", true);
            }
            properties.put("hibernate.hbm2ddl.auto", "update");
            properties.put("jakarta.persistence.schema-generation.database.action", "update");

            configuration.addProperties(properties);

            Set<Class<?>> classSet = new Reflections("de.presti.ree6.sql.entities").getTypesAnnotatedWith(Table.class);
            classSet.forEach(configuration::addAnnotatedClass);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

            return sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Set the JDBC URL used to connect to the Database.
     * @param jdbcURL The JDBC URL.
     */
    public static void setJdbcURL(String jdbcURL) {
        SQLSession.jdbcURL = jdbcURL;
    }

    /**
     * Set the max amount of connections allowed by Hikari.
     * @param maxPoolSize The max amount of connections.
     */
    public static void setMaxPoolSize(int maxPoolSize) {
        SQLSession.maxPoolSize = maxPoolSize;
    }

    /**
     * Get the JDBC URL used to connect to the Database.
     * @return The JDBC URL.
     */
    public static String getJdbcURL() {
        return jdbcURL;
    }

    /**
     * Get the max amount of connections allowed by Hikari.
     * @return The max amount of connections.
     */
    public static int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Get the current SessionFactory.
     * @return The SessionFactory.
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            return sessionFactory = buildSessionFactory(
                    Main.getInstance().getConfig().getConfiguration().getString("hikari.sql.user"),
                    Main.getInstance().getConfig().getConfiguration().getString("hikari.sql.pw"));
        return sessionFactory;
    }
}
