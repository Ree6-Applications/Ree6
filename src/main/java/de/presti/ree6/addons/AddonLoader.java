package de.presti.ree6.addons;

import de.presti.ree6.main.Main;
import lombok.extern.slf4j.Slf4j;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The actual Addon-Loader which Loads every single Addon from the Addon Folder.
 */
@Slf4j
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AddonLoader {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private AddonLoader() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create the Folder if not existing.
     */
    private static void createFolders() {
        if (!new File("addons/").exists()) {
            new File("addons/").mkdir();
        }

        if (!new File("addons/tmp/").exists()) {
            new File("addons/tmp/").mkdir();
        }
    }

    /**
     * Load every Addon.
     */
    public static void loadAllAddons() {

        // Create Folder if not existing.
        createFolders();

        // Get every single File from the Folder.
        File[] files = new File("addons/").listFiles();

        // Check if there are any Files.
        assert files != null;
        for (File file : files) {

            // Check if it's a jar File.
            if (file.getName().endsWith("jar")) {
                try {
                    // Try creating a Local-Addon and adding it into the loaded Addon List.
                    Addon addon = loadAddon(file.getName());

                    if (addon == null) {
                        log.error("Couldn't pre-load the addon {}", file.getName());
                    }

                    Main.getInstance().getAddonManager().loadAddon(addon);
                } catch (Exception ex) {
                    // If the Methode loadAddon fails notify.
                    log.error("[AddonManager] Couldn't load the Addon {}\nException: {}", file.getName(), ex.getMessage());
                }
            }
        }

    }

    /**
     * Actually load a Addon.
     *
     * @param fileName Name of the File.
     * @return a Local-Addon.
     * @throws IOException If it is an invalid Addon.
     */
    public static Addon loadAddon(String fileName) throws IOException {

        // Initialize local Variables to save Information about the Addon.
        String name = null, author = null, version = null, apiVersion = null, classPath = null;


        // Create a ZipInputStream to get every single class inside the JAR. I'm pretty sure there is a faster and more efficient way, but I didn't have the time to find it.
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream("addons/" + fileName))) {
            ZipEntry entry;

            // While there a still Classes inside the JAR it should check them.
            while ((entry = zipInputStream.getNextEntry()) != null) {
                try {
                    // Get the current name of the class.
                    String entryName = entry.getName();

                    // Check if it is a Directory if so don't do anything and skip.
                    // If it is the addon.yml then get the Data from it.
                    if (!entry.isDirectory() && entryName.equalsIgnoreCase("addon.yml")) {
                        String content;
                        // Create a FileOutputStream of the temporal File and write every bite from the File inside the JAR.
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                            for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                                os.write(c);
                            }

                            content = os.toString(StandardCharsets.UTF_8);
                        }

                        if (content == null) {
                            log.info("Error while trying to pre-load the Addon {}\nException: {}", fileName, "Content is null");
                            continue;
                        }

                        // Load it as a YAML-Config and fill the Variables.
                        FileConfiguration conf = YamlConfiguration.loadConfigurationFromString(content);

                        name = conf.getString("name");
                        author = conf.getString("author");
                        version = conf.getString("version");
                        apiVersion = conf.getString("api-version");
                        classPath = conf.getString("main");
                        break;
                    }
                } catch (Exception e) {
                    log.error("Error while trying to pre-load the Addon {}\nException: {}", fileName, e.getMessage());
                    zipInputStream.closeEntry();
                } finally {
                    zipInputStream.closeEntry();
                }
            }
        }

        // Check if there is any data core data if not throw this error.
        if (name == null && classPath == null) {
            log.error("Error while trying to pre-load the Addon {}, no addon.yml given.", fileName);
        } else {
            File addonFile = new File("addons/" + fileName);
            AddonInterface addonInterface = null;
            // Try loading the Class with a URL Class Loader.
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { addonFile.toURI().toURL() }, AddonLoader.class.getClassLoader())) {

                // Get the Addon Class.
                Class<?> addonClass = getClass(urlClassLoader, classPath);

                // If valid, call the onEnable methode.
                if (addonClass != null) {
                    log.info("[AddonManager] Loaded {} ({}) by {}", name, version, author);
                    addonInterface = (AddonInterface) addonClass.getDeclaredConstructor().newInstance();
                } else {
                    // If not inform about an invalid Addon.
                    log.error("[AddonManager] Couldn't load the Addon {}({}) by {}", name, version, author);
                    log.error("[AddonManager] The given Main class doesn't not implement our AddonInterface!");
                }
            } catch (Exception e) {
                log.error("[AddonManager] Couldn't start the Addon {}({}) by {}", name, version, author);
                log.error("[AddonManager] Exception: {}", e.getMessage());
            }

            if (addonInterface == null) {
                log.error("Error while trying to pre-load the Addon {}\nException: {}", fileName, "AddonInterface is null");
                return null;
            }

            return new Addon(addonInterface, name, author, version, apiVersion, classPath, new File("addons/" + fileName));
        }

        return null;
    }

    /**
     * Get the Addon Class from the Class-Loader and its path.
     *
     * @param classLoader the Class Loader of the File.
     * @param classPath   the Path to the Main class.
     * @return the Main class of the Addon.
     * @throws ClassNotFoundException if there is no file with the given Path.
     */
    private static Class<?> getClass(URLClassLoader classLoader, String classPath) throws ClassNotFoundException {
        // Class from the loader.
        Class<?> urlCl = classLoader.loadClass(classPath);

        // Get the Interfaces.
        Class<?>[] ifs = urlCl.getInterfaces();

        // Check if any of the Interfaces is the AddonInterface.
        for (Class<?> anIf : ifs) {

            // If it has the AddonInterface mark it as valid.
            if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                return urlCl;
            }
        }

        return null;
    }
}
