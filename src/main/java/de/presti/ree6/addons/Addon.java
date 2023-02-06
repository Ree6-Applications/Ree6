package de.presti.ree6.addons;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * Created a new Local-Addon
 *
 * @param name       Add-on Name.
 * @param author     Add-on Author.
 * @param version    Add-on Version.
 * @param apiVersion Ree6 Version used for the Addon.
 * @param classPath  Path to the Main-Class in the JAR.
 * @param file       The actual JAR as File.
 */
@Getter
public record Addon(AddonInterface addonInterface, String name, String author, String version,
                    String apiVersion, String classPath, File file) {

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
