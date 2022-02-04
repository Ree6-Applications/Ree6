package de.presti.ree6.addons;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.main.Main;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * The AddonManager has been created to save every Addon in a List to keep track of them
 * and to unload them if wanted/needed.
 */
public class AddonManager {

    // The actual List with the Addons.
    public final ArrayList<Addon> addons = new ArrayList<>();

    /**
     * Reload the AddonManager by stopping every Addon and reading them.
     */
    public void reload() {
        stopAddons();

        addons.clear();

        AddonLoader.loadAllAddons();

        startAddons();
    }

    /**
     * Try starting an Addon by calling the Class with a URL-Class Loader.
     *
     * @param addon the Local-Addon.
     */
    public void startAddon(Addon addon) {
        Main.getInstance().getLogger().info("[AddonManager] Loading " + addon.getName() + " (" + addon.getVersion() + ") by " + addon.getAuthor());

        // Check if it's made for the current Ree6 Version if not inform.
        if (!addon.getApiVersion().equalsIgnoreCase(BotInfo.build)) {

            Main.getInstance().getLogger().warn("[AddonManager] The Addon " + addon.getName() + " by " + addon.getAuthor() + " has been developed for the Version" + addon.getApiVersion() +
                            ", which is not the same version you are using. This could mean that the addon doesn't function right!");
        }

        try {

            // Try loading the Class with a URL Class Loader.
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()})) {

                // Get the Addon Class.
                Class<?> addonClass = getClass(urlClassLoader, addon.getClassPath());

                // If valid call the onEnable methode.
                if (addonClass != null) {
                    Main.getInstance().getLogger().info("[AddonManager] Loaded " + addon.getName() + " (" + addon.getVersion() + ") by " + addon.getAuthor());
                    AddonInterface inf = (AddonInterface) addonClass.getDeclaredConstructor().newInstance();
                    inf.onEnable();
                } else {
                    // If not inform about an invalid Addon.
                    Main.getInstance().getLogger().error("[AddonManager] Couldn't start the Addon " + addon.getName() + "(" + addon.getVersion() + ") by " + addon.getAuthor());
                    Main.getInstance().getLogger().error("[AddonManager] The given Main class doesn't not implement our AddonInterface!");
                }
            }

        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            Main.getInstance().getLogger().error("[AddonManager] Couldn't start the Addon " + addon.getName() + "(" + addon.getVersion() + ") by " + addon.getAuthor());
            Main.getInstance().getLogger().error("[AddonManager] Information: " + addon.getClassPath() + ", " + addon.getVersion() + ", " + addon.getApiVersion());
            Main.getInstance().getLogger().error("[AddonManager] Exception: " + ex.getCause().getMessage());
        }
    }

    /**
     * Start every Addon from the AddonList.
     */
    public void startAddons() {
        for (Addon addon : addons) {
            startAddon(addon);
        }
    }

    /**
     * Try stopping the Addon by calling the Main-Class methode onDisable.
     *
     * @param addon The Local-Addon.
     */
    public void stopAddon(Addon addon) {
        Main.getInstance().getLogger().info("[AddonManager] Unloading " + addon.getName() + " (" + addon.getVersion() + ") by " + addon.getAuthor());

        try {
            // Try loading the Class with a URL Class Loader.
            try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()})) {

                // Get the Addon Class.
                Class<?> addonClass = getClass(urlClassLoader, addon.getClassPath());

                // If valid call the onDisable methode.
                if (addonClass != null) {
                    Main.getInstance().getLogger().info("[AddonManager] Unloaded " + addon.getName() + " (" + addon.getVersion() + ") by " + addon.getAuthor());
                    AddonInterface inf = (AddonInterface) addonClass.getDeclaredConstructor().newInstance();
                    inf.onDisable();
                } else {
                    // If not inform about an invalid Addon.
                    Main.getInstance().getLogger().error("[AddonManager] Couldn't stop the Addon " + addon.getName() + "(" + addon.getVersion() + ") by " + addon.getAuthor());
                    Main.getInstance().getLogger().error("[AddonManager] The given Main class doesn't not implement our AddonInterface!");
                }
            }
        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            Main.getInstance().getLogger().error("[AddonManager] Couldn't stop the Addon " + addon.getName() + "(" + addon.getVersion() + ") by " + addon.getAuthor());
            Main.getInstance().getLogger().error("[AddonManager] Information: " + addon.getClassPath() + ", " + addon.getVersion() + ", " + addon.getApiVersion());
            Main.getInstance().getLogger().error("[AddonManager] Exception: " + ex.getCause().getMessage());
        }
    }

    /**
     * Stop every Addon.
     */
    public void stopAddons() {
        for (Addon addon : addons) {
            stopAddon(addon);
        }
    }

    /**
     * "Load" an Addon.
     *
     * @param addon the Local-Addon.
     */
    public void loadAddon(Addon addon) {
        addons.add(addon);
    }

    /**
     * Get the Addon Class from the Class-Loader and its path.
     *
     * @param classLoader the Class Loader of the File.
     * @param classPath   the Path to the Main class.
     * @return the Main {@link Class<>} of the Addon.
     * @throws ClassNotFoundException if there is no file with the given Path.
     */
    public Class<?> getClass(URLClassLoader classLoader, String classPath) throws ClassNotFoundException {
        // Class from the loader.
        Class<?> urlCl = classLoader.loadClass(classPath);

        // Get the Interfaces.
        Class<?>[] ifs = urlCl.getInterfaces();

        // Check if any of the Interfaces is the AddonInterface.
        for (Class<?> anIf : ifs) {

            // If it has the AddonInterface mark it as valid.
            if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                return anIf;
            }
        }

        return null;
    }

}
