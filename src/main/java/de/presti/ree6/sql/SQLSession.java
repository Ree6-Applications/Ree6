package de.presti.ree6.sql;

import com.google.gson.JsonElement;
import de.presti.ree6.utils.data.TypUtil;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.sql.Blob;
import java.util.Base64;
import java.util.Properties;

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
     * @return The SessionFactory.
     */
    public static SessionFactory buildSessionFactory() {
        if (sessionFactory != null) return getSessionFactory();

        try {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            properties.put("hibernate.connection.datasource", "com.zaxxer.hikari.HikariDataSource");
            properties.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            properties.put("hibernate.connection.url", jdbcURL);
            properties.put("hibernate.hikari.maximumPoolSize", maxPoolSize);
            properties.put("hibernate.dialect","org.hibernate.dialect.MariaDBDialect");
            configuration.addProperties(properties);
            configuration.addPackage("de.presti.ree6.sql.entities");
            configuration.addAttributeConverter(new AttributeConverter<JsonElement, Blob>() {
                @Override
                public Blob convertToDatabaseColumn(JsonElement attribute) {
                    return TypUtil.convertJSONToBlob(attribute);
                }

                @Override
                public JsonElement convertToEntityAttribute(Blob dbData) {
                    return TypUtil.convertBlobToJSON(dbData);
                }
            });

            configuration.addAttributeConverter(new AttributeConverter<byte[], String>() {
                @Override
                public byte[] convertToEntityAttribute(String attribute) {
                    return Base64.getDecoder().decode(attribute);
                }

                @Override
                public String convertToDatabaseColumn(byte[] dbData) {
                    return Base64.getEncoder().encodeToString(dbData);
                }
            });

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

            return sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            log.error("Initial SessionFactory creation failed." + ex);
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
        return sessionFactory;
    }
}
