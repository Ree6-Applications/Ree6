package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 */
@SuppressWarnings("ALL")
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    TextChannel textChannel;
    boolean loop = false;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only
        // if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the
        // player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            if (!queue.offer(track)) {
                Main.getInstance().getLogger().error("[TrackScheduler] Couldn't offer a new Track!");
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
        queue.addAll(audioTrackArrayList);
        Collections.shuffle(audioTrackArrayList);
        queue.clear();
        queue.addAll(audioTrackArrayList);
    }

    /**
     * Get the Text-Channel the commands have been performed from.
     *
     * @return the {@link TextChannel}.
     */
    public TextChannel getTextChannel() {
        return textChannel;
    }

    /**
     * Change the Text-Channel where the commands have been performed from.
     *
     * @param textChannel the {@link TextChannel}.
     */
    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
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
     */
    public void nextTrack(TextChannel textChannel) {
        // Start the next track, regardless of if something is already playing or not.
        // In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the
        // player.

        setTextChannel(textChannel);

        AudioTrack track = queue.poll();

        if (track != null) {
            if (getTextChannel() != null) {
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl())
                        .setTitle("Music Player!")
                        .setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Next Song!\nSong: ``" + FormatUtil.filter(track.getInfo().title) + "``")
                        .setFooter(getTextChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, getTextChannel().getGuild().getIconUrl()), 5, getTextChannel());
            }
            player.startTrack(track, false);
        }
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
                    if (getTextChannel() != null) {
                        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                                .setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl())
                                .setTitle("Music Player!")
                                .setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl())
                                .setColor(Color.RED)
                                .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: Track is not existing!")
                                .setFooter(getTextChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, getTextChannel().getGuild().getIconUrl()), 5, getTextChannel());
                    }

                    nextTrack(getTextChannel());
                }
                // If there was a error cancel the loop, and go to the next song in the playlist.
            } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                if (getTextChannel() != null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl())
                            .setTitle("Music Player!")
                            .setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: " + endReason.name())
                            .setFooter(getTextChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, getTextChannel().getGuild().getIconUrl()), 5, getTextChannel());
                }

                nextTrack(getTextChannel());
            }
        } else {
            // Check if a new song shoud be played or not.
            if (endReason.mayStartNext) {
                // check if there was an error on the current song if so inform user.
                if (endReason == AudioTrackEndReason.LOAD_FAILED && getTextChannel() != null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl())
                            .setTitle("Music Player!")
                            .setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription("Error while playing: ``" + FormatUtil.filter(track.getInfo().title) + "``\nError: " + endReason.name())
                            .setFooter(getTextChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, getTextChannel().getGuild().getIconUrl()), 5, getTextChannel());
                }
                nextTrack(getTextChannel());
            }
        }
    }

    /**
     * Stop every Song-Player on the Guild.
     *
     * @param guild           The Guild entity.
     * @param interactionHook the InteractionHook, incase it is a SlashCommand.
     */
    public void stopAll(Guild guild, InteractionHook interactionHook) {
        EmbedBuilder em = new EmbedBuilder();
        if (Main.getInstance().getMusicWorker().isConnected(getTextChannel().getGuild()) || getPlayer().getPlayingTrack() != null) {

            GuildMusicManager gmm = Main.getInstance().getMusicWorker().getGuildAudioPlayer(guild);

            gmm.player.stopTrack();

            gmm.scheduler.clearQueue();

            Main.getInstance().getMusicWorker().disconnect(guild);
            em.setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Successfully stopped the Player!");
        } else {
            em.setAuthor(textChannel.getJDA().getSelfUser().getName(), Data.WEBSITE, textChannel.getJDA().getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(textChannel.getJDA().getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");
        }

        em.setFooter(Data.ADVERTISEMENT);
        Main.getInstance().getCommandManager().sendMessage(em, 5, getTextChannel(), interactionHook);
    }
}