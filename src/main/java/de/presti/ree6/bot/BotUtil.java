package de.presti.ree6.bot;

import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class used to cover small utilities for the Bot.
 */
public class BotUtil {

    /**
     * Constructor for the Bot Utility class.
     */
    private BotUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create a new {@link net.dv8tion.jda.api.JDA} instance and set the rest information for later use.
     *
     * @param version the current Bot Version "typ".
     * @param build   the current Bot Version.
     * @throws LoginException when there is a problem with creating the Session.
     */
    public static void createBot(BotVersion version, String build) throws LoginException {
        BotInfo.version = version;
        BotInfo.token = BotInfo.version == BotVersion.DEV ? Main.getInstance().getConfig().getConfig().getString("bot.tokens.dev") : Main.getInstance().getConfig().getConfig().getString("bot.tokens.rel");
        BotInfo.state = BotState.INIT;
        BotInfo.build = build;

        BotInfo.shardManager = DefaultShardManagerBuilder.createDefault(BotInfo.token).enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).disableIntents(GatewayIntent.GUILD_PRESENCES).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY).build();
    }

    /**
     * Change the current Activity of the Bot.
     *
     * @param message      the Message of the Activity.
     * @param activityType the Activity type.
     */
    public static void setActivity(String message, Activity.ActivityType activityType) {
        // If the Bot Instance is null, if not set.
        if (BotInfo.shardManager != null)
            BotInfo.shardManager.setActivity(Activity.of(activityType, message.replace("%shards%", BotInfo.shardManager.getShardsTotal() + "")));
    }

    /**
     * Called when the Bot should Shut down.
     */
    public static void shutdown() {
        // Check if the Instance of null if not, shutdown.
        if (BotInfo.shardManager != null) {
            BotInfo.shardManager.shutdown();
        }
    }

    /**
     * Called to add a ListenerAdapter to the EventListener.
     *
     * @param listenerAdapter the Listener Adapter that should be added.
     */
    public static void addEvent(ListenerAdapter listenerAdapter) {
        BotInfo.shardManager.addEventListener(listenerAdapter);
    }

    /**
     * Called to get a random Embed supported Color.
     *
     * @return a {@link Color}.
     */
    public static Color randomEmbedColor() {
        String zeros = "000000";
        String s = Integer.toString(ThreadLocalRandom.current().nextInt(0X1000000), 16);
        s = zeros.substring(s.length()) + s;
        return Color.decode("#" + s);
    }
}