package de.presti.ree6.sql.entities;

import com.google.gson.JsonArray;
import de.presti.ree6.sql.base.annotations.Property;
import de.presti.ree6.sql.base.annotations.Table;
import de.presti.ree6.sql.base.entitis.SQLEntity;
import de.presti.ree6.utils.others.RandomUtils;

/**
 * This class is used to represent a Ree6-Voice-Recording, in our Database.
 */
@Table(name = "Recording")
public class Recording extends SQLEntity {

    /**
     * The Identifier for the recording.
     */
    @Property(name = "id", primary = true)
    String identifier;

    /**
     * The ID of the Guild.
     */
    @Property(name = "gid")
    String guildId;

    /**
     * The ID of the Voice-Channel.
     */
    @Property(name = "vid")
    String voiceId;

    /**
     * The ID of the Creator.
     */
    @Property(name = "creator")
    String creatorId;

    /**
     * The WAV-File bytes.
     */
    @Property(name = "recording", updateQuery = true, keepOriginalValue = false)
    byte[] recording;

    /**
     * An JsonArray containing the IDs of the Users who have participated in the Recording.
     */
    @Property(name = "participants", keepOriginalValue = false)
    JsonArray jsonArray;

    /**
     * Value used to tell us when this entry was made.
     */
    @Property(name = "created")
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
    public Recording(String guildId, String voiceId, String creatorId, byte[] recording, JsonArray jsonArray) {
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
        return jsonArray;
    }

    /**
     * Get the creation time of this entry.
     * @return the creation time of this entry.
     */
    public long getCreation() {
        return creation;
    }
}
