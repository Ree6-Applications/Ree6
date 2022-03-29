package de.presti.ree6.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Class to store information about the bot.
 */
public class BotInfo {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private BotInfo() {
        throw new IllegalStateException("Utility class");
    }

    // Current state of the Bot.
    public static BotState state;

    // Current Bot Version-Typ.
    public static BotVersion version;

    // Instance of the JDA ShardManager.
    public static ShardManager shardManager;

    /**
     * Instance of the main JDA connection.
     * @deprecated because of the usage of the current ShardManager
     */
    @Deprecated(forRemoval = true, since = "1.7.0")
    public static JDA botInstance;

    // The used Bot-Token.
    public static String token;

    // The current build / version.
    public static String build;

    // Start time of the Bot.
    public static long startTime;

}
