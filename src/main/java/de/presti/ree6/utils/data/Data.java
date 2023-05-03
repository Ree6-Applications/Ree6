package de.presti.ree6.utils.data;

import de.presti.ree6.main.Main;

/**
 * Utility class to save long term used Data.
 */
public class Data {

    /**
     * Constructor for the Data Utility class.
     */
    private Data() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the configured Discord Bot status.
     * @return the Discord Bot status from the config.
     */
    public static String getStatus() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.status", "ree6.de | %guilds% Servers. (%shard%)");
    }

    /**
     * Get the configured Discord Bot Website.
     * @return the Discord Bot Website from the config.
     */
    public static String getWebsite() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.website", "https://ree6.de");
    }

    /**
     * Get the configured Github repository.
     * @return the Github repository from the config.
     */
    public static String getGithub() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.github", "https://github.ree6.de");
    }

    /**
     * Get the configured Discord Bot invite.
     * @return the Discord Bot invite from the config.
     */
    public static String getInvite() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.invite", "https://invite.ree6.de");
    }

    /**
     * Get the configured Discord Bot support server.
     * @return the Discord Bot support server from the config.
     */
    public static String getSupport() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.support", "https://support.ree6.de");
    }

    /**
     * Get the configured Discord Bot Feedback channel id.
     * @return the Discord Bot Feedback channel id from the config.
     */
    public static long getFeedbackChannel() {
        return Main.getInstance().getConfig().getConfiguration().getLong("bot.misc.feedbackChannelId", 0);
    }

    /**
     * Get the configured Advertisement.
     * @return the Advertisement from the config.
     */
    public static String getAdvertisement() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.advertisement", "powered by Tube-Hosting");
    }

    /**
     * Get the configured Discord Bot owner.
     * @return the Discord Bot owner from the config.
     */
    public static String getBotOwner() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.ownerId", "321580743488831490");
    }

    /**
     * Get the configured Discord Bot name.
     * @return the Discord Bot name from the config.
     */
    public static String getBotName() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.name", "Ree6");
    }
}

