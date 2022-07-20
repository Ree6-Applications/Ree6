package de.presti.ree6.utils.data;

import de.presti.ree6.main.Main;
import de.presti.ree6.sql.migrations.Migration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

/**
 * MigrationUtil is used to manage migrations.
 */
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
     * @throws IOException if there was an error while running the migrations.
     */
    public static void runAllMigrations() throws IOException {
        File migrationsFolder = new File("migrations/");
        if (!migrationsFolder.exists()) {
            migrationsFolder.mkdir();
        }
        File[] files = migrationsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".migration")) {
                    runMigration(file);
                }
            }
        }
    }

    /**
     * Runs a migration.
     * @param file The migration file.
     * @throws IOException if there was an error while running the migration.
     */
    public static void runMigration(File file) throws IOException {
        Migration migration = loadMigration(file);

        migration.up(Main.getInstance().getSqlConnector());
    }

    /**
     * Loads a migration.
     * @param file The migration file.
     * @return The loaded migration.
     * @throws IOException if there was an error while loading the migration.
     */
    public static Migration loadMigration(File file) throws IOException {
        String migrationFileContent = Files.readString(file.toPath());

        String migrationName = migrationFileContent.split("name: ")[1].split("\n")[0];
        String upQuery = migrationFileContent.split("up: ")[1].split("down:")[0].replace("\n", " ");
        String downQuery = migrationFileContent.split("down: ")[1].replace("\n", " ");

        return new Migration() {
            @Override
            public String getName() {
                return migrationName;
            }

            @Override
            public String getUpQuery() {
                return upQuery;
            }

            @Override
            public String getDownQuery() {
                return downQuery;
            }
        };
    }

    /**
     * Save a migration to a file.
     * @param migration The Migration.
     */
    public static void saveMigration(Migration migration) {
        Main.getInstance().getLogger().info("Saving Migration: " + migration.getName());
        String path = new File("migrations/").getAbsolutePath();
        File folder = new File(path);

        if (!folder.exists()) {
            folder.mkdir();
        }

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Migrations is not a directory");
        }

        File file = new File(path,migration.getName() + ".migration");

        if (file.exists()) {
            throw new IllegalArgumentException("Migration already exists");
        }

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println("name: " + migration.getName());
            printWriter.println("up: " + migration.getUpQuery());
            printWriter.println("down: " + migration.getDownQuery());
        } catch (Exception exception) {
            Main.getInstance().getLogger().info("Could not save migration, reason: " + exception.getMessage(), exception);
        }
    }

}
