package de.presti.ree6.stats;

import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import java.util.HashMap;

public class StatsManager {

    public static void addStatsForCommand(Command cmd, String gid) {
        if (cmd != null) {
            Main.sqlConnector.getSqlWorker().addStats(gid, cmd.getCmd());
        }
    }

    public static HashMap<String, Long> getCommandStats(String gid) {
        return Main.sqlConnector.getSqlWorker().getStats(gid);
    }

    public static long getUsageForCommand(Command command) {
        return Main.sqlConnector.getSqlWorker().getStatsCommandGlobal(command.getCmd());
    }

    public static long getUsageForCommand(String command) {
        return Main.sqlConnector.getSqlWorker().getStatsCommandGlobal(command);
    }

    public static HashMap<String, Long> getCommandStats() {
        return Main.sqlConnector.getSqlWorker().getStatsGlobal();
    }

}
