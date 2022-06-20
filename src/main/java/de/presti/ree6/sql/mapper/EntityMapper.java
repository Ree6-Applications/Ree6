package de.presti.ree6.sql.mapper;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.data.SQLResponse;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

public class EntityMapper {

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

        return null;
    }

    private Object mapClass(ResultSet resultSet, Class<?> clazz) {
        try {
            Object entity = clazz.newInstance();
            Arrays.stream(clazz.getFields()).filter(field -> field.isAnnotationPresent(Property.class))
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
