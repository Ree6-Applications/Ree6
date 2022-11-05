package de.presti.ree6.sql.migrations;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.SQLConnector;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
     */
    public static void runAllMigrations(SQLConnector sqlConnector) {
        Reflections reflections = new Reflections("sql",Scanners.Resources);

        for (String url : reflections.getResources(".*\\.migration")) {
            if (url.endsWith(".migration")) {
                try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(url)) {
                    if (inputStream == null) break;

                    String migrationContent = new String(inputStream.readAllBytes());

                    runMigration(migrationContent, sqlConnector);
                } catch (Exception exception) {
                    log.error("Couldn't create run Migration!", exception);
                }
            }
        }
    }

    /**
     * Runs a migration.
     *
     * @param content      The migration content.
     * @param sqlConnector The SQLConnector.
     * @throws IOException if there was an error while running the migration.
     */
    public static void runMigration(String content, SQLConnector sqlConnector) throws IOException {
        runMigration(loadMigration(content), sqlConnector);
    }

    /**
     * Runs a migration.
     *
     * @param file         The migration file.
     * @param sqlConnector The SQLConnector.
     * @throws IOException if there was an error while running the migration.
     */
    public static void runMigration(File file, SQLConnector sqlConnector) throws IOException {
        runMigration(loadMigration(file), sqlConnector);
    }

    /**
     * Runs a migration.
     *
     * @param migration    The migration.
     * @param sqlConnector The SQLConnector.
     * @throws IOException if there was an error while running the migration.
     */
    public static void runMigration(Migration migration, SQLConnector sqlConnector) {
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
     * @param content The migration content.
     * @return The loaded migration.
     * @throws IOException if there was an error while loading the migration.
     */
    public static Migration loadMigration(String content) throws IOException {
        String migrationName = content.split("name: ")[1].split("\n")[0].trim();
        String[] upQuery = content.split("up: ")[1].split("down:")[0].split("\n");
        String[] downQuery = content.split("down: ")[1].split("\n");

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
     * Loads a migration.
     *
     * @param file The migration file.
     * @return The loaded migration.
     * @throws IOException if there was an error while loading the migration.
     */
    public static Migration loadMigration(File file) throws IOException {
        return loadMigration(Files.readString(file.toPath()));
    }

    /**
     * Save a migration to a file.
     *
     * @param migration The Migration.
     */
    public static void saveMigration(Migration migration) {
        log.info("Saving Migration: {}", migration.getName());
        String path = new File("src/main/resources/sql/migrations/").getAbsolutePath();
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
