package de.presti.ree6.addons;

import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.LoggerImpl;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The actual Addon-Loader which Loads every single Addon from the Addon Folder.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AddonLoader {

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
        for (File f : files) {

            // Check if it's a jar File.
            if (f.getName().endsWith("jar")) {
                try {
                    // Try creating a Local-Addon and adding it into the loaded Addon List.
                    Main.addonManager.loadAddon(loadAddon(f.getName()));
                } catch (Exception ex) {
                    // If the Methode loadAddon fails notify.
                    LoggerImpl.log("AddonManager", "Couldn't load the Addon " + f.getName() + "\nException: " + ex.getCause().getMessage());
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * Actually load a Addon.
     * @param fileName Name of the File.
     * @return a Local-Addon.
     * @throws Exception If it is an invalid Addon.
     */
    public static Addon loadAddon(String fileName) throws Exception {

        // Initialize local Variables to save Information about the Addon.
        String name = null, author = null, addonVer = null, ree6Ver = null, mainPath = null;

        File f = null;

        // Create a ZipInputStream to get every single class inside the JAR. I'm pretty sure there is a faster and more efficient way, but I didn't have the time to find it.
        ZipInputStream jis = new ZipInputStream(new FileInputStream("addons/" + fileName));
        ZipEntry entry;

        // While there a still Classes inside the JAR it should check them.
        while ((entry = jis.getNextEntry()) != null) {
            try {
                // Get the current name of the class.
                String fName = entry.getName();

                // Check if it is a Directory if so don't do anything and skip.
                if (!entry.isDirectory()) {

                    // If it is the addon.yml then get the Data from it.
                    if (fName.equalsIgnoreCase("addon.yml")) {

                        // Create a temporal File to extract the Data from. I'm pretty sure there is a better way but as I said earlier didn't have the time for it.
                        f = new File("addons/tmp/temp_" + ArrayUtil.getRandomShit(9) + ".yml");

                        // Create a FileOutputStream of the temporal File and write every bite from the File inside the JAR.
                        FileOutputStream os = new FileOutputStream(f);

                        for (int c = jis.read(); c != -1; c = jis.read()) {
                            os.write(c);
                        }

                        os.close();

                        // Load it as a YAML-Config and fill the Variables.
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(f);

                        name = conf.getString("name");
                        author = conf.getString("author");
                        addonVer = conf.getString("version");
                        ree6Ver = conf.getString("ree6-version");
                        mainPath = conf.getString("main");

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                jis.closeEntry();
            } finally {
                jis.closeEntry();
            }
        }
        jis.close();

        // Check if the File isn't null and exists if so delete.
        if (f != null && f.exists()) {
            f.delete();
        }

        // Check if there is any data core data if not throw this error.
        if (name == null && mainPath == null) {
            throw new FileNotFoundException("Couldn't find addon.yml");
        } else {
            return new Addon(name, author, addonVer, ree6Ver, mainPath, new File("addons/" + fileName));
        }

    }

}
