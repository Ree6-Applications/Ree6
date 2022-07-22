package de.presti.ree6.sql.mapper;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.data.SQLEntity;
import de.presti.ree6.sql.base.data.SQLResponse;
import de.presti.ree6.sql.base.data.SQLUtil;
import de.presti.ree6.sql.base.data.StoredResultSet;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Base64;

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
                setAllFields(resultSet, entity, clazz.getSuperclass().getDeclaredFields(), clazz);
            }

            setAllFields(resultSet, entity, clazz.getDeclaredFields(), clazz);

            return entity;
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't map Class: " + clazz.getSimpleName(), exception);
        }

        return null;
    }

    /**
     * Set all fields of the given entity to the given values.
     *
     * @param resultSet For the column mappings.
     * @param entity    The entity instance for the value setting.
     * @param fields    The fields to set.
     * @param clazz     The class of the entity.
     */
    public void setAllFields(StoredResultSet.StoredData resultSet, Object entity, Field[] fields, Class<?> clazz) {
        for (Field field : fields) {
            Property property = field.getAnnotation(Property.class);
            String columnName = property.name().toUpperCase();
            try {
                if (!field.canAccess(entity)) field.trySetAccessible();

                Object value = resultSet.getValue(columnName);

                if (!property.keepOriginalValue()) {
                    if (value instanceof String valueString &&
                            field.getType().isAssignableFrom(byte[].class)) {
                        value = Base64.getDecoder().decode(valueString);
                    } else if (value instanceof Blob blob) {
                        value = SQLUtil.convertBlobToJSON(blob);
                    }
                }

                field.set(entity, value);
            } catch (Exception e) {
                Main.getInstance().getLogger().error("Could not set field " + field.getName() + " of class " + clazz.getSimpleName(), e);
            }
        }
    }
}
