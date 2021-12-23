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
import de.presti.ree6.sql.SQLWorker;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.Config;
import de.presti.ree6.utils.LoggerImpl;
import de.presti.ree6.utils.TwitchAPIHandler;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class Main {

    public static Main instance;

    public static TwitchAPIHandler twitchAPIHandler;

    public static CommandManager commandManager;
    public static AddonManager addonManager;
    public static SQLConnector sqlConnector;
    public static LoggerQueue loggerQueue;
    public static MusicWorker musicWorker;

    static Logger logger;

    public static Thread checker;
    public static Config config;

    public static String lastDay = "";

    public static void main(String[] args) {
        instance = new Main();

        logger = LoggerFactory.getLogger(Main.class);

        loggerQueue = new LoggerQueue();

        config = new Config();

        config.init();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> new RaygunClient(config.getConfig().getString("raygun.apitoken")).send(e));

        sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getString("mysql.db"), config.getConfig().getInt("mysql.port"));

        commandManager = new CommandManager();

        twitchAPIHandler = new TwitchAPIHandler();

        for (String name : sqlConnector.getSqlWorker().getAllTwitchNames()) {
            twitchAPIHandler.registerChannel(name);
        }

        twitchAPIHandler.registerTwitchLive();

        try {
            BotUtil.createBot(BotVersion.PRERELASE, "1.5.0");
            musicWorker = new MusicWorker();
            instance.addEvents();
        } catch (Exception ex) {
            LoggerImpl.log("Main", "Error while init: " + ex.getMessage());
        }

        instance.addHooks();

        BotInfo.startTime = System.currentTimeMillis();

        addonManager = new AddonManager();
        AddonLoader.loadAllAddons();
        addonManager.startAddons();
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
            sqlConnector.close();
            LoggerImpl.log("Main", "Closed Database Connection!");
        }

        LoggerImpl.log("Main", "Disabling every Addon!");
        addonManager.stopAddons();
        LoggerImpl.log("Main", "Every Addon has been disabled!");

        LoggerImpl.log("Main", "JDA Instance shutdown init. !");
        BotUtil.shutdown();
        LoggerImpl.log("Main", "JDA Instance has been shutdowned!");

        LoggerImpl.log("Main", "Everything has been shutdowned in " + (System.currentTimeMillis() - start) + "ms!");
        LoggerImpl.log("Main", "Good bye!");
    }

    public void createCheckerThread() {
        checker = new Thread(() -> {
            while (BotInfo.state != BotState.STOPPED) {

                if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                    sqlConnector.close();

                    sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getString("mysql.db"), config.getConfig().getInt("mysql.port"));


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

                        if (g.getSelfMember().getVoiceState() != null && g.getSelfMember().getVoiceState().inAudioChannel() && !playerSendHandler.isMusicPlaying(g)) {
                            gmm.scheduler.stopAll();
                        }

                    } catch (Exception ex) {
                        gmm.scheduler.stopAll();
                        ex.printStackTrace();
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
     * Retrieve the Instance of the Logger.
     * @return {@link Logger} Instance of the Logger.
     */
    public Logger getLogger() { return logger; }
}
