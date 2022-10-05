package de.presti.ree6.sql.entities;

import jakarta.persistence.*;

/**
 * Class used to store information about the Suggestions.
 */
@Table(name = "Suggestions")
public class Suggestions {

    /**
     * The ID of the Guild.
     */
    long guildId;

    /**
     * The ID of the Channel.
     */
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
    @Id
    @Column(name = "guildId")
    public long getGuildId() {
        return guildId;
    }

    /**
     * Get the ID of the Channel.
     *
     * @return the ID of the Channel.
     */
    @Column(name = "channelId")
    public long getChannelId() {
        return channelId;
    }
}
