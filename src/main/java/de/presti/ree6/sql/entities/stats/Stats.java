package de.presti.ree6.sql.entities.stats;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.data.SQLEntity;

/**
 * SQL Entity for the Stats.
 */
@Table(name = "CommandStats")
public class Stats extends SQLEntity {

    /**
     * Name of the Command.
     */
    @Property(name = "command")
    private String command;

    /**
     * Number of times the Command was used.
     */
    @Property(name = "uses", updateQuery = true)
    private int uses;

    /**
     * Constructor.
     */
    public Stats() {
    }

    /**
     * Constructor.
     * @param command Name of the Command.
     * @param uses Number of times the Command was used.
     */
    public Stats(String command, int uses) {
        this.command = command;
        this.uses = uses;
    }

    /**
     * Get the name of the Command.
     * @return the command name.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Get the number of times the Command was used.
     * @return the number of uses.
     */
    public int getUses() {
        return uses;
    }

    /**
     * Set the number of times the Command was used.
     * @param uses the number of uses.
     */
    public void setUses(int uses) {
        this.uses = uses;
    }
}
