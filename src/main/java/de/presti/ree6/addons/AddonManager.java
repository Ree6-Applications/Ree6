package de.presti.ree6.addons;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.utils.others.VersionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * The AddonManager has been created to save every Addon in a List to keep track of them
 * and to unload them if wanted/needed.
 */
@Slf4j
public class AddonManager {
    /**
     * The actual List with the Addons.
     */
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
        log.info("[AddonManager] Loading {} ({}) by {}", addon.getName(), addon.getVersion(), addon.getAuthor());

        // Check if it's made for the current Ree6 Version if not inform.
        if (!addon.getApiVersion().equalsIgnoreCase(BotWorker.getBuild())) {

            VersionUtil.VersionType difference = VersionUtil.compareVersion(BotWorker.getBuild(), addon.getApiVersion());

            boolean isAddonForOlder = true;

            if (difference == VersionUtil.VersionType.NONE) {
                VersionUtil.VersionType reverseDifference = VersionUtil.compareVersion(addon.getApiVersion(), BotWorker.getBuild());
                if (reverseDifference != VersionUtil.VersionType.NONE) {
                    isAddonForOlder = false;
                    difference = reverseDifference;
                }
            }

            if (isAddonForOlder) {
                log.warn("[AddonManager] The Addon {}({}) by {} is made for an older version of Ree6. Ree6 is on Version {} and the Addon is made for Version {}.", addon.getName(), addon.getVersion(), addon.getAuthor(), BotWorker.getBuild(), addon.getApiVersion());
            } else {
                log.warn("[AddonManager] The Addon {}({}) by {} is made for a newer version of Ree6. Ree6 is on Version {} and the Addon is made for Version {}.", addon.getName(), addon.getVersion(), addon.getAuthor().toLowerCase(), BotWorker.getBuild(), addon.getApiVersion());
            }

            if (difference == VersionUtil.VersionType.MAYOR) {
                log.warn("[AddonManager] Version difference is at least one mayor version, be advised this will most likely cause Issues or not work at all!");
            } else if (difference == VersionUtil.VersionType.MINOR || difference == VersionUtil.VersionType.PATCH) {
                log.warn("[AddonManager] Version difference is at least one " + difference.name().toLowerCase() + " version, be advised this might cause Issues!");
            }
        }

        try {
            addon.getAddonInterface().onEnable();
        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            log.error("[AddonManager] Couldn't start the Addon {}({}) by {}", addon.getName(), addon.getVersion(), addon.getAuthor());
            log.error("[AddonManager] Information: {}, {}, {}", addon.getClassPath(), addon.getVersion(), addon.getApiVersion());
            log.error("[AddonManager] Exception: ", ex);
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
        log.info("[AddonManager] Unloading {} ({}) by {}", addon.getName(), addon.getVersion(), addon.getAuthor());

        try {
            addon.getAddonInterface().onDisable();
        } catch (Exception ex) {
            // Throw an error if the Addon is invalid or corrupted.
            log.error("[AddonManager] Couldn't stop the Addon {}({}) by {}", addon.getName(), addon.getVersion(), addon.getAuthor());
            log.error("[AddonManager] Information: {}, {}, {}", addon.getClassPath(), addon.getVersion(), addon.getApiVersion());
            log.error("[AddonManager] Exception: ", ex);
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
}
