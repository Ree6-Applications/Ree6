package de.presti.ree6.main;

import com.mindscapehq.raygun4java.core.RaygunClient;
import de.presti.ree6.addons.AddonLoader;
import de.presti.ree6.addons.AddonManager;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotState;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.bot.BotVersion;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.events.LoggingEvents;
import de.presti.ree6.events.OtherEvents;
import de.presti.ree6.logger.LoggerQueue;
import de.presti.ree6.music.AudioPlayerSendHandler;
import de.presti.ree6.music.GuildMusicManager;
import de.presti.ree6.music.MusicWorker;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.Config;
import de.presti.ree6.utils.LoggerImpl;
import de.presti.ree6.utils.Notifier;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class Main {

    static Main instance;

    Notifier notifier;

    CommandManager commandManager;
    AddonManager addonManager;
    SQLConnector sqlConnector;
    LoggerQueue loggerQueue;
    MusicWorker musicWorker;

    Logger logger;

    Thread checker;
    Config config;

    String lastDay = "";

    public static void main(String[] args) {
        instance = new Main();

        instance.logger = LoggerFactory.getLogger(Main.class);

        instance.loggerQueue = new LoggerQueue();

        instance.config = new Config();

        instance.config.init();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> new RaygunClient(instance.config.getConfig().getString("raygun.apitoken")).send(e));

        instance.sqlConnector = new SQLConnector(instance.config.getConfig().getString("mysql.user"),
                instance.config.getConfig().getString("mysql.db"), instance.config.getConfig().getString("mysql.pw"),
                instance.config.getConfig().getString("mysql.host"), instance.config.getConfig().getInt("mysql.port"));

        instance.commandManager = new CommandManager();

        instance.notifier = new Notifier();

        instance.notifier.registerTwitchChannel(instance.sqlConnector.getSqlWorker().getAllTwitchNames());

        instance.notifier.registerTwitchEventHandler();

        try {
            BotUtil.createBot(BotVersion.DEV, "1.5.7");
            instance.musicWorker = new MusicWorker();
            instance.addEvents();
        } catch (Exception ex) {
            LoggerImpl.log("Main", "Error while init: " + ex.getMessage());
        }

        instance.addHooks();

        BotInfo.startTime = System.currentTimeMillis();

        instance.addonManager = new AddonManager();
        AddonLoader.loadAllAddons();
        instance.addonManager.startAddons();
    }

    private void addEvents() {
        BotUtil.addEvent(new OtherEvents());
        BotUtil.addEvent(new LoggingEvents());
    }

    private void addHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void shutdown() {
        long start = System.currentTimeMillis();
        LoggerImpl.log("Main", "Shutdown init. !");

        if (sqlConnector != null && (sqlConnector.IsConnected())) {
            LoggerImpl.log("Main", "Closing Database Connection!");
            getSqlConnector().close();
            LoggerImpl.log("Main", "Closed Database Connection!");
        }

        LoggerImpl.log("Main", "Disabling every Addon!");
        getAddonManager().stopAddons();
        LoggerImpl.log("Main", "Every Addon has been disabled!");

        LoggerImpl.log("Main", "Closing Twitch API Instance!");
        getNotifier().getTwitchClient().close();
        LoggerImpl.log("Main", "Twitch API Instance closed!");

        LoggerImpl.log("Main", "JDA Instance shutdown init. !");
        BotUtil.shutdown();
        LoggerImpl.log("Main", "JDA Instance has been shut down!");

        LoggerImpl.log("Main", "Everything has been shut down in " + (System.currentTimeMillis() - start) + "ms!");
        LoggerImpl.log("Main", "Good bye!");
    }

    public void createCheckerThread() {
        checker = new Thread(() -> {
            while (BotInfo.state != BotState.STOPPED) {

                if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                    getSqlConnector().close();

                    sqlConnector =new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.db"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getInt("mysql.port"));

                    ArrayUtil.messageIDwithMessage.clear();
                    ArrayUtil.messageIDwithUser.clear();

                    BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Guilds", Activity.ActivityType.WATCHING);

                    LoggerImpl.log("Stats", "");
                    LoggerImpl.log("Stats", "Today's Stats:");
                    LoggerImpl.log("Stats", "Guilds: " + BotInfo.botInstance.getGuilds().size());
                    LoggerImpl.log("Stats", "Overall Users: " + BotInfo.botInstance.getGuilds().stream().mapToInt(Guild::getMemberCount).sum());
                    LoggerImpl.log("Stats", "");
                    lastDay = new SimpleDateFormat("dd").format(new Date());
                }

                for (Guild g : BotInfo.botInstance.getGuilds().stream().filter(guild -> guild.getAudioManager().getSendingHandler() != null
                        && guild.getSelfMember().getVoiceState() != null &&
                        guild.getSelfMember().getVoiceState().inAudioChannel()).collect(Collectors.toList())) {
                    GuildMusicManager gmm = musicWorker.getGuildAudioPlayer(g);

                    try {
                        AudioPlayerSendHandler playerSendHandler = (AudioPlayerSendHandler) g.getAudioManager().getSendingHandler();

                        if (g.getSelfMember().getVoiceState() != null && g.getSelfMember().getVoiceState().inAudioChannel() && (playerSendHandler == null || !playerSendHandler.isMusicPlaying(g))) {
                            gmm.scheduler.stopAll(null);
                        }

                    } catch (Exception ex) {
                        gmm.scheduler.stopAll(null);
                        getLogger().error("Error", ex);
                    }
                }

                try {
                    Thread.sleep((10 * (60000L)));
                } catch (InterruptedException ignore) {
                }
            }
        });
        checker.start();
    }

    /**
     * Retrieve the Instance of the Main class.
     * @return {@link Main} Instance of the Main class.
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Retrieve the Instance of the Notifier.
     * @return {@link Notifier} Instance of the Notifier.
     */
    public Notifier getNotifier() {
        return notifier;
    }

    /**
     * Retrieve the Instance of the CommandManager.
     * @return {@link CommandManager} Instance of the CommandManager.
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Retrieve the Instance of the AddonManager.
     * @return {@link AddonManager} Instance of the AddonManager.
     */
    public AddonManager getAddonManager() {
        return addonManager;
    }

    /**
     * Retrieve the Instance of the SQL-Connector.
     * @return {@link SQLConnector} Instance of the SQL-Connector.
     */
    public SQLConnector getSqlConnector() {
        return sqlConnector;
    }

    /**
     * Retrieve the Instance of the LoggerQueue.
     * @return {@link LoggerQueue} Instance of the LoggerQueue.
     */
    public LoggerQueue getLoggerQueue() {
        return loggerQueue;
    }

    /**
     * Retrieve the Instance of the Music-Worker.
     * @return {@link MusicWorker} Instance of the Music-Worker.
     */
    public MusicWorker getMusicWorker() {
        return musicWorker;
    }

    /**
     * Retrieve the Instance of the Logger.
     * @return {@link Logger} Instance of the Logger.
     */
    public Logger getLogger() { return logger; }

    /**
     * Retrieve the Instance of the Checker-Thread.
     * @return {@link Thread} Instance of the Checker-Thread.
     */
    public Thread getChecker() {
        return checker;
    }

    /**
     * Retrieve the Instance of the Config.
     * @return {@link Config} Instance of the Config.
     */
    public Config getConfig() {
        return config;
    }
}
