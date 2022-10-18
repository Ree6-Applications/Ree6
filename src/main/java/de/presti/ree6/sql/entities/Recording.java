package de.presti.ree6.sql.entities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.presti.ree6.sql.converter.ByteAttributeConverter;
import de.presti.ree6.sql.converter.JsonAttributeConverter;
import de.presti.ree6.utils.others.RandomUtils;
import jakarta.persistence.*;

/**
 * This class is used to represent a Ree6-Voice-Recording, in our Database.
 */
@Entity
@Table(name = "Recording")
public class Recording {

    /**
     * The Identifier for the recording.
     */
    @Id
    @Column(name = "id")
    String identifier;

    /**
     * The ID of the Guild.
     */
    @Column(name = "gid")
    String guildId;

    /**
     * The ID of the Voice-Channel.
     */
    @Column(name = "vid")
    String voiceId;

    /**
     * The ID of the Creator.
     */
    @Column(name = "creator")
    String creatorId;

    /**
     * The WAV-File bytes.
     */
    @Convert(converter = ByteAttributeConverter.class)
    @Column(name = "recording")
    byte[] recording;

    /**
     * An JsonArray containing the IDs of the Users who have participated in the Recording.
     */
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "participants")
    JsonElement jsonArray;

    /**
     * Value used to tell us when this entry was made.
     */
    @Column(name = "created")
    long creation;

    /**
     * Constructor
     */
    public Recording() {
    }

    /**
     * Constructor.
     * @param guildId the ID of the Guild.
     * @param voiceId the ID of the Voice-Channel.
     * @param creatorId the ID of the Creator.
     * @param recording the WAV-File bytes.
     * @param jsonArray an JsonArray containing the IDs of the Users who have participated in the Recording.
     */
    public Recording(String guildId, String voiceId, String creatorId, byte[] recording, JsonElement jsonArray) {
        this.identifier = RandomUtils.getRandomBase64String(16);
        this.guildId = guildId;
        this.voiceId = voiceId;
        this.creatorId = creatorId;
        this.recording = recording;
        this.jsonArray = jsonArray;
        this.creation = System.currentTimeMillis();
    }

    /**
     * Get the Identifier for the recording.
     * @return the Identifier for the recording.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the ID of the Guild.
     * @return the ID of the Guild.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Get the ID of the Voice-Channel.
     * @return the ID of the Voice-Channel.
     */
    public String getVoiceId() {
        return voiceId;
    }

    /**
     * Get the ID of the Creator.
     * @return the ID of the Creator.
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Get the WAV-File bytes.
     * @return the WAV-File bytes.
     */
    public byte[] getRecording() {
        return recording;
    }

    /**
     * Get the IDs of the Users who have participated in the Recording.
     * @return the IDs of the Users who have participated in the Recording.
     */
    public JsonArray getJsonArray() {
        return jsonArray.getAsJsonArray();
    }

    /**
     * Get the creation time of this entry.
     * @return the creation time of this entry.
     */
    public long getCreation() {
        return creation;
    }
}
