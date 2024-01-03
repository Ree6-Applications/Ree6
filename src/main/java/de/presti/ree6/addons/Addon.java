package de.presti.ree6.addons;

import java.io.File;

/**
 * Created a new Local-Addon
 *
 * @param addonInterface AddonInterface.
 * @param name           Add-on Name.
 * @param author         Add-on Author.
 * @param version        Add-on Version.
 * @param apiVersion     Ree6 Version used for the Addon.
 * @param classPath      Path to the Main-Class in the JAR.
 * @param file           The actual JAR as File.
 */
public record Addon(AddonInterface addonInterface, String name, String author, String version,
                    String apiVersion, String classPath, File file) {

    /**
     * Get the AddonInterface.
     *
     * @return AddonInterface.
     */
    public AddonInterface getAddonInterface() {
        return addonInterface;
    }

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
        return classPath;
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
     * Get the ClassLoader of the Addon.
     * @return ClassLoader.
     */
    public ClassLoader getClassLoader() {
        return addonInterface.getClass().getClassLoader();
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
