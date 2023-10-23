package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import de.presti.ree6.audio.AudioPlayerSendHandler;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
@Getter
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    private final IPlayer player;
    
    /**
     * Track scheduler for the player.
     */
    private final TrackScheduler scheduler;

    /**
     * The AudioPlayer Send-Handler.
     */
    private AudioPlayerSendHandler audioPlayerSendHandler;

    /**
     * The Guild of the Music-Manager.
     */
    private final Guild guild;

    /**
     * Creates a player and a track scheduler.
     *
     * @param guild   The Guild of the Music-Manager.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(Guild guild, AudioPlayerManager manager) {
        this.guild = guild;
        player = Data.shouldUseLavaLink() ? Main.getInstance().getLavalink().getLink(guild).getPlayer() : new LavaplayerPlayerWrapper(manager.createPlayer());
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
    }

    /**
     * Check if the Audio-Player is playing any Music!
     * @return true, if yes.
     */
    public boolean isMusicPlaying() {
        return guild.getSelfMember().getVoiceState() != null && guild.getSelfMember().getVoiceState().inAudioChannel() &&
                player.getPlayingTrack() != null && !player.isPaused();
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        if (Data.shouldUseLavaLink()) return null;

        if (audioPlayerSendHandler != null) return audioPlayerSendHandler;

        if (player instanceof LavaplayerPlayerWrapper lavaplayerPlayerWrapper) {
            return audioPlayerSendHandler = new AudioPlayerSendHandler(lavaplayerPlayerWrapper);
        }

        return null;
    }
}