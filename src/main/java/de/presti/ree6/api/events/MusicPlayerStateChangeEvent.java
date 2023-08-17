package de.presti.ree6.api.events;

import best.azura.eventbus.core.Event;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Event class used to contain information about the current state of the music player.
 */
@Getter
@AllArgsConstructor
public class MusicPlayerStateChangeEvent implements Event {

    /**
     * The guild the event has been fired in.
     */
    private final Guild guild;

    /**
     * The current state of the music player.
     */
    private final State state;

    /**
     * The current track that is being played.
     */
    private AudioTrack track;

    /**
     * Enum used to store the current state of the music player.
     */
    public enum State {
        /**
         * The music player is currently playing a track.
         */
        PLAYING,

        /**
         * The Track has been added to the Queue.
         */
        QUEUE_ADD,

        /**
         * The Queue is empty.
         */
        QUEUE_EMPTY,

        /**
         * The music player is currently paused.
         */
        PAUSED,

        /**
         * The music player is currently stopped.
         */
        STOPPED,

        /**
         * The music player has finished playing a track.
         */
        FINISHED,

        /**
         * The music player has encountered an error.
         */
        ERROR
    }
}
