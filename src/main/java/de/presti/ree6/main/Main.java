package de.presti.ree6.main;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IEventSubscription;
import com.github.twitch4j.common.events.TwitchEvent;
import com.github.twitch4j.pubsub.PubSubSubscription;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import com.google.gson.JsonObject;
import de.presti.ree6.addons.AddonLoader;
import de.presti.ree6.addons.AddonManager;
import de.presti.ree6.audio.music.MusicWorker;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.events.GameEvents;
import de.presti.ree6.events.LoggingEvents;
import de.presti.ree6.events.MenuEvents;
import de.presti.ree6.events.OtherEvents;
import de.presti.ree6.game.core.GameManager;
import de.presti.ree6.game.impl.musicquiz.util.MusicQuizUtil;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.logger.events.LoggerQueue;
import de.presti.ree6.sql.DatabaseTyp;
import de.presti.ree6.sql.SQLSession;
import de.presti.ree6.sql.entities.Setting;
import de.presti.ree6.sql.entities.TwitchIntegration;
import de.presti.ree6.sql.entities.stats.ChannelStats;
import de.presti.ree6.sql.entities.stats.Statistics;
import de.presti.ree6.sql.util.SettingsManager;
import de.presti.ree6.streamtools.StreamActionContainerCreator;
import de.presti.ree6.utils.apis.Notifier;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Config;
import de.presti.ree6.utils.data.CustomOAuth2Credential;
import de.presti.ree6.utils.data.CustomOAuth2Util;
import de.presti.ree6.utils.others.ThreadUtil;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Main Application class, used to store Instances of System Relevant classes.
 */
@Slf4j
public class Main {
    /**
     * An Instance of the class itself.
     */
    static Main instance;

    /**
     * Instance of the Notifier Manager, used to manage the Notifier Tools.
     */
    Notifier notifier;

    /**
     * Instance of the Command.
     */
    CommandManager commandManager;

    /**
     * Addon Manager, used to manage the Addons.
     */
    AddonManager addonManager;

    /**
     * Instance of the LoggerQueue, used to merge Logs to prevent Rate-Limits.
     */
    LoggerQueue loggerQueue;

    /**
     * Instance of the MusicWorker used to manage the Music-Player.
     */
    MusicWorker musicWorker;

    /**
     * Instance of the Config System.
     */
    Config config;

    /**
     * String used to identify the last day.
     */
    String lastDay = "";

    /**
     * Main methode called when Application starts.
     *
     * @param args Start Arguments.
     */
    public static void main(String[] args) {

        // To allow Image creation on CPU.
        System.setProperty("java.awt.headless", "true");

        // Create the Main instance.
        instance = new Main();

        // Create the LoggerQueue Instance.
        instance.loggerQueue = new LoggerQueue();

        // Create the Config System Instance.
        instance.config = new Config();

        // Initialize the Config.
        instance.config.init();

        log.info("Creating Sentry Instance.");

        // Create a Sentry Instance to send Exception to an external Service for bug fixing.
        Sentry.init(options -> {
            String dsn = instance.config.getConfiguration().getString("sentry.dsn");
            options.setDsn((dsn == null || dsn.equalsIgnoreCase("yourSentryDSNHere")) ? "" : dsn);
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setRelease(BotWorker.getBuild());
        });

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Sentry.captureException(e));

        log.info("Starting preparations of the Bot...");

        LanguageService.downloadLanguages();

        log.info("Finished preparations of the Bot!");

        log.info("Starting Ree6!");

        DatabaseTyp databaseTyp;

        switch (getInstance().getConfig().getConfiguration().getString("hikari.misc.storage").toLowerCase()) {
            case "mariadb" -> databaseTyp = DatabaseTyp.MariaDB;

            default -> databaseTyp = DatabaseTyp.SQLite;
        }

