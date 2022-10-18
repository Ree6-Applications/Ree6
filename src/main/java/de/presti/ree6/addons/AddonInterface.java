package de.presti.ree6.addons;

/**
 * The Interface used for the Addons.
 */
public interface AddonInterface {

    /**
     * Called methode when the Addon gets started (What a surprise isn't it :))
     */
    void onEnable();

    /**
     * Called methode when the Addon get stopped.
     */
    void onDisable();
}
