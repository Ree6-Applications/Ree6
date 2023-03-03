package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// TODO:: translate

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 */
@Slf4j
@SuppressWarnings("ALL")
public class TrackScheduler extends AudioEventAdapter {
    /**
     * The {@link AudioPlayer} from the current Channel.
     */
    private final AudioPlayer player;

    /**
     * The {@link GuildMusicManager} related to this TrackScheduler.
     */
    private final GuildMusicManager guildMusicManager;

    /**
     * The Song-Queue.
     */
    private final BlockingQueue<AudioTrack> queue;

    /**
     * The Channel where the command had been executed.
     */
    MessageChannelUnion channel;

    /**
     * If the bot should loop the current track.
     */
    boolean loop = false;

    /**
     * Constructs a TrackScheduler.
     *
     * @param guildMusicManager The GuildMusicManager related to this TrackScheduler.
     * @param player            The audio player this scheduler uses
     */
    public TrackScheduler(GuildMusicManager guildMusicManager, AudioPlayer player) {
        this.guildMusicManager = guildMusicManager;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        queue(track, false);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     * @param force If the track should be played right away.
     */
    public void queue(AudioTrack track, boolean force) {
        // Calling startTrack with the noInterrupt set to true will start the track only
        // if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the
        // player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, !force)) {
            if (!queue.offer(track)) {
                log.error("[TrackScheduler] Couldn't offer a new Track!");
            }
        }
    }


    /**
     * Toggle the loop.
     *
     * @return if the loop is activ or not.
     */
    public boolean loop() {
        setLoop(!loop);
        return loop;
    }

    /**
     * Check if loop is activ or not.
     *
     * @return true, if it is activ | false, if not.
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Toggle the loop.
     *
     * @param loop should the loop we activ or not?
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * Shuffle the current playlsist.
     */
    public void shuffle() {
        ArrayList<AudioTrack> audioTrackArrayList = new ArrayList<>();
        audioTrackArrayList.addAll(queue.stream().toList());
        Collections.shuffle(audioTrackArrayList);
        queue.clear();
        queue.addAll(audioTrackArrayList);
    }

    /**
     * Get the Text-Channel the commands have been performed from.
     *
     * @return the {@link TextChannel}.
     */
    public MessageChannelUnion getChannel() {
        return channel;
    }

    /**
     * Change the Text-Channel where the commands have been performed from.
     *
     * @param channel the {@link TextChannel}.
     */
    public void setChannel(MessageChannelUnion channel) {
        this.channel = channel;
    }

