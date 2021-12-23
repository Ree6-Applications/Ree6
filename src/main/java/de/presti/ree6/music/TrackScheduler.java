package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
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
    public TextChannel thechannel;
    public boolean loop = false;
    public boolean shuffle = false;

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
            queue.offer(track);
        }
    }

    public boolean loop() {
        return loop;
    }

    public TextChannel getThechannel() {
        return thechannel;
    }

    public void setThechannel(TextChannel thechannel) {
        this.thechannel = thechannel;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void clearQueue() {
        queue.clear();
    }

    public AudioTrack randomTrack(BlockingQueue<AudioTrack> list) {
        ArrayList<AudioTrack> tracks = new ArrayList<>();

        tracks.addAll(list);

        AudioTrack track = tracks.get(new Random().nextInt((tracks.size() - 1)));

        list.remove(track);
        return track;
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     *
     * @param channel
     */
    public void nextRandomTrack(TextChannel channel) {

        AudioTrack track = randomTrack(getQueue());

        if (track != null) {
            if (channel != null) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Next Song!\nSong: ``" + track.getInfo().title + "``");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);
            }
            player.startTrack(track, false);
        }
    }

    public void nextTrack(TextChannel channel) {
        // Start the next track, regardless of if something is already playing or not.
        // In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the
        // player.

        AudioTrack track = shuffle ? randomTrack(getQueue()) : queue.poll();

        if (track != null) {
            if (channel != null) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Next Song!\nSong: ``" + track.getInfo().title + "``");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);
            }
            player.startTrack(track, false);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or
        // LOAD_FAILED)

        if (loop) {
            if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                        BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.RED);
                em.setDescription("Error while playing: ``" + track.getInfo().title + "``\nError: "
                        + endReason.name());
                em.setFooter(thechannel.getGuild().getName() + " - " + Data.ADVERTISEMENT, thechannel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, thechannel);

                nextTrack(thechannel);
            } else {
                AudioTrack loopTrack = track.makeClone();

                if (loopTrack != null && player != null) {
                    player.startTrack((loopTrack), false);
                } else {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                            BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setColor(Color.RED);
                    em.setDescription("Error while playing: ``" + track.getInfo().title + "``\nError: Track is not existing!");
                    em.setFooter(thechannel.getGuild().getName() + " - " + Data.ADVERTISEMENT, thechannel.getGuild().getIconUrl());

                    Main.commandManager.sendMessage(em, 5, thechannel);

                    nextTrack(thechannel);
                }
            }
        } else if (shuffle) {
            if (endReason.mayStartNext) {
                if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                            BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setColor(Color.RED);
                    em.setDescription("Error while playing: ``" + track.getInfo().title + "``\nError: "
                            + endReason.name());
                    em.setFooter(thechannel.getGuild().getName() + " - " + Data.ADVERTISEMENT, thechannel.getGuild().getIconUrl());

                    Main.commandManager.sendMessage(em, 5, thechannel);
                }
                nextRandomTrack(thechannel);
            }
        } else {
            if (endReason.mayStartNext) {
                if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                    EmbedBuilder em = new EmbedBuilder();
                    em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                            BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setTitle("Music Player!");
                    em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                    em.setColor(Color.RED);
                    em.setDescription("Error while playing: ``" + track.getInfo().title + "``\nError: "
                            + endReason.name());
                    em.setFooter(thechannel.getGuild().getName() + " - " + Data.ADVERTISEMENT, thechannel.getGuild().getIconUrl());

                    Main.commandManager.sendMessage(em, 5, thechannel);
                }
                nextTrack(thechannel);
            }
        }
    }

    public void stopAll() {
        EmbedBuilder em = new EmbedBuilder();
        if (Main.musicWorker.isConnected(thechannel.getGuild()) || getPlayer().getPlayingTrack() != null) {

            GuildMusicManager gmm = Main.musicWorker.getGuildAudioPlayer(thechannel.getGuild());

            gmm.player.stopTrack();

            gmm.scheduler.clearQueue();

            Main.musicWorker.disconnect(thechannel.getGuild());
            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Successfully stopped the Player!");
        } else {
            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");
        }

        em.setFooter(Data.ADVERTISEMENT);
        Main.commandManager.sendMessage(em, 5, thechannel);
    }
}