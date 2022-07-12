package de.presti.ree6.addons;

import java.io.File;

/**
 * Created a new Local-Addon
 *
 * @param name       Add-on Name.
 * @param author     Add-on Author.
 * @param version    Add-on Version.
 * @param apiVersion Ree6 Version used for the Addon.
 * @param path       Path to the Main-Class in the JAR.
 * @param file       The actual JAR as File.
 */
public record Addon(String name, String author, String version,
                    String apiVersion, String path, File file) {
    
    /**
     * Get the Add-on Name.
     *
     * @return Add-on Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Author Name.
     *
     * @return Author Name.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Get the Add-on Version.
     *
     * @return Add-on Version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the Ree6 Version used for the Addon.
     *
     * @return used Ree6 Version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Get the Path to the Main class.
     *
     * @return Main class Path.
     */
    public String getClassPath() {
        return path;
    }

    /**
     * Get the actual File.
     *
     * @return File.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get everything in a single String.
     *
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
