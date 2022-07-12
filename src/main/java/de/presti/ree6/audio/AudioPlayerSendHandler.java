package de.presti.ree6.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

import java.nio.ByteBuffer;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an AudioSendHandler for JDA. As JDA calls canProvide
 * before every call to provide20MsAudio(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide20MsAudio().
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    /**
     * @param audioPlayer Audio player to wrap.
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
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
     * Check if the Audio-Player is playing any Music!
     * @param guild the Guild.
     * @return true, if yes.
     */
    public boolean isMusicPlaying(Guild guild) {
        return guild.getSelfMember().getVoiceState() != null && guild.getSelfMember().getVoiceState().inAudioChannel() &&
                audioPlayer.getPlayingTrack() != null && !audioPlayer.isPaused();
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