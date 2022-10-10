package de.presti.ree6.sql.entities.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * SQL Entity for the Guild-Stats.
 */
@Entity
@Table(name = "GuildStats")
public class GuildCommandStats extends CommandStats {

    /**
     * The Guild ID.
     */
    @Column(name = "gid")
    private String guildId;

    /**
     * Constructor.
     */
    public GuildCommandStats() {
    }

    /**
     * Constructor.
     *
     * @param command Name of the Command.
     * @param uses    Number of times the Command was used.
     */
    public GuildCommandStats(String guildId, String command, int uses) {
        super(command, uses);
        this.guildId = guildId;
    }

    /**
     * Get the Guild ID.
     * @return the guild ID.
     */
    public String getGuild() {
        return guildId;
    }
}
