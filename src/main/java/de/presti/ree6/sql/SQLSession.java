package de.presti.ree6.sql;

import com.google.gson.JsonElement;
import de.presti.ree6.utils.data.TypUtil;
import jakarta.persistence.AttributeConverter;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.sql.Blob;
import java.util.Base64;
import java.util.Properties;

public class SQLSession {

    static String jdbcURL;
    static int maxPoolSize;

    public static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            properties.put("hibernate.connection.datasource", "com.zaxxer.hikari.HikariDataSource");
            properties.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            properties.put("hibernate.connection.url", jdbcURL);
            properties.put("hibernate.hikari.maximumPoolSize", maxPoolSize);
            properties.put("hibernate.dialect","org.hibernate.dialect.MariaDBDialect");
            configuration.addProperties(properties);
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

            return configuration.buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

}
