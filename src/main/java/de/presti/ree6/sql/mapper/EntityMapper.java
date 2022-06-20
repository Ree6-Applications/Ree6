package de.presti.ree6.sql.mapper;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Query;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLParameter;
import de.presti.ree6.sql.base.data.SQLResponse;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityMapper {

    private SQLConnector sqlConnector;

    public EntityMapper(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public boolean createTable(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name();
        List<SQLParameter> sqlParameters =
                Arrays.stream(clazz.getFields()).filter(field -> field.isAnnotationPresent(Property.class))
                        .map(e -> {
                                    Property property = e.getAnnotation(Property.class);
                                    return new SQLParameter(property.name(), e.getType(), property.primary());
                                }
                        ).toList();
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE ");
        query.append(tableName);
        query.append(" (");
        sqlParameters.forEach(parameter -> {
            query.append(parameter.getName());
            query.append(" ");
            query.append(parameter.getValue().getSimpleName());
            query.append(", ");
        });

        sqlParameters.stream().filter(SQLParameter::isPrimaryKey).findFirst().ifPresent(primaryKey -> {
            query.append("PRIMARY KEY (");
            query.append(primaryKey.getName());
            query.append(")");
        });

        if (query.charAt(query.length() - 1) == ',') {
            query.deleteCharAt(query.length() - 1);
        }

        query.append(")");

        try(ResultSet resultSet = sqlConnector.querySQL(query.toString())) {
            return resultSet != null && resultSet.next();
        } catch (Exception e) {
            return false;
        }
    }

    public SQLResponse mapEntity(ResultSet resultSet, Class<?> clazz) {
        try {
            resultSet.last();
            int rowCount = resultSet.getRow();
            resultSet.beforeFirst();
            if (rowCount == 0) {
                return new SQLResponse(mapClass(resultSet, clazz));
            } else {
                ArrayList<Class<?>> classes = new ArrayList<>();
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

    private Class<?> mapClass(ResultSet resultSet, Class<?> clazz) {
        try {
            Object entity = clazz.newInstance();
            Arrays.stream(clazz.getFields()).filter(field -> field.isAnnotationPresent(Property.class))
                    .forEach(field -> {
                        Property property = field.getAnnotation(Property.class);
                        String columnName = property.name();
                        try {
                            field.set(entity, resultSet.getObject(columnName));
                        } catch (Exception e) {
                            Main.getInstance().getLogger().error("Could not set field " + field.getName() + " of class " + clazz.getName());
                        }
                    });
            return entity.getClass();
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Couldn't map Class: " + exception.getMessage());
        }

        return null;
    }

}
