package de.presti.ree6.sql.entities.stats;

import jakarta.persistence.*;

/**
 * SQL Entity for the Stats.
 */
@Entity
@Table(name = "CommandStats")
public class CommandStats {

    /**
     * The PrimaryKey of the Entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /**
     * Name of the Command.
     */
    @Column(name = "command")
    private String command;

    /**
     * Number of times the Command was used.
     */
    @Column(name = "uses")
    private int uses;

    /**
     * Constructor.
     */
    public CommandStats() {
    }

    /**
     * Constructor.
     * @param command Name of the Command.
     * @param uses Number of times the Command was used.
     */
    public CommandStats(String command, int uses) {
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
