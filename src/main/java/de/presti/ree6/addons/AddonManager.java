package de.presti.ree6.addons;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.utils.Logger;

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

    public AddonManager() {
    }

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
     * @param addon the Local-Addon.
     */
    public void startAddon(Addon addon) {

        // Check if it's made for the newest Ree6 Version if not inform.
        if (!addon.getRee6Ver().equalsIgnoreCase(BotInfo.build)) {
            Logger.log("AddonManager", "The Addon " + addon.getName() + " by " + addon.getAuthor() + " has been developed for Ree6 b" + addon.getRee6Ver() + " but you have a newer Version so be careful!");
        }

        try {

            // Try loading the Class with a URL Class Loader.
            Class<?> urlCl = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()}).loadClass(addon.getMainPath());

            boolean valid = false;

            // Get the Interfaces.
            Class<?>[] ifs = urlCl.getInterfaces();

            // Check if any of the Interfaces is the AddonInterface.
            for (Class<?> anIf : ifs) {

                // If it has the AddonInterface mark it as valid.
                if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                    valid = true;
                    break;
                }
            }

            // If valid call the onEnable methode.
            if (valid) {
                AddonInterface inf = (AddonInterface) urlCl.newInstance();
                inf.onEnable();
            } else {
                // If not inform about an invalid Addon.
                Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
                Logger.log("AddonManager", "It doesn't implement the AddonInterface!");
            }


        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
            Logger.log("AddonManager", "Information: " + addon.getMainPath() + ", " + addon.getAddonVer() + ", " + addon.getRee6Ver());
            Logger.log("AddonManager", "Exception: " + ex.getCause().getMessage());
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
     * @param addon The Local-Addon.
     */
    public void stopAddon(Addon addon) {
        try {
            // Try loading the Class with a URL Class Loader.
            Class<?> urlCl = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()}).loadClass(addon.getMainPath());

            boolean valid = false;

            // Get the Interfaces.
            Class<?>[] ifs = urlCl.getInterfaces();

            // Check if any of the Interfaces is the AddonInterface.
            for (Class<?> anIf : ifs) {

                // If it has the AddonInterface mark it as valid.
                if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                    valid = true;
                    break;
                }
            }

            // If valid call the onDisable methode.
            if (valid) {
                AddonInterface inf = (AddonInterface) urlCl.newInstance();
                inf.onDisable();
            } else {
                // If not inform about an invalid Addon.
                Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
                Logger.log("AddonManager", "It doesn't implement the AddonInterface!");
            }
        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            Logger.log("AddonManager", "Couldn't stop the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor() + "\nException: " + ex.getCause().getMessage());
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
     * @param addon the Local-Addon.
     */
    public void loadAddon(Addon addon) {
        addons.add(addon);
    }

}
