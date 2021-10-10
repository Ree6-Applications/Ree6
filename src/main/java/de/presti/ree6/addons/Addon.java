package de.presti.ree6.addons;

import java.io.File;

/**
 * Local Class to identify and cache Addons.
 * This needs a revamp too.
 */

// TODO update
public class Addon {

    // Name of the Addon.
    final String name;

    // Author of the Addon.
    final String author;

    // Version of the Addon.
    final String addonVer;

    // Version of the Ree6 used for this Addon.
    final String ree6Ver;

    // JavaPath to the Addon-Main class.
    final String mainPath;

    // The actual Addon Jar File.
    final File file;

    /**
     * Created a new Local-Addon
     * @param name Addon Name.
     * @param author Addon Author.
     * @param addonVer Addon Version.
     * @param ree6Ver Ree6 Version used for the Addon.
     * @param mainPath Path to the Main-Class in the JAR.
     * @param file The actual JAR as File.
     */
    public Addon(String name, String author, String addonVer, String ree6Ver, String mainPath, File file) {
        this.name = name;
        this.author = author;
        this.addonVer = addonVer;
        this.ree6Ver = ree6Ver;
        this.mainPath = mainPath;
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
    public String getAddonVer() {
        return addonVer;
    }

    /**
     * Get the Ree6 Version used for the Addon.
     * @return used Ree6 Version.
     */
    public String getRee6Ver() {
        return ree6Ver;
    }

    /**
     * Get the Path to the Main class.
     * @return Main class Path.
     */
    public String getMainPath() {
        return mainPath;
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
                ", addonVer='" + getAddonVer() + '\'' +
                ", ree6Ver='" + getRee6Ver() + '\'' +
                ", mainPath='" + getMainPath() + '\'' +
                ", file=" + getFile().getAbsolutePath() +
                '}';
    }
}
