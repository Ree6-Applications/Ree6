package de.presti.ree6.addons;

import java.io.File;

/**
 * Local Class to identify and cache Addons.
 */

public class Addon {

    // Name of the Addon, Author of the Addon, Version of the Addon, used API-Version and path to the Main class.
    final String name, author, version, apiVersion, path;

    // The actual Addon Jar File.
    final File file;

    /**
     * Created a new Local-Addon
     * @param name Addon Name.
     * @param author Addon Author.
     * @param version Addon Version.
     * @param apiVersion Ree6 Version used for the Addon.
     * @param path Path to the Main-Class in the JAR.
     * @param file The actual JAR as File.
     */
    public Addon(String name, String author, String version, String apiVersion, String path, File file) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.apiVersion = apiVersion;
        this.path = path;
        this.file = file;
    }

    /**
     * Get the Addon Name.
     * @return Addon Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Author Name.
     * @return Author Name.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get the Addon Version.
     * @return Addon Version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the Ree6 Version used for the Addon.
     * @return used Ree6 Version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Get the Path to the Main class.
     * @return Main class Path.
     */
    public String getClassPath() {
        return path;
    }

    /**
     * Get the actual File.
     * @return File.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get everything in a single String.
     * @return a String with every data.
     */
    @Override
    public String toString() {
        return "Addon{" +
                "name='" + getName() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", version='" + getVersion() + '\'' +
                ", api='" + getApiVersion() + '\'' +
                ", path='" + getClassPath() + '\'' +
                ", file=" + getFile().getAbsolutePath() +
                '}';
    }
}
