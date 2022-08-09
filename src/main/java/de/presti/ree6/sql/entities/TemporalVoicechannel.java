package de.presti.ree6.sql.entities;

import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.entitis.SQLEntity;

/**
 * Class used to store information about the temporal Voice-Channel.
 */
@Table(name = "TemporalVoicechannel")
public class TemporalVoicechannel extends SQLEntity {

    /**
     * The ID of the Guild.
     */
    @Property(name = "gid")
    String guildId;

    /**
     * The ID of the Voice-channel.
     */
    @Property(name = "vid", updateQuery = true)
    String voiceChannelId;

    /**
     * Constructor.
     */
    public TemporalVoicechannel() {
    }

    /**
     * Constructor for the TemporalVoicechannel.
     *
     * @param guildId the ID of the Guild.
     * @param voiceChannelId the ID of the Voice-channel.
     */
    public TemporalVoicechannel(String guildId, String voiceChannelId) {
        this.guildId = guildId;
        this.voiceChannelId = voiceChannelId;
    }

    /**
     * Get the ID of the Guild.
     *
     * @return the ID of the Guild.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the ID of the Voice-channel.
     *
     * @return the ID of the Voice-channel.
     */
    public String getVoiceChannelId() {
        return voiceChannelId;
    }
}
