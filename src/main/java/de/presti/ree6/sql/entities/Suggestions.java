package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.entities.SQLEntity;

/**
 * Class used to store information about the Suggestions.
 */
@Table(name = "Suggestions")
public class Suggestions extends SQLEntity {

    /**
     * The ID of the Guild.
     */
    @Property(name = "guildId")
    long guildId;

    /**
     * The ID of the Channel.
     */
    @Property(name = "channelId", updateQuery = true)
    long channelId;

    /**
     * Constructor.
     */
    public Suggestions() {
    }

    /**
     * Constructor for the Suggestions.
     *
     * @param guildId the ID of the Guild.
     * @param channelId the ID of the Channel.
     */
    public Suggestions(long guildId, long channelId) {
        this.guildId = guildId;
        this.channelId = channelId;
    }

    /**
     * Get the ID of the Guild.
     *
     * @return the ID of the Guild.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Get the ID of the Channel.
     *
     * @return the ID of the Channel.
     */
    public long getChannelId() {
        return channelId;
    }
}
