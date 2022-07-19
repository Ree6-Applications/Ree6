package de.presti.ree6.sql.mapper;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.data.SQLResponse;

import java.sql.ResultSet;
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
    public SQLResponse mapEntity(ResultSet resultSet, Class<?> clazz) {
        try {
            resultSet.last();
            int rowCount = resultSet.getRow();
            resultSet.beforeFirst();
            if (rowCount == 0) {
                return new SQLResponse(mapClass(resultSet, clazz));
            } else {
                ArrayList<Object> classes = new ArrayList<>();
                while (resultSet.next()) {
                    classes.add(mapClass(resultSet, clazz));
                }
                return new SQLResponse(classes);
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().error("Couldn't map Entity: " + e.getMessage());
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
    private Object mapClass(ResultSet resultSet, Class<?> clazz) {
        try {
            Object entity = clazz.newInstance();
            Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class))
                    .forEach(field -> {
                        Property property = field.getAnnotation(Property.class);
                        String columnName = property.name();
                        try {
                            if (!field.canAccess(entity)) field.trySetAccessible();
                            field.set(entity, resultSet.getObject(columnName));
                        } catch (Exception e) {
                            Main.getInstance().getLogger().error("Could not set field " + field.getName() + " of class " + clazz.getName());
                        }
                    });
            return entity;
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't map Class: " + exception.getMessage());
        }

        return null;
    }

}
