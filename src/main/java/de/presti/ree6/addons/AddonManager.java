package de.presti.ree6.addons;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.utils.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class AddonManager {

    public final ArrayList<Addon> addons = new ArrayList<>();

    public AddonManager() {
    }

    public void reload() {
        stopAddons();

        addons.clear();

        AddonLoader.loadAllAddons();

        startAddons();
    }

    public void startAddon(Addon addon) {
        if (!addon.getRee6Ver().equalsIgnoreCase(BotInfo.build)) {
            Logger.log("AddonManager", "The Addon " + addon.getName() + " by " + addon.getAuthor() + " has been developed for Ree6 b" + addon.getRee6Ver() + " but you have a newer Version so be careful!");
        }

        try {

            Class<?> urlCl = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()}).loadClass(addon.getMainPath());

            boolean valid = false;


            Class<?>[] ifs = urlCl.getInterfaces();

            for (Class<?> anIf : ifs) {
                if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                AddonInterface inf = (AddonInterface) urlCl.newInstance();
                inf.onEnable();
            } else {
                Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
                Logger.log("AddonManager", "It doesn't implement the AddonInterface!");
            }


        } catch (Exception ex) {
            Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
            Logger.log("AddonManager", "Information: " + addon.getMainPath() + ", " + addon.getAddonVer() + ", " + addon.getRee6Ver());
            Logger.log("AddonManager", "Exception: " + ex.getCause().getMessage());
        }
    }

    public void startAddons() {
        for (Addon addon : addons) {
            startAddon(addon);
        }
    }

    public void stopAddon(Addon addon) {
        try {
            Class<?> urlCl = new URLClassLoader(new URL[]{addon.getFile().toURI().toURL()}).loadClass(addon.getMainPath());

            boolean valid = false;


            Class<?>[] ifs = urlCl.getInterfaces();

            for (Class<?> anIf : ifs) {
                if (anIf.getName().equalsIgnoreCase("de.presti.ree6.addons.AddonInterface")) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                AddonInterface inf = (AddonInterface) urlCl.newInstance();
                inf.onDisable();
            } else {
                Logger.log("AddonManager", "Couldn't start the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor());
                Logger.log("AddonManager", "It doesn't implement the AddonInterface!");
            }
        } catch (Exception ex) {
            Logger.log("AddonManager", "Couldn't stop the Addon " + addon.getName() + "(" + addon.getAddonVer() + ") by " + addon.getAuthor() + "\nException: " + ex.getCause().getMessage());
        }
    }

    public void stopAddons() {
        for (Addon addon : addons) {
            stopAddon(addon);
        }
    }

    public void loadAddon(Addon addon) {
        addons.add(addon);
    }

}
