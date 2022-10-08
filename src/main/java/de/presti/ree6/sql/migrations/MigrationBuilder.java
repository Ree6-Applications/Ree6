package de.presti.ree6.sql.migrations;

import de.presti.ree6.sql.SQLConnector;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * MigrationBuilder is used to build a Migration.
 */
@Slf4j
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

        // TODO:: find a new way to do this.

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
