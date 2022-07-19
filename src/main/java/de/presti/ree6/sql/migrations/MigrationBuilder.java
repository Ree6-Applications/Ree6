package de.presti.ree6.sql.migrations;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLEntity;
import de.presti.ree6.sql.base.data.SQLParameter;
import de.presti.ree6.utils.data.StoredResultSet;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * MigrationBuilder is used to build a Migration.
 */
public class MigrationBuilder {

    /**
     * The name of the Migration.
     */
    String migrationName;

    /**
     * The Migration that has been built.
     */
    Migration migration;

    /**
     * Change the name of the migration.
     * @param name The name of the migration.
     * @return The MigrationBuilder.
     */
    public MigrationBuilder name(String name) {
        this.migrationName = name;
        return this;
    }

    /**
     * Build and Migration.
     * @param sqlConnector The SQLConnector.
     * @return The Migration.
     */
    public MigrationBuilder build(SQLConnector sqlConnector) {

        StringBuilder upQuery = new StringBuilder();
        StringBuilder downQuery = new StringBuilder();

        Reflections reflections = new Reflections("de.presti.ree6");
        Set<Class<? extends SQLEntity>> classes = reflections.getSubTypesOf(SQLEntity.class);
        for (Class<? extends SQLEntity> aClass : classes) {
            Main.getInstance().getLogger().info("Checking " + aClass.getSimpleName());

            try {
                StoredResultSet resultSet = sqlConnector.querySQL("SELECT * FROM " + aClass.getAnnotation(Table.class).name() + " LIMIT 1");
                if (resultSet != null && resultSet.hasResults()) {
                    int columns = resultSet.getColumnCount();

                    if (aClass.getSuperclass() != null && !aClass.getSuperclass().isInstance(SQLEntity.class)) {
                        for (Field field : Arrays.stream(aClass.getSuperclass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class)).toList()) {
                            boolean found = false;

                            for (int i = 1; i <= columns; i++) {
                                if (resultSet.getColumnName(i).equals(field.getAnnotation(Property.class).name())) {
                                    found = true;
                                }
                            }

                            if (!found) {
                                Main.getInstance().getLogger().info("Found a not existing column in " + aClass.getSimpleName() + ": " + field.getAnnotation(Property.class).name());
                                upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" ADD COLUMN ").append(field.getAnnotation(Property.class).name()).append(" ").append(field.getType().getSimpleName()).append(";\n");
                                downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" DROP COLUMN ").append(field.getAnnotation(Property.class).name()).append(";\n");
                            }
                        }
                    }

                    for (Field field : Arrays.stream(aClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class)).toList()) {
                        boolean found = false;

                        for (int i = 1; i <= columns; i++) {
                            if (resultSet.getColumnName(i).equals(field.getAnnotation(Property.class).name())) {
                                found = true;
                            }
                        }

                        if (!found) {
                            Main.getInstance().getLogger().info("Found a not existing column in " + aClass.getSimpleName() + ": " + field.getAnnotation(Property.class).name());
                            upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" ADD COLUMN ").append(field.getAnnotation(Property.class).name()).append(" ").append(field.getType().getSimpleName()).append(";\n");
                            downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" DROP COLUMN ").append(field.getAnnotation(Property.class).name()).append(";\n");
                        }
                    }
                } else {
                    Main.getInstance().getLogger().info("Could not get any data from table " + aClass.getAnnotation(Table.class).name() + ", trying to create it.");
                    Table table = aClass.getAnnotation(Table.class);
                    String tableName = table.name();
                    List<SQLParameter> sqlParameters =
                            Arrays.stream(aClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class))
                                    .map(e -> {
                                                Property property = e.getAnnotation(Property.class);
                                                return new SQLParameter(property.name(), e.getType(), property.primary());
                                            }
                                    ).toList();
                    upQuery.append("CREATE TABLE ");
                    upQuery.append(tableName);
                    upQuery.append(" (");
                    sqlParameters.forEach(parameter -> {
                        upQuery.append(parameter.getName());
                        upQuery.append(" ");
                        upQuery.append(parameter.getValue().getSimpleName());
                        upQuery.append(", ");
                    });

                    sqlParameters.stream().filter(SQLParameter::isPrimaryKey).findFirst().ifPresent(primaryKey -> {
                        upQuery.append("PRIMARY KEY (");
                        upQuery.append(primaryKey.getName());
                        upQuery.append(")");
                    });

                    if (upQuery.charAt(upQuery.length() - 2) == ',') {
                        upQuery.deleteCharAt(upQuery.length() - 2);
                    }

                    upQuery.append(");\n");

                    downQuery.append("DROP TABLE ").append(tableName).append(";\n");
                }
            } catch (Exception ignore) {}
        }

        migration = new Migration() {

            @Override
            public String getName() {
                return migrationName + new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss").format(new Date());
            }

            @Override
            String getUpQuery() {
                return upQuery.toString();
            }

            @Override
            String getDownQuery() {
                return downQuery.toString();
            }
        };

        return this;
    }

    /**
     * Store a migration.
     */
    public void storeMigration() {
        MigrationUtil.saveMigration(migration);
    }
}
