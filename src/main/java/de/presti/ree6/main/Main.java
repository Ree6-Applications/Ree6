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
import de.presti.ree6.music.GuildMusicManager;
import de.presti.ree6.music.MusicWorker;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.SQLWorker;
import de.presti.ree6.utils.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Main {

    public static Main instance;

    public static TwitchAPIHandler twitchAPIHandler;

    public static CommandManager commandManager;
    public static AddonManager addonManager;
    public static SQLConnector sqlConnector;
    public static SQLWorker sqlWorker;
    public static LoggerQueue loggerQueue;
    public static MusicWorker musicWorker;
    
    public static Thread checker;
    public static Config config;
    
    public static String lastDay = "";

    public static void main(String[] args) {
        instance = new Main();

        loggerQueue = new LoggerQueue();

        config = new Config();

        config.init();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> new RaygunClient(config.getConfig().getString("raygun.apitoken")).send(e));

        sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getString("mysql.db"), config.getConfig().getInt("mysql.port"));

        sqlWorker = new SQLWorker();

        commandManager = new CommandManager();

        twitchAPIHandler = new TwitchAPIHandler();

        for(String name : sqlWorker.getAllTwitchNotifyUsers()) {
            twitchAPIHandler.registerChannel(name);
        }

        twitchAPIHandler.registerTwitchLive();

        try {
            BotUtil.createBot(BotVersion.PRERELASE, "1.5.0");
            musicWorker = new MusicWorker();
            instance.addEvents();

            commandManager.addSlashCommand();

        } catch (Exception ex) {
            Logger.log("Main", "Error while init: " + ex.getMessage());
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
        Logger.log("Main", "Shutdown init. !");

        if (sqlConnector != null && (sqlConnector.isConnected() || sqlConnector.isConnected2())) {
            Logger.log("Main", "Closing Database Connection!");
            sqlConnector.close();
            Logger.log("Main", "Closed Database Connection!");
        }

        Logger.log("Main", "Disabling every Addon!");
        addonManager.stopAddons();
        Logger.log("Main", "Every Addon has been disabled!");

        Logger.log("Main", "JDA Instance shutdown init. !");
        BotUtil.shutdown();
        Logger.log("Main", "JDA Instance has been shutdowned!");

        Logger.log("Main", "Everything has been shutdowned in " + (System.currentTimeMillis() - start) + "ms!");
        Logger.log("Main", "Good bye!");
    }

    public void createCheckerThread() {
        checker = new Thread(() -> {
            while (BotInfo.state != BotState.STOPPED) {

                if (!lastDay.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                    sqlConnector.close();

                    sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getString("mysql.db"), config.getConfig().getInt("mysql.port"));
                    sqlWorker = new SQLWorker();


                    ArrayUtil.messageIDwithMessage.clear();
                    ArrayUtil.messageIDwithUser.clear();

                    BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Guilds", Activity.ActivityType.WATCHING);

                    Logger.log("Stats", "");
                    Logger.log("Stats", "Today's Stats:");
                    Logger.log("Stats", "Guilds: " + BotInfo.botInstance.getGuilds().size());
                    Logger.log("Stats", "Overall Users: " + BotInfo.botInstance.getGuilds().stream().mapToInt(Guild::getMemberCount).sum());
                    Logger.log("Stats", "");
                    lastDay = new SimpleDateFormat("dd").format(new Date());
                }

                for (Guild g : BotInfo.botInstance.getGuilds()) {
                    GuildMusicManager gmm = musicWorker.getGuildAudioPlayer(g);

                    if (musicWorker.isConnected(gmm.guild) && (gmm.player.getPlayingTrack() == null || gmm.player.isPaused())) {
                        gmm.scheduler.stopAll();
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
}
