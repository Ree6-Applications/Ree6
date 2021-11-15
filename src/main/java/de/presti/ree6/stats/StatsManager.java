package de.presti.ree6.stats;

import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import java.util.HashMap;

public class StatsManager {

    public static void addStatsForCommand(Command cmd, String gid) {
        if (cmd != null) {
            Main.sqlWorker.addStats(cmd, gid);
        }
    }

    public static HashMap<String, Long> getCommandStats(String gid) {
        return Main.sqlWorker.getStatsFromGuild(gid);
    }

    public static long getUsageForCommand(Command command) {
        return Main.sqlWorker.getStatsFromCommand(command.getCmd());
    }

    public static long getUsageForCommand(String command) {
        return Main.sqlWorker.getStatsFromCommand(command);
    }

    public static HashMap<String, Long> getCommandStats() {
        return Main.sqlWorker.getStatsForCommands();
    }

}
