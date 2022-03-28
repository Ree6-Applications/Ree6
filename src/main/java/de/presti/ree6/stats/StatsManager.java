package de.presti.ree6.stats;

import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;

import java.util.HashMap;

// TODO rework, this class isn't really needed and only serves a single purpose which could be just migrated into a overall utility class.

/**
 * Utility class to manage Stats.
 */
public class StatsManager {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     * @throws IllegalStateException it is a utility class.
     */
    private StatsManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Add Stats for a Command.
     *
     * @param command an Instance of the Command.
     * @param guildId the ID of the Guild.
     */
    public static void addStatsForCommand(ICommand command, String guildId) {
        if (command != null) {
            Main.getInstance().getSqlConnector().getSqlWorker().addStats(guildId, command.getClass().getAnnotation(Command.class).name());
        }
    }

    /**
     * Get a {@link HashMap<String, Long>} with the Stats of the Guild.
     *
     * @param guildId the ID of the Guild.
     * @return {@link HashMap<String, Long>} with the Stats of the Guild.
     */
    public static HashMap<String, Long> getCommandStats(String guildId) {
        return Main.getInstance().getSqlConnector().getSqlWorker().getStats(guildId);
    }

    /**
     * Get the Usage of a Command.
     * @param command the Command.
     * @return {@link Long} as Usage.
     */
    public static long getUsageForCommand(ICommand command) {
        return Main.getInstance().getSqlConnector().getSqlWorker().getStatsCommandGlobal(command.getClass().getAnnotation(Command.class).name());
    }

    /**
     * Get the Usage of a Command.
     * @param command the Command Name.
     * @return {@link Long} as Usage.
     */
    public static long getUsageForCommand(String command) {
        return Main.getInstance().getSqlConnector().getSqlWorker().getStatsCommandGlobal(command);
    }

    /**
     * Get a {@link HashMap<String, Long>} with every Stat.
     *
     * @return {@link HashMap<String, Long>} with every Stat.
     */
    public static HashMap<String, Long> getCommandStats() {
        return Main.getInstance().getSqlConnector().getSqlWorker().getStatsGlobal();
    }

}