    /**
     * Get the used Audio-Player.
     *
     * @return the {@link AudioPlayer}.
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Get the current Queue.
     *
     * @return the {@link BlockingQueue<AudioTrack>}.
     */
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    /**
     * Clear the current Queue.
     */
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Get the next Track from the Queue.
     *
     * @param textChannel the Text-Channel where the command have been performed from.
     * @param silent      should the bot send a message or not?
     */
    public void nextTrack(MessageChannelUnion textChannel, boolean silent) {
        if (loop) {
            player.startTrack(player.getPlayingTrack(), true);
            return;
        }

        setChannel(textChannel);

        AudioTrack track = queue.poll();

        if (track != null) {
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Next Song!\nSong: ``" + FormatUtil.filter(track.getInfo().title) + "``")
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            player.startTrack(track, false);
        } else {
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.RED)
                        .setDescription("There is no new Song!")
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
        }
    }

    /**
     * Get the next Track from the Queue.
     *
     * @param textChannel the Text-Channel where the command have been performed from.
     * @param position    the position of the track it should skip to. (relative to the current track)
     * @param silent      should the bot send a message or not?
     */
    public void nextTrack(MessageChannelUnion textChannel, int position, boolean silent) {
        if (loop) {
            player.startTrack(player.getPlayingTrack().makeClone(), true);
            return;
        }

        setChannel(textChannel);

        AudioTrack track = null;


        if (!queue.isEmpty() && queue.size() >= (position + 1)) {
            for (int currentPosition = 0; currentPosition < position; currentPosition++) {
                track = queue.poll();
            }
        }

        if (track != null) {
            // TODO:: Really stupid workaround for https://github.com/Ree6-Applications/Ree6/issues/299! This should be rechecked later if it even worked.
            if (track == player.getPlayingTrack()) track = player.getPlayingTrack().makeClone();
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Next Song!\nSong: ``" + FormatUtil.filter(track.getInfo().title) + "``")
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            player.startTrack(track, false);
        } else {
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.RED)
                        .setDescription("There is no new Song!")
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
        }
    }

    /**
     * Seek to a specific position in the current track.
     *
     * @param channel             the Text-Channel where the command have been performed from.
     * @param seekAmountInSeconds the amount of seconds to seek to.
     */
    public void seekPosition(MessageChannelUnion channel, int seekAmountInSeconds) {
        if (player.getPlayingTrack() == null) {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                    .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                    .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription("There is no Song playing!")
                    .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            return;
        }

        if (player.getPlayingTrack().getPosition() / 1000 + seekAmountInSeconds > player.getPlayingTrack().getDuration() / 1000) {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                    .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                    .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription("You can't seek to a position that is longer than the song!")
                    .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            return;
        }

        long skipPosition = player.getPlayingTrack().getPosition() + ((long) seekAmountInSeconds * 1000);
        if (skipPosition < 0) {
            player.getPlayingTrack().setPosition(0L);
        } else {
            player.getPlayingTrack().setPosition(skipPosition);
        }

        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setColor(Color.GREEN)
                .setDescription("Seeked to ``" + FormatUtil.formatTime(player.getPlayingTrack().getPosition()) + "``!")
                .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
    }

    /**
     * Override the default onTrackEnd method, to inform user about the next song or problems.
     *
     * @param player    the current AudioPlayer.
     * @param track     the current Track.
     * @param endReason the current end Reason.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or
        // LOAD_FAILED)

        // Check if loop is toggled
        if (loop) {
            // Check if the song just ended and if so start again.
            if (endReason == AudioTrackEndReason.FINISHED) {
                AudioTrack loopTrack = track.makeClone();

                if (loopTrack != null && player != null) {
                    player.startTrack(loopTrack, false);
                } else {
                    if (getChannel() != null) {
                        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                                .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                                .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                                .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                                .setColor(Color.RED)
                                .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: Track is not existing!")
                                .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
                    }

                    nextTrack(getChannel(), track.getSourceManager() instanceof LocalAudioSourceManager);
                }
                // If there was a error cancel the loop, and go to the next song in the playlist.
            } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                if (getChannel() != null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                            .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: " + endReason.name())
                            .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
                }

                nextTrack(getChannel(), track.getSourceManager() instanceof LocalAudioSourceManager);
            }
        } else {
            if (endReason == AudioTrackEndReason.CLEANUP) {
                return;
            }

            // Check if a new song shoud be played or not.
            if (endReason.mayStartNext) {
                // check if there was an error on the current song if so inform user.
                if (endReason == AudioTrackEndReason.LOAD_FAILED && getChannel() != null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                            .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: " + endReason.name())
                            .setFooter(guildMusicManager.getGuild().getName() + " - " + Data.ADVERTISEMENT, guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
                }
                nextTrack(getChannel(), track.getSourceManager() instanceof LocalAudioSourceManager);
            }
        }
    }

    /**
     * Stop every Song-Player on the Guild.
     *
     * @param guild           The Guild entity.
     * @param interactionHook the InteractionHook, incase it is a SlashCommand.
     */
    public void stopAll(InteractionHook interactionHook) {
        EmbedBuilder em = new EmbedBuilder();
        if (Main.getInstance().getMusicWorker().isConnected(guildMusicManager.getGuild()) || getPlayer().getPlayingTrack() != null) {

            guildMusicManager.getPlayer().stopTrack();

            guildMusicManager.getScheduler().clearQueue();

            Main.getInstance().getMusicWorker().disconnect(guildMusicManager.getGuild());
            em.setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"));
            em.setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Successfully stopped the Player!");
        } else {
            em.setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), Data.WEBSITE, guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"));
            em.setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");
        }

        em.setFooter(Data.ADVERTISEMENT);
        Main.getInstance().getCommandManager().sendMessage(em, 5, getChannel(), interactionHook);
    }
}