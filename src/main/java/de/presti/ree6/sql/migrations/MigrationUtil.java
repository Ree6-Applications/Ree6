package de.presti.ree6.sql.migrations;

import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.migrations.Migration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * MigrationUtil is used to manage migrations.
 */
@Slf4j
public class MigrationUtil {

    /**
     * Constructor for the MigrationUtil class.
     */
    private MigrationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Runs all stored migrations.
     *
     * @param sqlConnector The SQLConnector.
     * @throws IOException if there was an error while running the migrations.
     */
    public static void runAllMigrations(SQLConnector sqlConnector) throws IOException {
        File migrationsFolder = new File("migrations/");
        if (!migrationsFolder.exists()) {
            migrationsFolder.mkdir();
        }
        File[] files = migrationsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".migration")) {
                    runMigration(file, sqlConnector);
                }
            }
        }
    }

    /**
     * Runs a migration.
     *
     * @param file         The migration file.
     * @param sqlConnector The SQLConnector.
     * @throws IOException if there was an error while running the migration.
     */
    public static void runMigration(File file, SQLConnector sqlConnector) throws IOException {

        Migration migration = loadMigration(file);

        if (sqlConnector.querySQL("SELECT * FROM Migrations WHERE NAME=?", migration.getName()) instanceof Boolean result) {
            if (result) {
                log.info("Migration {} already ran.", migration.getName());
                return;
            }
        }

        migration.up(sqlConnector);
        log.info("Migration {} ran.", migration.getName());
    }

    /**
     * Loads a migration.
     *
     * @param file The migration file.
     * @return The loaded migration.
     * @throws IOException if there was an error while loading the migration.
     */
    public static Migration loadMigration(File file) throws IOException {
        String migrationFileContent = Files.readString(file.toPath());

        String migrationName = migrationFileContent.split("name: ")[1].split("\n")[0].trim();
        String[] upQuery = migrationFileContent.split("up: ")[1].split("down:")[0].split("\n");
        String[] downQuery = migrationFileContent.split("down: ")[1].split("\n");

        return new Migration() {
            @Override
            public String getName() {
                return migrationName;
            }

            @Override
            public String[] getUpQuery() {
                return upQuery;
            }

            @Override
            public String[] getDownQuery() {
                return downQuery;
            }
        };
    }

    /**
     * Save a migration to a file.
     *
     * @param migration The Migration.
     */
    public static void saveMigration(Migration migration) {
        log.info("Saving Migration: {}", migration.getName());
        String path = new File("migrations/").getAbsolutePath();
        File folder = new File(path);

        if (!folder.exists()) {
            folder.mkdir();
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Migrations is not a directory");
        }

        File file = new File(path, migration.getName() + ".migration");

        if (file.exists()) {
            throw new IllegalArgumentException("Migration already exists");
        }

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println("name: " + migration.getName());
            printWriter.println("up: " + String.join("\n", Arrays.stream(migration.getUpQuery()).filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new)));
            printWriter.println("down: " + String.join("\n", Arrays.stream(migration.getDownQuery()).filter(s -> !s.isEmpty() && !s.isBlank()).toArray(String[]::new)));
        } catch (Exception exception) {
            log.info("Could not save migration, reason: " + exception.getMessage(), exception);
        }
    }

}
