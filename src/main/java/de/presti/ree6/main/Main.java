package de.presti.ree6.main;

import com.mindscapehq.raygun4java.core.RaygunClient;
import de.presti.ree6.addons.AddonLoader;
import de.presti.ree6.addons.AddonManager;
import de.presti.ree6.audio.AudioPlayerSendHandler;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.audio.music.MusicWorker;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.bot.version.BotVersion;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.events.LoggingEvents;
import de.presti.ree6.events.OtherEvents;
import de.presti.ree6.logger.events.LoggerQueue;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.utils.apis.Notifier;
import de.presti.ree6.utils.data.ArrayUtil;
import de.presti.ree6.utils.data.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Application class, used to store Instances of System Relevant classes.
 */
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
     * Instance of the SQL-Connector used to manage the connection between the SQL Server and the Application.
     */
    SQLConnector sqlConnector;

    /**
     * Instance of the LoggerQueue, used to merge Logs to prevent Rate-Limits.
     */
    LoggerQueue loggerQueue;

    /**
     * Instance of the MusicWorker used to manage the Music-Player.
     */
    MusicWorker musicWorker;

    /**
     * Instance of the Logger used to log the Command output.
     */
    Logger logger;

    /**
     * Instance of the Logger used to store debug information.
     */
    Logger analyticsLogger;

    /**
     * Instance of the Config System.
     */
    Config config;

    /**
     * A Thread used to check if a day has passed, and if so to clean the cache.
     */
    Thread checker;

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

        // Create the Logger Instance.
        instance.logger = LoggerFactory.getLogger(Main.class);
        instance.analyticsLogger = LoggerFactory.getLogger("analytics");

        // Create the LoggerQueue Instance.
        instance.loggerQueue = new LoggerQueue();

        // Create the Config System Instance.
        instance.config = new Config();

        // Initialize the Config.
        instance.config.init();

        // Check if there is a default value, if so close application and inform.
        if (instance.config.getConfiguration().getString("mysql.pw").equalsIgnoreCase("yourpw")) {
            instance.logger.error("It looks like the default configuration has not been updated!");
            instance.logger.error("Please update the configuration file and restart the application!");
            System.exit(0);
        }

        instance.logger.info("Starting Ree6!");

        instance.logger.info("Creating RayGun Instance.");
        // Create a RayGun Client to send Exception to an external Service for Bug fixing.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            RaygunClient raygunClient = new RaygunClient(instance.config.getConfiguration().getString("raygun.apitoken"));
            raygunClient.setVersion("1.7.20");
        });

        // Create a new connection between the Application and the SQL-Server.
        instance.sqlConnector = new SQLConnector(instance.config.getConfiguration().getString("mysql.user"),
                instance.config.getConfiguration().getString("mysql.db"), instance.config.getConfiguration().getString("mysql.pw"),
                instance.config.getConfiguration().getString("mysql.host"), instance.config.getConfiguration().getInt("mysql.port"));

        try {
            // Create the Command-Manager instance.
            instance.commandManager = new CommandManager();
        } catch (Exception exception) {
            instance.logger.error("Shutting down, because of an critical error!", exception);
            return;
        }

        instance.logger.info("Creating Notifier.");

        // Create the Notifier-Manager instance.
        instance.notifier = new Notifier();

        // Register all Twitch Channels.
        instance.notifier.registerTwitchChannel(instance.sqlConnector.getSqlWorker().getAllTwitchNames());

        // Register the Event-handler.
        instance.notifier.registerTwitchEventHandler();

        // Register all Twitter Users.
        instance.notifier.registerTwitterUser(instance.sqlConnector.getSqlWorker().getAllTwitterNames());

        // Register all YouTube channels.
        instance.notifier.registerYouTubeChannel(instance.sqlConnector.getSqlWorker().getAllYouTubeChannels());

        instance.logger.info("Creating JDA Instance.");

        // Create a new Instance of the Bot, as well as add the Events.
        try {
            BotWorker.createBot(BotVersion.DEVELOPMENT_BUILD, "1.8.0");
            instance.musicWorker = new MusicWorker();
            instance.addEvents();
        } catch (Exception ex) {
            instance.logger.error("[Main] Error while init: " + ex.getMessage());
        }

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
        BotWorker.addEvent(new OtherEvents());
        BotWorker.addEvent(new LoggingEvents());
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
        instance.logger.info("[Main] Shutdown init. !");
        BotWorker.setState(BotState.STOPPED);

        // Check if there is an SQL-connection if so, shutdown.
        if (sqlConnector != null && (sqlConnector.isConnected())) {
            instance.logger.info("[Main] Closing Database Connection!");
            getSqlConnector().close();
            instance.logger.info("[Main] Closed Database Connection!");
        }

        // Shutdown every Addon.
        instance.logger.info("[Main] Disabling every Addon!");
        getAddonManager().stopAddons();
        instance.logger.info("[Main] Every Addon has been disabled!");

        // Close the Twitch-Client
        instance.logger.info("[Main] Closing Twitch API Instance!");
        getNotifier().getTwitchClient().close();
        instance.logger.info("[Main] Twitch API Instance closed!");

        if (checker != null && !checker.isInterrupted()) {
            // Shutdown Checker Thread.
            instance.logger.info("[Main] Interrupting the Checker thread!");
            checker.interrupt();
            instance.logger.info("[Main] Interrupted the Checker thread!");
        }

        // Shutdown the Bot instance.
        instance.logger.info("[Main] JDA Instance shutdown init. !");
        BotWorker.shutdown();
        instance.logger.info("[Main] JDA Instance has been shut down!");

        // Inform of how long it took.
        instance.logger.info("[Main] Everything has been shut down in {}ms!", System.currentTimeMillis() - start);
        instance.logger.info("[Main] Good bye!");
    }

    /**
     * Method creates a Thread used to create a Checker Thread.
     */
    public void createCheckerThread() {
        checker = new Thread(() -> {
            while (BotWorker.getState() != BotState.STOPPED) {

                if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                    if (BotWorker.getStartTime() > System.currentTimeMillis() + 10000) {
                        getSqlConnector().close();
                        sqlConnector = new SQLConnector(config.getConfiguration().getString("mysql.user"), config.getConfiguration().getString("mysql.db"), config.getConfiguration().getString("mysql.pw"), config.getConfiguration().getString("mysql.host"), config.getConfiguration().getInt("mysql.port"));
                    }

                    ArrayUtil.messageIDwithMessage.clear();
                    ArrayUtil.messageIDwithUser.clear();

                    for (JDA jda : BotWorker.getShardManager().getShards()) {
                        BotWorker.setActivity(jda, "ree6.de | %guilds% Servers. (%shard%)", Activity.ActivityType.PLAYING);
                    }

                    instance.logger.info("[Stats] ");
                    instance.logger.info("[Stats] Today's Stats:");
                    instance.logger.info("[Stats] Guilds: {}", BotWorker.getShardManager().getGuilds().size());
                    instance.logger.info("[Stats] Overall Users: {}", BotWorker.getShardManager().getGuilds().stream().mapToInt(Guild::getMemberCount).sum());
                    instance.logger.info("[Stats] ");
                    lastDay = new SimpleDateFormat("dd").format(new Date());
                }

                for (Guild guild : BotWorker.getShardManager().getGuilds().stream().filter(guild -> guild.getAudioManager().getSendingHandler() != null
                        && guild.getSelfMember().getVoiceState() != null &&
                        guild.getSelfMember().getVoiceState().inAudioChannel()).toList()) {
                    GuildMusicManager guildMusicManager = musicWorker.getGuildAudioPlayer(guild);

                    try {
                        AudioPlayerSendHandler playerSendHandler = (AudioPlayerSendHandler) guild.getAudioManager().getSendingHandler();

                        if (guild.getSelfMember().getVoiceState() != null && guild.getSelfMember().getVoiceState().inAudioChannel() && (playerSendHandler == null ||
                                !playerSendHandler.isMusicPlaying(guild))) {
                            guildMusicManager.scheduler.stopAll(guild, null);
                        }

                    } catch (Exception ex) {
                        guildMusicManager.scheduler.stopAll(guild, null);
                        getLogger().error("Error accessing the AudioPlayer.", ex);
                    }
                }

                try {
                    Thread.sleep((10 * (60000L)));
                } catch (InterruptedException exception) {
                    getInstance().getLogger().error("Checker Thread interrupted", exception);
                }
            }
        });
        checker.start();
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
     * Retrieve the Instance of the SQL-Connector.
     *
     * @return {@link SQLConnector} Instance of the SQL-Connector.
     */
    public SQLConnector getSqlConnector() {
        return sqlConnector;
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
     * Retrieve the Instance of the Logger.
     *
     * @return {@link Logger} Instance of the Logger.
     */
    public Logger getLogger() {
        if (logger == null) {
            return logger = LoggerFactory.getLogger(Main.class);
        }
        return logger;
    }

    /**
     * Retrieve the Instance of the Analytics-Logger.
     *
     * @return {@link Logger} Instance of the Logger.
     */
    public Logger getAnalyticsLogger() {
        if (analyticsLogger == null) {
            return analyticsLogger = LoggerFactory.getLogger("analytics");
        }
        return analyticsLogger;
    }

    /**
     * Retrieve the Instance of the Checker-Thread.
     *
     * @return {@link Thread} Instance of the Checker-Thread.
     */
    public Thread getChecker() {
        return checker;
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
