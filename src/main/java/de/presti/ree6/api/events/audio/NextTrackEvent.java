package de.presti.ree6.api.events.audio;

import best.azura.eventbus.events.CancellableEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class NextTrackEvent extends CancellableEvent {

    private final long guildId;
    private final AudioTrack audioTrack;

    public NextTrackEvent(long guildId, AudioTrack audioTrack) {
        this.guildId = guildId;
        this.audioTrack = audioTrack;
    }

    public long getGuildId() {
        return guildId;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }
}