        new SQLSession(instance.config.getConfiguration().getString("hikari.sql.user"),
                instance.config.getConfiguration().getString("hikari.sql.db"), instance.config.getConfiguration().getString("hikari.sql.pw"),
                instance.config.getConfiguration().getString("hikari.sql.host"), instance.config.getConfiguration().getInt("hikari.sql.port"),
                instance.config.getConfiguration().getString("hikari.misc.storageFile"), databaseTyp,
                instance.config.getConfiguration().getInt("hikari.misc.poolSize"));

        try {
            // Create the Command-Manager instance.
            instance.commandManager = new CommandManager();
            // Create Command Settings.
            for (ICommand command : getInstance().getCommandManager().getCommands()) {

                // Skip the hidden Commands.
                if (command.getClass().getAnnotation(Command.class).category() == Category.HIDDEN) continue;

                SettingsManager.getSettings().add(new Setting("-1",
                        "command_" + command.getClass().getAnnotation(Command.class).name().toLowerCase(), true));
            }
        } catch (Exception exception) {
            log.error("Shutting down, because of an critical error!", exception);
            System.exit(0);
            return;
        }

        log.info("Creating JDA Instance.");

        // Create a new Instance of the Bot, as well as add the Events.
        try {
            List<String> argList = Arrays.stream(args).map(String::toLowerCase).toList();

            if (argList.contains("--dev")) {
                BotWorker.createBot(BotVersion.DEVELOPMENT_BUILD);
            } else if (argList.contains("--prod")) {
                BotWorker.createBot(BotVersion.RELEASE);
            } else if (argList.contains("--beta")) {
                BotWorker.createBot(BotVersion.BETA_BUILD);
            } else {
                BotWorker.createBot(BotVersion.RELEASE);
            }

            instance.musicWorker = new MusicWorker();
            instance.addEvents();
        } catch (Exception ex) {
            log.error("[Main] Error while init: " + ex.getMessage());
            System.exit(0);
            return;
        }

        log.info("Loading SpotifyAPI");
        new SpotifyAPIHandler();

        log.info("Loading MusicQuizUtil");
        new MusicQuizUtil();

        log.info("Loading GameManager");
        GameManager.loadAllGames();

        log.info("Loading Stream-actions");
        StreamActionContainerCreator.loadAll();

        log.info("Creating Notifier.");

        // Create the Notifier-Manager instance.
        instance.notifier = new Notifier();

        ThreadUtil.createThread(x -> {
            log.info("Loading Notifier data.");
            List<ChannelStats> channelStats = SQLSession.getSqlConnector().getSqlWorker().getEntityList(new ChannelStats(), "SELECT * FROM ChannelStats", null);

            // Register all Twitch Channels.
            instance.notifier.registerTwitchChannel(SQLSession.getSqlConnector().getSqlWorker().getAllTwitchNames());
            instance.notifier.registerTwitchChannel(channelStats.stream().map(ChannelStats::getTwitchFollowerChannelUsername).filter(Objects::nonNull).toList());

            // Register the Event-handler.
            instance.notifier.registerTwitchEventHandler();

            // Register all Twitter Users.
            instance.notifier.registerTwitterUser(SQLSession.getSqlConnector().getSqlWorker().getAllTwitterNames());
            instance.notifier.registerTwitterUser(channelStats.stream().map(ChannelStats::getTwitterFollowerChannelUsername).filter(Objects::nonNull).toList());

            // Register all YouTube channels.
            instance.notifier.registerYouTubeChannel(SQLSession.getSqlConnector().getSqlWorker().getAllYouTubeChannels());
            instance.notifier.registerYouTubeChannel(channelStats.stream().map(ChannelStats::getYoutubeSubscribersChannelUsername).filter(Objects::nonNull).toList());

            // Register all Reddit Subreddits.
            instance.notifier.registerSubreddit(SQLSession.getSqlConnector().getSqlWorker().getAllSubreddits());
            instance.notifier.registerSubreddit(channelStats.stream().map(ChannelStats::getSubredditMemberChannelSubredditName).filter(Objects::nonNull).toList());

            // Register all Instagram Users.
            instance.notifier.registerInstagramUser(SQLSession.getSqlConnector().getSqlWorker().getAllInstagramUsers());
            instance.notifier.registerInstagramUser(channelStats.stream().map(ChannelStats::getInstagramFollowerChannelUsername).filter(Objects::nonNull).toList());
        }, t -> Sentry.captureException(t.getCause()));

