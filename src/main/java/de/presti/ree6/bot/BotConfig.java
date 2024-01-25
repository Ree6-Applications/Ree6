package de.presti.ree6.bot;

import de.presti.ree6.main.Main;

import java.awt.*;

/**
 * Utility class to save long term used Data.
 */
public class BotConfig {

    /**
     * Constructor for the Data Utility class.
     */
    private BotConfig() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Color values to be preset to prevent parsing hex codes from the config over and over and over and over and over again.
     */
    private static Color rankTextColor, rankDetailColor, rankHighlightColor,  rankProgressbarColor, rankProgressbarBackgroundColor;

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

    /**
     * Check if a module is activated in the config.
     * @param moduleName Name of the module.
     * @return if the module is activated.
     */
    public static boolean isModuleActive(String moduleName) {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.modules." + moduleName, true);
    }

    /**
     * Check if the module notification should be hidden.
     * @return if the module notification should be hidden.
     */
    public static boolean shouldHideModuleNotification() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.hideModuleNotification", false);
    }

    /**
     * Check if the bot should be able to send the recordings in chat.
     * @return if the bot is allowed to send the recordings in chat.
     */
    public static boolean allowRecordingInChat() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.allowRecordingInChat", false);
    }

    /**
     * Get the configured default language.
     * @return the default language from the config.
     */
    public static String getDefaultLanguage() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.defaultLanguage", "en-GB");
    }

    /**
     * Get the configured Twitch Auth Url.
     * @return the Twitch Auth Url from the config.
     */
    public static String getTwitchAuth() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.twitchAuth", "https://cp.ree6.de/external/twitch");
    }

    /**
     * Get the configured Recording Url.
     * @return the Recording Url from the config.
     */
    public static String getRecordingUrl() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.recording", "https://cp.ree6.de/external/recording");
    }

    /**
     * Get the configured Webinterface Url.
     * @return the Webinterface Url from the config.
     */
    public static String getWebinterface() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.webinterface", "https://cp.ree6.de");
    }

    /**
     * Get the configured default prefix.
     * @return the default prefix from the config.
     */
    public static String getDefaultPrefix() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.defaultPrefix", "ree!");
    }

    /**
     * Get the configured Font for the text.
     * @return the Font for the text from the config.
     */
    public static String getTextFont() {
        return Main.getInstance().getConfig().getConfiguration().getString("bot.misc.textFont", "Verdana");
    }

    /**
     * Check if the bot should run in debug mode.
     * @return if the bot should run in debug mode.
     */
    public static boolean isDebug() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.debug", false);
    }

    /**
     * Check if the bot should use LavaLink.
     * @return if the bot should use LavaLink.
     */
    public static boolean shouldUseLavaLink() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("lavalink.enable", false);
    }

    /**
     * Check if the leveling progress should be reset on mute.
     * @return if the leveling progress should be reset on mute.
     */
    public static boolean shouldResetOnMute() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.leveling.resets.mute", true);
    }

    /**
     * Check if the leveling progress should be reset on global mute.
     * @return if the leveling progress should be reset on global mute.
     */
    public static boolean shouldResetOnMuteGlobal() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.leveling.resets.muteGlobal", true);
    }

    /**
     * Check if the leveling progress should be reset on deafen.
     * @return if the leveling progress should be reset on deafen.
     */
    public static boolean shouldResetOnDeafen() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.leveling.resets.deafen", true);
    }

    /**
     * Check if the leveling progress should be reset on global deafen.
     * @return if the leveling progress should be reset on global deafen.
     */
    public static boolean shouldResetOnDeafenGlobal() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("bot.misc.leveling.resets.deafenGlobal", true);
    }

    /**
     * Check if Ree6 should use Sentry to report exceptions.
     * @return if Ree6 should use Sentry to report exceptions.
     */
    public static boolean shouldUseSentry() {
        return Main.getInstance().getConfig().getConfiguration().getBoolean("sentry.enable", true);
    }

    //region Rank Colors

    /**
     * Get the configured color for the rank card.
     * @return the color for the rank card from the config.
     */
    public static Color getRankTextColor() {
        if (rankTextColor == null) {
            rankTextColor = Color.decode(Main.getInstance().getConfig().getConfiguration().getString("bot.misc.rankCard.textColor", "#FFFFFF"));
        }
        return rankTextColor;
    }

    /**
     * Get the configured color for the highlight on the rank card.
     * @return the color for the highlight on the rank card from the config.
     */
    public static Color getRankHighlightColor() {
        if (rankHighlightColor == null) {
            rankHighlightColor = Color.decode(Main.getInstance().getConfig().getConfiguration().getString("bot.misc.rankCard.highlightColor", "#FF00FF"));
        }
        return rankHighlightColor;
    }

    /**
     * Get the configured color for the details on the rank card.
     * @return the color for the details on the rank card from the config.
     */
    public static Color getRankDetailColor() {
        if (rankDetailColor == null) {
            rankDetailColor = Color.decode(Main.getInstance().getConfig().getConfiguration().getString("bot.misc.rankCard.detailColor", "#C0C0C0"));
        }
        return rankDetailColor;
    }

    /**
     * Get the configured color for the progressbar on the rank card.
     * @return the color for the progressbar on the rank card from the config.
     */
    public static Color getRankProgressbarColor() {
        if (rankProgressbarColor == null) {
            rankProgressbarColor = Color.decode(Main.getInstance().getConfig().getConfiguration().getString("bot.misc.rankCard.progressbarColor", "#FF00FF"));
        }
        return rankProgressbarColor;
    }

    /**
     * Get the configured color for the background of the progressbar on the rank card.
     * @return the color for the background of the progressbar on the rank card from the config.
     */
    public static Color getRankProgressbarBackgroundColor() {
        if (rankProgressbarBackgroundColor == null) {
            rankProgressbarBackgroundColor = Color.decode(Main.getInstance().getConfig().getConfiguration().getString("bot.misc.rankCard.progressbarBackgroundColor", "#7C007C"));
        }
        return rankProgressbarBackgroundColor;
    }

    //endregion
}

