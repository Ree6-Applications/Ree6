package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import de.presti.ree6.audio.AudioPlayerSendHandler;
import net.dv8tion.jda.api.entities.Guild;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    private final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    private final TrackScheduler scheduler;

    private AudioPlayerSendHandler audioPlayerSendHandler;

    /**
     * The Guild of the Music-Manager.
     */
    private final Guild guild;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(Guild guild, AudioPlayerManager manager) {
        this.guild = guild;
        player = manager.createPlayer();
        scheduler = new TrackScheduler(this, player);
        player.addListener(scheduler);
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        if (audioPlayerSendHandler != null) return audioPlayerSendHandler;

        return audioPlayerSendHandler = new AudioPlayerSendHandler(player);
    }

    /**
     * @return AudioPlayer for the Guild.
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * @return {@link TrackScheduler} for the Guild.
     */
    public TrackScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Retrieve the Guild of the Music-Manager.
     *
     * @return The Guild of the Music-Manager.
     */
    public Guild getGuild() {
        return guild;
    }
}