package de.presti.ree6.sql.mapper;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.data.SQLEntity;
import de.presti.ree6.sql.base.data.SQLResponse;
import de.presti.ree6.sql.base.data.StoredResultSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is used to map an SQL Result into the right Class-Entity.
 */
public class EntityMapper {

    /**
     * This method is used to map a ResultSet into a Class-Entity.
     *
     * @param resultSet The ResultSet to map.
     * @param clazz     The Class-Entity to map the ResultSet into.
     * @return The mapped Class-Entity in the form of a {@link SQLResponse}.
     */
    public SQLResponse mapEntity(StoredResultSet resultSet, Class<?> clazz) {
        try {
            int rowCount = resultSet.getRowsCount();
            if (rowCount == 0) {
                if (resultSet.hasResults()) {
                    return new SQLResponse(mapClass(resultSet.getStoredData().get(0), clazz));
                } else {
                    return new SQLResponse(null);
                }
            } else {
                if (resultSet.hasResults()) {
                    ArrayList<Object> classes = new ArrayList<>();
                    for (StoredResultSet.StoredData entries : resultSet.getStoredData()) {
                        classes.add(mapClass(entries, clazz));
                    }
                    return new SQLResponse(classes);
                } else {
                    return new SQLResponse(null);
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't map Entity: " + clazz.getSimpleName(), e);
        }

        return new SQLResponse(null);
    }

    /**
     * This method is used to map a ResultSet into a Class-Entity.
     *
     * @param resultSet The ResultSet to map.
     * @param clazz     The Class-Entity to map the ResultSet into.
     * @return The mapped Class-Entity.
     */
    private Object mapClass(StoredResultSet.StoredData resultSet, Class<?> clazz) {
        try {
            Object entity = clazz.getDeclaredConstructor().newInstance();

            if (clazz.getSuperclass() != null && !clazz.getSuperclass().isInstance(SQLEntity.class)) {
                Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class))
                        .forEach(field -> {
                            Property property = field.getAnnotation(Property.class);
                            String columnName = property.name().toUpperCase();
                            try {
                                if (!field.canAccess(entity)) field.trySetAccessible();
                                field.set(entity, resultSet.getValue(columnName));
                            } catch (Exception e) {
                                Main.getInstance().getLogger().error("Could not set field " + field.getName() + " of class " + clazz.getSimpleName(), e);
                            }
                        });
            }

            Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class))
                    .forEach(field -> {
                        Property property = field.getAnnotation(Property.class);
                        String columnName = property.name().toUpperCase();
                        try {
                            if (!field.canAccess(entity)) field.trySetAccessible();
                            field.set(entity, resultSet.getValue(columnName));
                        } catch (Exception e) {
                            Main.getInstance().getLogger().error("Could not set field " + field.getName() + " of class " + clazz.getSimpleName(), e);
                        }
                    });

            return entity;
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't map Class: " + clazz.getSimpleName(), exception);
        }

        return null;
    }

}
