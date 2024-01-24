package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import de.presti.ree6.api.events.MusicPlayerStateChangeEvent;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.FormatUtil;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
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
@Slf4j
@SuppressWarnings("ALL")
public class TrackScheduler extends AudioEventAdapterWrapped {
    /**
     * The {@link IPlayer} from the current Channel.
     */
    @Getter
    private final IPlayer player;

    /**
     * The {@link GuildMusicManager} related to this TrackScheduler.
     */
    @Getter
    private final GuildMusicManager guildMusicManager;

    /**
     * The Song-Queue.
     */
    @Getter
    private final BlockingQueue<AudioTrack> queue;

    /**
     * The Channel where the command had been executed.
     */
    @Setter
    @Getter
    private GuildMessageChannelUnion channel;

    /**
     * If the bot should loop the current track.
     */
    @Setter
    @Getter
    private boolean loop = false;

    /**
     * Constructs a TrackScheduler.
     *
     * @param guildMusicManager The GuildMusicManager related to this TrackScheduler.
     * @param player            The audio player this scheduler uses
     */
    public TrackScheduler(GuildMusicManager guildMusicManager, IPlayer player) {
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
        if (!player.playTrack(track, !force)) {
            if (!queue.offer(track)) {
                log.error("[TrackScheduler] Couldn't offer a new Track!");
                Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.ERROR, track));
            } else {
                Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.QUEUE_ADD, track));
            }
        } else {
            Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.PLAYING, track));
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
    public void nextTrack(GuildMessageChannelUnion textChannel, boolean silent) {
        nextTrack(textChannel, 0, silent);
    }

    /**
     * Get the next Track from the Queue.
     *
     * @param textChannel the Text-Channel where the command have been performed from.
     * @param position    the position of the track it should skip to. (relative to the current track)
     * @param silent      should the bot send a message or not?
     */
    public void nextTrack(GuildMessageChannelUnion textChannel, int position, boolean silent) {
        if (loop && player.getPlayingTrack() != null) {
            player.playTrack(player.getPlayingTrack().makeClone(), true);
            return;
        }

        setChannel(textChannel);

        AudioTrack track = null;

        if (position > 0 && !queue.isEmpty() && queue.size() >= position) {
            for (int currentPosition = 0; currentPosition < position; currentPosition++) {
                track = queue.poll();
            }
        } else if (position == 0) {
            track = queue.poll();
        }

        if (track != null) {
            // TODO:: Really stupid workaround for https://github.com/Ree6-Applications/Ree6/issues/299! This should be rechecked later if it even worked.
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.songNext", FormatUtil.filter(track.getInfo().title)))
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());

            Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.PLAYING, track));
            player.playTrack(track.makeClone(), false);
        } else {
            if (!silent)
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                        .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                        .setColor(Color.RED)
                        .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.songQueueReachedEnd"))
                        .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());

            Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.QUEUE_EMPTY, null));
        }
    }

    /**
     * Seek to a specific position in the current track.
     *
     * @param channel             the Text-Channel where the command have been performed from.
     * @param seekAmountInSeconds the amount of seconds to seek to.
     */
    public void seekPosition(GuildMessageChannelUnion channel, int seekAmountInSeconds) {
        if (player.getPlayingTrack() == null) {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                    .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                    .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.notPlaying"))
                    .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            return;
        }

        if (player.getPlayingTrack().getPosition() / 1000 + seekAmountInSeconds > player.getPlayingTrack().getDuration() / 1000) {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                    .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                    .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                    .setColor(Color.RED)
                    .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.seek.failed"))
                    .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
            return;
        }

        long skipPosition = player.getPlayingTrack().getPosition() + ((long) seekAmountInSeconds * 1000);
        if (skipPosition < 0) {
            player.getPlayingTrack().setPosition(0L);
        } else {
            player.getPlayingTrack().setPosition(skipPosition);
        }

        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                .setColor(Color.GREEN)
                .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.seek.success", FormatUtil.formatTime(player.getPlayingTrack().getPosition())))
                .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        onTrackEnd(this.player, track, endReason);
    }

    /**
     * Used to inform user about the next song or problems.
     *
     * @param player    the current AudioPlayer.
     * @param track     the current Track.
     * @param endReason the current end Reason.
     */
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or
        // LOAD_FAILED)
        if (endReason == AudioTrackEndReason.FINISHED)
            Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.FINISHED, null));

        // Check if loop is toggled
        if (loop) {
            // Check if the song just ended and if so start again.
            if (endReason == AudioTrackEndReason.FINISHED) {
                AudioTrack loopTrack = track.makeClone();

                if (loopTrack != null && player != null) {
                    player.playTrack(loopTrack, false);
                } else {
                    Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.ERROR, null));

                    if (getChannel() != null) {
                        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                                .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                                .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                                .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.failedPlaying", FormatUtil.filter(track.getInfo().title), "Track does not exist (Internally?)"))
                                .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
                    }

                    nextTrack(getChannel(), track.getSourceManager() instanceof LocalAudioSourceManager);
                }
                // If there was a error cancel the loop, and go to the next song in the playlist.
            } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.ERROR, null));

                if (getChannel() != null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                            .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.failedPlaying", FormatUtil.filter(track.getInfo().title), endReason.name()))
                            .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
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
                    Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.ERROR, null));
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"))
                            .setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl())
                            .setColor(Color.RED)
                            .setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.failedPlaying", FormatUtil.filter(track.getInfo().title), endReason.name()))
                            .setFooter(guildMusicManager.getGuild().getName() + " - " + BotConfig.getAdvertisement(), guildMusicManager.getGuild().getIconUrl()), 5, getChannel());
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
            Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guildMusicManager.getGuild(), MusicPlayerStateChangeEvent.State.STOPPED, null));

            guildMusicManager.getPlayer().stopTrack();

            guildMusicManager.getScheduler().clearQueue();

            Main.getInstance().getMusicWorker().disconnect(guildMusicManager.getGuild());
            em.setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"));
            em.setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.stop"));
        } else {
            em.setAuthor(guildMusicManager.getGuild().getSelfMember().getEffectiveName(), BotConfig.getWebsite(), guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setTitle(LanguageService.getByGuild(guildMusicManager.getGuild(), "label.musicPlayer"));
            em.setThumbnail(guildMusicManager.getGuild().getSelfMember().getEffectiveAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription(LanguageService.getByGuild(guildMusicManager.getGuild(), "message.music.notPlaying"));
        }

        em.setFooter(BotConfig.getAdvertisement());
        Main.getInstance().getCommandManager().sendMessage(em, 5, getChannel(), interactionHook);
    }
}