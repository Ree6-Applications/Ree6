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


    public static String getWebsite() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.website", "https://ree6.de");
    }

    public static String getGithub() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.github", "https://github.ree6.de");
    }

    public static String getInvite() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.invite", "https://invite.ree6.de");
    }

    public static String getSupport() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.support", "https://support.ree6.de");
    }

    public static long getFeedbackChannel() {
        return Main.getInstance().getConfig().getConfiguration().getLong("bot.misc.feedbackChannelId", 0);
    }

    public static String getAdvertisement() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.advertisement", "powered by Tube-Hosting");
    }

    public static String getBotOwner() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.ownerId", "321580743488831490");
    }

    public static String getBotName() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.name", "Ree6");
    }
}

