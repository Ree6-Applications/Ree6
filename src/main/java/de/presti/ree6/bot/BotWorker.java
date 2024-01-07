package de.presti.ree6.bot;

import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.main.Main;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to store information about the bot.
 */
@Slf4j
public class BotWorker {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private BotWorker() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Current Bot state.
     */
    @Getter
    @Setter
    private static BotState state;

    /**
     * Current Bot version.
     */
    private static BotVersion version;

    /**
     * Current {@link ShardManager}.
     */
    @Getter
    private static ShardManager shardManager;

    /**
     * Current Bot-Token.
     */
    private static String token;

    /**
     * Current Bot build.
     */
    private static String build;

    /**
     * Bot start time.
     */
    @Setter
    @Getter
    private static long startTime;

    /**
     * Git commit id.
     */
    private static String gitCommitFull;

    /**
     * Git commit abbreviated id.
     */
    private static String gitCommit;

    /**
     * Git build version.
     */
    private static String gitVersion;

    /**
     * Load the git information from the git.properties file.
     */
    private static void loadGitProperties() {
        Properties prop = new Properties();
        try {
            //load a properties file from class path, inside static method
            prop.load(Main.class.getClassLoader().getResourceAsStream("git.properties"));

            //get the property value and print it out
            gitCommitFull = prop.getProperty("git.commit.id.full");
            gitCommit = prop.getProperty("git.commit.id.abbrev");
            gitVersion = prop.getProperty("git.build.version");
        }
        catch (IOException ex) {
            log.error("Failed to read git information from file!", ex);
        }
    }

    /**
     * Create a new {@link net.dv8tion.jda.api.sharding.ShardManager} instance and set the rest information for later use.
     *
     * @param version1    the current Bot Version "typ".
     * @param shardAmount the amount of shards to use.
     */
    public static void createBot(BotVersion version1, int shardAmount) {
        log.info("Loading git information...");
        loadGitProperties();

        log.info("Creating Instance build " + build);
        version = version1;
        token = Main.getInstance().getConfig().getConfiguration().getString(getVersion().getTokenPath());
        state = BotState.INIT;

        DefaultShardManagerBuilder defaultShardManagerBuilder = DefaultShardManagerBuilder
                .createDefault(token)
                .setShardsTotal(shardAmount)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MODERATION)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.EMOJI, CacheFlag.ACTIVITY);

        if (BotConfig.shouldUseLavaLink()) {
            defaultShardManagerBuilder.addEventListeners(Main.getInstance().getLavalink());
            defaultShardManagerBuilder.setVoiceDispatchInterceptor(Main.getInstance().getLavalink().getVoiceInterceptor());
        }

        shardManager = defaultShardManagerBuilder.build();

        if (BotConfig.shouldUseLavaLink() && Main.getInstance().getLavalink().getNodes().isEmpty()) {
            Main.getInstance().getLavalink().setUserId(shardManager.getShardById(0).getSelfUser().getId());
        }
    }

    /**
     * Change the current Activity of the Bot.
     *
     * @param message      the Message of the Activity.
     * @param activityType the Activity type.
     */
    public static void setActivity(String message, Activity.ActivityType activityType) {
        // If the Bot Instance is null, if not set.
        if (shardManager != null)
            shardManager.setActivity(Activity.of(activityType,
                    message.replace("%shards%", String.valueOf(shardManager.getShardsTotal()))
                            .replace("%guilds%", String.valueOf(shardManager.getGuilds().size()))));
    }

    /**
     * Change the current Activity of the Bot.
     *
     * @param jda          the JDA instance.
     * @param message      the Message of the Activity.
     * @param activityType the Activity type.
     */
    public static void setActivity(JDA jda, String message, Activity.ActivityType activityType) {
        // If the Bot Instance is null, if not set.
        if (jda != null)
            jda.getPresence().setActivity(Activity.of(activityType,
                    message.replace("%shards%", String.valueOf(shardManager.getShardsTotal()))
                            .replace("%shard%", String.valueOf(jda.getShardInfo().getShardId()))
                            .replace("%guilds%", String.valueOf(shardManager.getGuilds().size()))
                            .replace("%shard_guilds%", String.valueOf(jda.getGuilds().size()))));
    }

    /**
     * Called when the Bot should Shut down.
     */
    public static void shutdown() {
        // Check if the Instance of null if not, shutdown.
        if (shardManager != null) {
            shardManager.shutdown();
        }
    }

    /**
     * Called to add a ListenerAdapter to the EventListener.
     *
     * @param listenerAdapters the Listener Adapter(s) that should be added.
     */
    public static void addEvent(ListenerAdapter... listenerAdapters) {
        for (ListenerAdapter listenerAdapter : listenerAdapters) {
            shardManager.addEventListener(listenerAdapter);
        }
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

    /**
     * Get the current Bot Version.
     *
     * @return the {@link BotVersion}
     */
    public static BotVersion getVersion() {
        if (version == null) return BotVersion.RELEASE;
        return version;
    }

    /**
     * Get the build / the actual version in the x.y.z format.
     *
     * @return the Build.
     */
    public static String getBuild() {
        if (build == null) {
            build = Objects.requireNonNullElse(Main.class.getPackage().getImplementationVersion(),
                    Objects.requireNonNullElse(gitVersion, "3.1.4"));
        }
        return build;
    }

    /**
     * Get the commit of the current build.
     *
     * @return the commit.
     */
    public static String getCommit() {
        return gitCommit;
    }

    /**
     * Get the commit of the current build.
     *
     * @return the commit.
     */
    public static String getCommitFull() {
        return gitCommitFull;
    }
}
