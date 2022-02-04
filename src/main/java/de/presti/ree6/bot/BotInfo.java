package de.presti.ree6.bot;

import net.dv8tion.jda.api.JDA;

/**
 * Class to store information about the bot.
 */
public class BotInfo {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    public BotInfo() {
        throw new IllegalStateException("Utility class");
    }

    // Current state of the Bot.
    public static BotState state;

    // Current Bot Version-Typ.
    public static BotVersion version;

    // Instance of the JDA Bot Session.
    public static JDA botInstance;

    // The used Bot-Token.
    public static String token;

    // The current build / version.
    public static String build;

    // Start time of the Bot.
    public static long startTime;

}
