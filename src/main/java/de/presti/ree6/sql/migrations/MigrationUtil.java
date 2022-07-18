package de.presti.ree6.sql.migrations;

import de.presti.ree6.main.Main;

import java.io.File;
import java.io.PrintWriter;

/**
 * MigrationUtil is used to manage migrations.
 */
public class MigrationUtil {

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

        System.out.println(file.getAbsolutePath());

        if (file.exists()) {
            throw new IllegalArgumentException("Migration already exists");
        }

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println("name: " + migration.getName());
            printWriter.println("up: " + migration.getUpQuery());
            printWriter.println("down: " + migration.getDownQuery());
        } catch (Exception exception) {
            Main.getInstance().getLogger().info("Could not save migration, reason: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

}
