package de.presti.ree6.sql.migrations;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLEntity;
import de.presti.ree6.sql.base.data.SQLParameter;
import de.presti.ree6.utils.data.MigrationUtil;
import de.presti.ree6.utils.data.SQLUtil;
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
     *
     * @param name The name of the migration.
     * @return The MigrationBuilder.
     */
    public MigrationBuilder name(String name) {
        this.migrationName = name;
        return this;
    }

    /**
     * Build and Migration.
     *
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
                String tabelName = SQLUtil.getTable(aClass);

                if (tabelName == null)
                    continue;

                StoredResultSet resultSet = sqlConnector.querySQL("SELECT * FROM " + tabelName + " LIMIT 1");
                if (resultSet != null && resultSet.hasResults()) {
                    int columns = resultSet.getColumnCount();

                    if (aClass.getSuperclass() != null && !aClass.getSuperclass().isInstance(SQLEntity.class)) {
                        for (Field field : Arrays.stream(aClass.getSuperclass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class)).toList()) {
                            boolean found = false;

                            String currentPropertyName = field.getAnnotation(Property.class).name().toUpperCase();

                            for (int i = 0; i <= columns; i++) {
                                if (resultSet.getColumnName(i).equals(currentPropertyName)) {
                                    found = true;
                                }
                            }

                            String currentTyp = SQLUtil.mapJavaToSQL(resultSet.getValue(currentPropertyName).getClass());
                            String classValueTyp = SQLUtil.mapJavaToSQL(field.getType());
                            if (!found) {
                                Main.getInstance().getLogger().info("Found a not existing column in " + aClass.getSimpleName() + ": " + currentPropertyName);
                                upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" ADD ").append(currentPropertyName).append(" ").append(classValueTyp).append(";\n");
                                downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" DROP COLUMN ").append(currentPropertyName).append(";\n");
                            } else if (!currentTyp.equals(classValueTyp)) {
                                Main.getInstance().getLogger().info("Found a not matching column in " + aClass.getSimpleName() + ": " + currentPropertyName);
                                upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" MODIFY ").append(currentPropertyName).append(" ").append(classValueTyp).append(";\n");
                                downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" MODIFY ").append(currentPropertyName).append(" ").append(currentTyp).append(";\n");
                            }
                        }
                    }

                    for (Field field : Arrays.stream(aClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Property.class)).toList()) {
                        boolean found = false;

                        String currentPropertyName = field.getAnnotation(Property.class).name().toUpperCase();

                        for (int i = 0; i <= columns; i++) {
                            if (resultSet.getColumnName(i).equals(currentPropertyName)) {
                                found = true;
                            }
                        }

                        String currentTyp = SQLUtil.mapJavaToSQL(resultSet.getValue(currentPropertyName).getClass());
                        String classValueTyp = SQLUtil.mapJavaToSQL(field.getType());
                        if (!found) {
                            Main.getInstance().getLogger().info("Found a not existing column in " + aClass.getSimpleName() + ": " + currentPropertyName);
                            upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" ADD ").append(currentPropertyName).append(" ").append(classValueTyp).append(";\n");
                            downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" DROP COLUMN ").append(currentPropertyName).append(";\n");
                        } else if (!currentTyp.equals(classValueTyp)) {
                            Main.getInstance().getLogger().info("Found a not matching column in " + aClass.getSimpleName() + ": " + currentPropertyName);
                            upQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" MODIFY ").append(currentPropertyName).append(" ").append(classValueTyp).append(";\n");
                            downQuery.append("ALTER TABLE ").append(aClass.getAnnotation(Table.class).name()).append(" MODIFY ").append(currentPropertyName).append(" ").append(currentTyp).append(";\n");
                        }
                    }
                } else {
                    Main.getInstance().getLogger().info("Could not get any data from table " + aClass.getAnnotation(Table.class).name() + ", trying to create it.");
                    Table table = aClass.getAnnotation(Table.class);
                    String tableName = table.name();
                    List<SQLParameter> sqlParameters = SQLUtil.getAllSQLParameter(aClass, false);
                    upQuery.append("CREATE TABLE ");
                    upQuery.append(tableName);
                    upQuery.append(" (");
                    sqlParameters.forEach(parameter -> {
                        upQuery.append(parameter.getName());
                        upQuery.append(" ");
                        upQuery.append(SQLUtil.mapJavaToSQL(parameter.getValue()));
                        upQuery.append(", ");
                    });

                    sqlParameters.stream().filter(SQLParameter::isPrimaryKey).findFirst().ifPresent(primaryKey -> {
                        upQuery.append("PRIMARY KEY (");
                        upQuery.append(primaryKey.getName());
                        upQuery.append(")");
                    });

                    if (upQuery.charAt(upQuery.length() - 2) == ',') {
                        upQuery.delete(upQuery.length() - 2, upQuery.length());
                    }

                    upQuery.append(");\n");

                    downQuery.append("DROP TABLE ").append(tableName).append(";\n");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        migration = new Migration() {

            @Override
            public String getName() {
                return migrationName + new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss").format(new Date());
            }

            @Override
            public String[] getUpQuery() {
                return Arrays.stream(upQuery.toString().split("\n")).filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
            }

            @Override
            public String[] getDownQuery() {
                return Arrays.stream(downQuery.toString().split("\n")).filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new);
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
