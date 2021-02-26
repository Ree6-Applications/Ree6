package de.presti.ree6.main;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.BotState;
import de.presti.ree6.bot.BotUtil;
import de.presti.ree6.bot.BotVersion;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.events.BotManagingEvent;
import de.presti.ree6.events.Logging;
import de.presti.ree6.music.MusikWorker;
import de.presti.ree6.sql.SQLConnector;
import de.presti.ree6.sql.SQLWorker;
import de.presti.ree6.utils.ArrayUtil;
import de.presti.ree6.utils.Config;
import de.presti.ree6.utils.LinkConverter;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static Main insance;
    public static CommandManager cm;
    public static SQLConnector sqlConnector;
    public static SQLWorker sqlWorker;
    public static Thread checker;
    public static Config config;
    public static String lastday = "";

    public static void main(String[] args) {
        insance = new Main();

        config = new Config();

        config.init();

        sqlConnector = new SQLConnector(config.getConfig().getString("mysql.user"), config.getConfig().getString("mysql.pw"), config.getConfig().getString("mysql.host"), config.getConfig().getString("mysql.db"), config.getConfig().getInt("mysql.port"));

        sqlWorker = new SQLWorker();

        new LinkConverter();
        cm = new CommandManager();


        try {
            BotUtil.createBot(BotVersion.PUBLIC);
            new MusikWorker();
            insance.addEvents();
        } catch (Exception ex) {
            System.out.println("Error while init: " + ex.getMessage());
        }
        insance.addHooks();
    }

    private void addEvents() {
        BotUtil.addEvent(new BotManagingEvent());
        BotUtil.addEvent(new Logging());
    }

    private void addHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }));
    }

    private void shutdown() throws SQLException {
        System.out.println("Good Bye!");
        sqlWorker.saveAllInvites();
        sqlWorker.saveAllChatProtectors();
        sqlConnector.close();
        BotUtil.shutdown();
    }

    public void createCheckerThread() {
        checker = new Thread(() -> {
            while(BotInfo.state != BotState.STOPPED) {

                if(!lastday.equalsIgnoreCase(new SimpleDateFormat("dd").format(new Date()))) {

                    ArrayUtil.messageIDwithMessage.clear();
                    ArrayUtil.messageIDwithUser.clear();

                    BotUtil.setActivity(BotInfo.botInstance.getGuilds().size() + " Guilds", Activity.ActivityType.WATCHING);

                    System.out.println();
                    System.out.println("Todays Stats:");
                    System.out.println("Guilds: " + BotInfo.botInstance.getGuilds().size());

                    int i = 0;

                    for(Guild guild : BotInfo.botInstance.getGuilds()) {
                        i += guild.getMemberCount();
                    }

                    System.out.println("Overall Users: " + i);

                    lastday = new SimpleDateFormat("dd").format(new Date());
                }


                try {
                    Thread.sleep( (5 * (60000L)));
                } catch (InterruptedException e) {}
            }
        });
        checker.start();
    }
}
