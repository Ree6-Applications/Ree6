package de.presti.ree6.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an AudioSendHandler for JDA. As JDA calls canProvide
 * before every call to provide20MsAudio(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide20MsAudio().
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    /**
     * {@link AudioPlayer} that sends the Music Data.
     */
    private final LavaplayerPlayerWrapper audioPlayer;

    /**
     * Audio ByteBuffer.
     */
    private final ByteBuffer buffer;

    /**
     * Current Audio Frame.
     */
    private final MutableAudioFrame frame;

    /**
     * @param audioPlayer Audio player to wrap.
     */
    public AudioPlayerSendHandler(LavaplayerPlayerWrapper audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * Check if the AudioPlayer can provide Audio.
     * @return true if audio was provided.
     */
    @Override
    public boolean canProvide() {
        // returns true if audio was provided
        return audioPlayer.provide(frame);
    }

    /**
     * Let the AudioPlayer provide a ByteBuffer of the Audio.
     * @return the ByteBuffer.
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        // flip to make it a read buffer
        buffer.flip();
        return buffer;
    }

    /**
     * Check if it is using the Opus Audio Library.
     * @return true, if yes.
     */
    @Override
    public boolean isOpus() {
        return true;
    }
}