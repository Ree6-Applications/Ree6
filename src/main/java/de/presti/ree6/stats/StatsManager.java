package de.presti.ree6.stats;

import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import java.util.HashMap;

/**
 * Utility class to manage Stats.
 */
public class StatsManager {

    /**
     * Add Stats for a Command.
     *
     * @param command an Instance of the Command.
     * @param guildId the ID of the Guild.
     */
    public static void addStatsForCommand(Command command, String guildId) {
        if (command != null) {
            Main.sqlConnector.getSqlWorker().addStats(guildId, command.getCmd());
        }
    }

    /**
     * Get a {@link HashMap<String, Long>} with the Stats of the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<String, Long>} with the Stats of the Guild.
     */
    public static HashMap<String, Long> getCommandStats(String guildId) {
        return Main.sqlConnector.getSqlWorker().getStats(guildId);
    }

    /**
     * Get the Usage of a Command.
     * @param command the Command.
     * @return {@link Long} as Usage.
     */
    public static long getUsageForCommand(Command command) {
        return Main.sqlConnector.getSqlWorker().getStatsCommandGlobal(command.getCmd());
    }

    /**
     * Get the Usage of a Command.
     * @param command the Command Name.
     * @return {@link Long} as Usage.
     */
    public static long getUsageForCommand(String command) {
        return Main.sqlConnector.getSqlWorker().getStatsCommandGlobal(command);
    }

    /**
     * Get a {@link HashMap<String, Long>} with every Stat.
     *
     * @return {@link HashMap<String, Long>} with every Stat.
     */
    public static HashMap<String, Long> getCommandStats() {
        return Main.sqlConnector.getSqlWorker().getStatsGlobal();
    }

}
