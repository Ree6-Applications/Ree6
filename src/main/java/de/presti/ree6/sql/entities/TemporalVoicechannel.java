package de.presti.ree6.sql.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Class used to store information about the temporal Voice-Channel.
 */
@Entity
@Table(name = "TemporalVoicechannel")
public class TemporalVoicechannel {

    /**
     * The ID of the Guild.
     */
    @Id
    @Column(name = "gid")
    String guildId;

    /**
     * The ID of the Voice-channel.
     */
    @Column(name = "vid")
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