        // Add the Runtime-hooks.
        instance.addHooks();

        // Set the start Time for stats.
        BotWorker.setStartTime(System.currentTimeMillis());

        // Initialize the Addon-Manager.
        instance.addonManager = new AddonManager();

        // Initialize the Addon-Loader.
        AddonLoader.loadAllAddons();

        // Start all Addons.
        instance.addonManager.startAddons();

        // Create checker Thread.
        instance.createCheckerThread();
    }

    /**
     * Called to add all Events.
     */
    private void addEvents() {
        BotWorker.addEvent(new GameEvents(), new LoggingEvents(), new MenuEvents(), new OtherEvents());
    }

    /**
     * Called to add all Runtime-hooks.
     */
    private void addHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Called when the application shutdowns.
     */
    private void shutdown() {
        // Current time for later stats.
        long start = System.currentTimeMillis();
        log.info("[Main] Shutdown init. !");
        BotWorker.setState(BotState.STOPPED);

        // Deleting every temporal voicechannel.
        for (String voiceIds : ArrayUtil.temporalVoicechannel) {
            VoiceChannel voiceChannel = BotWorker.getShardManager().getVoiceChannelById(voiceIds);

            if (voiceChannel != null) {
                voiceChannel.delete().complete();
            }
        }

        // Check if there is an SQL-connection if so, shutdown.
        if (SQLSession.getSqlConnector() != null && (SQLSession.getSqlConnector().isConnected())) {
            log.info("[Main] Closing Database Connection!");
            SQLSession.getSqlConnector().close();
            log.info("[Main] Closed Database Connection!");
        }

        // Shutdown every Addon.
        log.info("[Main] Disabling every Addon!");
        getAddonManager().stopAddons();
        log.info("[Main] Every Addon has been disabled!");

        // Close the Twitch-Client
        log.info("[Main] Closing Twitch API Instance!");
        getNotifier().getTwitchClient().close();
        log.info("[Main] Twitch API Instance closed!");

        // Shutdown the Bot instance.
        log.info("[Main] JDA Instance shutdown init. !");
        BotWorker.shutdown();
        log.info("[Main] JDA Instance has been shut down!");

        // Inform of how long it took.
        log.info("[Main] Everything has been shut down in {}ms!", System.currentTimeMillis() - start);
        log.info("[Main] Good bye!");
    }

    /**
     * Method creates a Thread used to create a Checker Thread.
     */
    public void createCheckerThread() {
        ThreadUtil.createThread(x -> {

            if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                ArrayUtil.messageIDwithMessage.clear();
                ArrayUtil.messageIDwithUser.clear();

                BotWorker.getShardManager().getShards().forEach(jda ->
                        BotWorker.setActivity(jda, "ree6.de | %guilds% Servers. (%shard%)", Activity.ActivityType.PLAYING));

                log.info("[Stats] ");
                log.info("[Stats] Today's Stats:");
                int guildSize = BotWorker.getShardManager().getGuilds().size(), userSize = BotWorker.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum();
                log.info("[Stats] Guilds: {}", guildSize);
                log.info("[Stats] Overall Users: {}", userSize);
                log.info("[Stats] ");

                LocalDate yesterday = LocalDate.now().minusDays(1);
                Statistics statistics = SQLSession.getSqlConnector().getSqlWorker().getStatistics(yesterday.getDayOfMonth(), yesterday.getMonthValue(), yesterday.getYear());
                JsonObject jsonObject = statistics != null ? statistics.getStatsObject() : new JsonObject();
                JsonObject guildStats = statistics != null && jsonObject.has("guild") ? jsonObject.getAsJsonObject("guild") : new JsonObject();

                guildStats.addProperty("amount", guildSize);
                guildStats.addProperty("users", userSize);

                jsonObject.add("guild", guildStats);

                SQLSession.getSqlConnector().getSqlWorker().updateStatistic(jsonObject);

                Calendar currentCalendar = Calendar.getInstance();

                SQLSession.getSqlConnector().getSqlWorker()
                        .getBirthdays().stream().filter(birthday -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(birthday.getBirthdate());
                            return calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                    calendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);
                        }).forEach(birthday -> {
                            TextChannel textChannel = BotWorker.getShardManager().getTextChannelById(birthday.getChannelId());

                            if (textChannel != null && textChannel.canTalk())
                                textChannel.sendMessage("Happy birthday to <@" + birthday.getUserId() + ">!").queue();
                        });

                lastDay = new SimpleDateFormat("dd").format(new Date());
            }

            File storageTemp = new File("storage/tmp/");
            if (storageTemp.isDirectory()) {
                File[] files = storageTemp.listFiles();
                if (files != null) {
                    Arrays.stream(files).forEach(f -> {
                        try {
                            Files.deleteIfExists(f.toPath());
                        } catch (IOException e) {
                            log.error("Couldn't delete file " + f.getName(), e);
                        }
                    });
                }
            }

            // Need to load them all.
            Main.getInstance().getNotifier().getCredentialManager().load();

            for (TwitchIntegration twitchIntegrations :
                    SQLSession.getSqlConnector().getSqlWorker().getEntityList(new TwitchIntegration(), "SELECT * FROM TwitchIntegration", null)) {

                CustomOAuth2Credential credential = CustomOAuth2Util.convert(twitchIntegrations);

                OAuth2Credential originalCredential = new OAuth2Credential("twitch", credential.getAccessToken(), credential.getRefreshToken(),  credential.getUserId(), credential.getUserName(), credential.getExpiresIn(), credential.getScopes());

                if (!Main.getInstance().getNotifier().getTwitchSubscription().containsKey(credential.getUserId())) {
                    PubSubSubscription[] subscriptions = new PubSubSubscription[2];
                    subscriptions[0] = Main.getInstance().getNotifier().getTwitchClient().getPubSub().listenForChannelPointsRedemptionEvents(originalCredential, twitchIntegrations.getChannelId());
                    subscriptions[1] = Main.getInstance().getNotifier().getTwitchClient().getPubSub().listenForSubscriptionEvents(originalCredential, twitchIntegrations.getChannelId());

                    Main.getInstance().getNotifier().getTwitchSubscription().put(credential.getUserId(), subscriptions);
                }
            }
        }, null, Duration.ofMinutes(1), true, false);
    }

    /**
     * Retrieve the Instance of the Main class.
     *
     * @return {@link Main} Instance of the Main class.
     */
    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }

        return instance;
    }

    /**
     * Retrieve the Instance of the Notifier.
     *
     * @return {@link Notifier} Instance of the Notifier.
     */
    public Notifier getNotifier() {
        return notifier;
    }

    /**
     * Retrieve the Instance of the CommandManager.
     *
     * @return {@link CommandManager} Instance of the CommandManager.
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Retrieve the Instance of the AddonManager.
     *
     * @return {@link AddonManager} Instance of the AddonManager.
     */
    public AddonManager getAddonManager() {
        return addonManager;
    }

    /**
     * Retrieve the Instance of the LoggerQueue.
     *
     * @return {@link LoggerQueue} Instance of the LoggerQueue.
     */
    public LoggerQueue getLoggerQueue() {
        return loggerQueue;
    }

    /**
     * Retrieve the Instance of the Music-Worker.
     *
     * @return {@link MusicWorker} Instance of the Music-Worker.
     */
    public MusicWorker getMusicWorker() {
        return musicWorker;
    }

    /**
     * Retrieve the Instance of the Config.
     *
     * @return {@link Config} Instance of the Config.
     */
    public Config getConfig() {
        return config;
    }
}
