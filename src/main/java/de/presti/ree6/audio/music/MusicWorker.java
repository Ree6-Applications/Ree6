package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.api.events.MusicPlayerStateChangeEvent;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.language.LanguageService;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.apis.SpotifyAPIHandler;
import de.presti.ree6.utils.apis.YouTubeAPIHandler;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.utils.others.FormatUtil;
import io.sentry.Sentry;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLink;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Wrapper class that handles most Music-related stuff.
 */
@Slf4j
public class MusicWorker {

    /**
     * The AudioPlayer Manager of the Bot.
     */
    public final AudioPlayerManager playerManager;

    /**
     * All Guild Music Managers
     */
    public final Map<Long, GuildMusicManager> musicManagers;

    /**
     * The constructor of the Music-worker.
     */
    public MusicWorker() {
        musicManagers = new HashMap<>();
        playerManager = BotConfig.shouldUseLavaLink() ? Main.getInstance().getLavalink().getAudioPlayerManager() : new DefaultAudioPlayerManager();

        // Register AudioSources if music module is active.
        // If not, then don't register them.
        // This will cause a failed resolve whenever a command is being executed.
        if (BotConfig.isModuleActive("music") && !BotConfig.shouldUseLavaLink()) {
            playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
            playerManager.registerSourceManager(new BandcampAudioSourceManager());
            playerManager.registerSourceManager(new VimeoAudioSourceManager());
            playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            playerManager.registerSourceManager(new YoutubeAudioSourceManager());
            playerManager.registerSourceManager(new HttpAudioSourceManager());
        }
    }

    /**
     * Retrieve the GuildMusicManager of a Guild.
     *
     * @param guild the Guild.
     * @return the MusicManager of that Guild.
     */
    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());

        musicManagers.putIfAbsent(guildId, new GuildMusicManager(guild, playerManager));

        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (!BotConfig.shouldUseLavaLink()) {
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        }

        return musicManager;
    }


    /**
     * Play or add a Song to the Queue without a Message.
     *
     * @param channel         the TextChannel where the command has been performed, used for errors.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook an InteractionHook if it was a SlashCommand.
     */
    public void loadAndPlaySilence(final GuildMessageChannelUnion channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook) {
        loadAndPlay(channel, audioChannel, trackUrl, interactionHook, true);
    }


    /**
     * Play or add a Song to the Queue with a Message.
     *
     * @param channel         the TextChannel where the command has been performed.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook an InteractionHook if it was a SlashCommand.
     * @param silent          if the Bot shouldn't send a Message.
     */
    public void loadAndPlay(final GuildMessageChannelUnion channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook, boolean silent) {
        loadAndPlay(channel, audioChannel, trackUrl, interactionHook, silent, false);
    }

    /**
     * Play or add a Song to the Queue with a Message.
     *
     * @param channel         the TextChannel where the command has been performed.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook an InteractionHook if it was a SlashCommand.
     * @param silent          if the Bot shouldn't send a Message.
     * @param force           if the song should be forced or not.
     */
    public void loadAndPlay(final GuildMessageChannelUnion channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook, boolean silent, boolean force) {
        loadAndPlay(channel.getGuild(), channel, audioChannel, trackUrl, interactionHook, silent, force);
    }

    /**
     * Play or add a Song to the Queue with a Message.
     *
     * @param guild           the Guild where the command has been performed.
     * @param channel         the TextChannel where the command has been performed.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook an InteractionHook if it was a SlashCommand.
     * @param silent          if the Bot shouldn't send a Message.
     * @param force           if the song should be forced or not.
     */
    public void loadAndPlay(final Guild guild, GuildMessageChannelUnion channel, AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook, boolean silent, boolean force) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);

        if (channel != null)
            musicManager.getScheduler().setChannel(channel);

        if (channel == null) {
            if (musicManager.getScheduler() != null && musicManager.getScheduler().getChannel() != null) {
                channel = musicManager.getScheduler().getChannel();
            } else if (interactionHook == null && !silent) {
                // Return since there is no way to communicate with the user.
                return;
            }
        }

        if (audioChannel == null) {
            GuildVoiceState guildVoiceState = guild.getSelfMember().getVoiceState();
            if (guildVoiceState != null && guildVoiceState.inAudioChannel()) {
                audioChannel = guildVoiceState.getChannel();
            } else {
                if (silent) return;

                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                                .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                                        guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setTitle(LanguageService.getByGuild(guild, "label.musicPlayer"))
                                .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                                .setColor(Color.RED)
                                .setDescription(LanguageService.getByGuild(guild, "message.music.notPlaying"))
                                .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl())
                        , channel, interactionHook);
                return;
            }
        }

        final AudioChannel finalAudioChannel = audioChannel;
        MessageChannel messageChannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl.trim(), new AudioLoadResultHandler() {

            /**
             * Override the default trackLoaded with a simple play call.
             * @param track the AudioTrack that you want to play
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guild, "label.musicPlayer"))
                            .setImage((track.getInfo().artworkUrl != null && track.getInfo().artworkUrl.isBlank()) ? guild.getJDA().getSelfUser().getEffectiveAvatarUrl() : track.getInfo().artworkUrl)
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(guild, "message.music.queueAdded.default", FormatUtil.filter(track.getInfo().title)))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl()), 5, messageChannel, interactionHook);

                play(finalAudioChannel, musicManager, track, force);
            }

            /**
             * Override the default playlistLoaded with a rather simple play and queue call.
             * @param playlist the AudioPlaylist.
             */
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                    playlist.getTracks().remove(0);
                }

                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guild, "label.musicPlayer"))
                            .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(guild, "message.music.queueAdded.firstOfList", FormatUtil.filter(firstTrack.getInfo().title), FormatUtil.filter(playlist.getName())))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl()), 5, messageChannel, interactionHook);

                play(finalAudioChannel, musicManager, firstTrack, force);

                if (playlist.getTracks().size() > 1) {
                    for (AudioTrack tracks : playlist.getTracks()) {
                        musicManager.getScheduler().queue(tracks);
                    }
                }
            }

            /**
             * Override the default noMatches Method to inform the user about it.
             */
            @Override
            public void noMatches() {
                Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guild, MusicPlayerStateChangeEvent.State.ERROR, null));

                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guild, "label.musicPlayer"))
                            .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(guild, "message.music.searchUrlFailed", FormatUtil.filter(trackUrl)))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl()), 5, messageChannel, interactionHook);
            }

            /**
             * Override the default loadFailed Method to inform the user about it.
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                Main.getInstance().getEventBus().post(new MusicPlayerStateChangeEvent(guild, MusicPlayerStateChangeEvent.State.ERROR, null));

                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuild(guild, "label.musicPlayer"))
                            .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuild(guild, "message.music.failedLoading", exception.getMessage()))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl()), 5, messageChannel, interactionHook);
            }
        });
    }

    /**
     * The simple Audio play method, used to Queue songs and let the Bot join the Audio-channel if not present.
     *
     * @param audioChannel the Audio-channel for the Bot.
     * @param musicManager the Music-Manager of the Guild.
     * @param track        the AudioTrack that should be played.
     */
    public void play(AudioChannel audioChannel, GuildMusicManager musicManager, AudioTrack track) {
        play(audioChannel, musicManager, track, false);
    }

    /**
     * The simple Audio play method, used to Queue songs and let the Bot join the Audio-channel if not present.
     *
     * @param audioChannel the Audio-channel for the Bot.
     * @param musicManager the Music-Manager of the Guild.
     * @param track        the AudioTrack that should be played.
     * @param force        if the Track should be played immediately.
     */
    public void play(AudioChannel audioChannel, GuildMusicManager musicManager, AudioTrack track, boolean force) {
        connectToAudioChannel(musicManager.getGuild().getAudioManager(), audioChannel);
        musicManager.getScheduler().queue(track, force);
    }

    /**
     * Play a specific song.
     *
     * @param value        The song name or url.
     * @param commandEvent The command event.
     */
    public void playSong(String value, CommandEvent commandEvent) {
        if (!commandEvent.isSlashCommand()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String argument : commandEvent.getArguments()) {
                stringBuilder.append(argument).append(" ");
            }

            playSong(stringBuilder.toString(), commandEvent.getGuild(), commandEvent.getMember(), commandEvent.getChannel(), commandEvent.getInteractionHook());
        } else {
            playSong(value, commandEvent.getGuild(), commandEvent.getMember(), commandEvent.getChannel(), commandEvent.getInteractionHook());
        }
    }

    /**
     * Play a specific song.
     *
     * @param value           The song name or url.
     * @param guild           The Guild this command has been executed on.
     * @param member          The Member that has executed this.
     * @param channel         The channel it has been executed in.
     * @param interactionHook The Interaction-Hook of the member
     */
    public void playSong(String value, Guild guild, Member member, GuildMessageChannelUnion channel, InteractionHook interactionHook) {
        Interaction interaction = interactionHook != null ? interactionHook.getInteraction() : null;
        if (FormatUtil.isUrl(value)) {
            boolean isspotify = false;
            ArrayList<String> spotiftrackinfos = null;

            if (value.contains("spotify")) {
                try {
                    isspotify = true;
                    spotiftrackinfos = SpotifyAPIHandler.getInstance().convert(value);
                } catch (Exception exception) {
                    Sentry.captureException(exception);
                    log.error("Couldn't convert Spotify URL to Track Info", exception);
                } finally {
                    if (spotiftrackinfos == null) spotiftrackinfos = new ArrayList<>();
                }
            }

            if (!isspotify) {
                if (value.contains("youtu.be/") || value.contains("youtube.com/")) {
                    Matcher matcher = YouTubeAPIHandler.getPattern().matcher(value);
                    if (matcher.find()) {
                        value = matcher.group(0);
                    }
                }

                loadAndPlay(channel, Objects.requireNonNull(member.getVoiceState()).getChannel(), value, interactionHook, false);
            } else {
                if (spotiftrackinfos.isEmpty()) {
                    EmbedBuilder em = new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(),
                                    BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuildOrInteraction(guild, interaction, "label.musicPlayer"))
                            .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuildOrInteraction(guild, interaction, "message.music.notFound", value))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl());
                    Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
                    return;
                }

                ArrayList<String> loadFailed = new ArrayList<>();

                boolean addedFirst = false;

                for (String search : spotiftrackinfos) {
                    String result = null;
                    try {
                        result = YouTubeAPIHandler.getInstance().searchYoutube(search);
                    } catch (Exception exception) {
                        log.error("Error while searching for " + search + " on YouTube", exception);
                    }

                    if (result == null) {
                        loadFailed.add(search);
                    } else {
                        if (!addedFirst) {
                            loadAndPlay(channel, Objects.requireNonNull(member.getVoiceState()).getChannel(), result, interactionHook, false);
                            addedFirst = true;
                        } else {
                            loadAndPlaySilence(channel, Objects.requireNonNull(member.getVoiceState()).getChannel(), result, interactionHook);
                        }
                    }
                }

                if (!loadFailed.isEmpty()) {
                    EmbedBuilder em = new EmbedBuilder()
                            .setAuthor(guild.getJDA().getSelfUser().getName(),
                                    BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setTitle(LanguageService.getByGuildOrInteraction(guild, interaction, "label.musicPlayer"))
                            .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                            .setColor(Color.GREEN)
                            .setDescription(LanguageService.getByGuildOrInteraction(guild, interaction, "message.music.notFoundMultiple", loadFailed.size()))
                            .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl());
                    Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
                }
            }
        } else {
            String ytResult;

            try {
                ytResult = YouTubeAPIHandler.getInstance().searchYoutube(value);
            } catch (Exception exception) {
                EmbedBuilder em = new EmbedBuilder()
                        .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuildOrInteraction(guild, interaction, "label.musicPlayer"))
                        .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setColor(Color.RED)
                        .setDescription(LanguageService.getByGuildOrInteraction(guild, interaction, "message.music.searchFailed"))
                        .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
                log.error("Error while searching for " + value + " on YouTube", exception);
                return;
            }

            if (ytResult == null) {
                EmbedBuilder em = new EmbedBuilder()
                        .setAuthor(guild.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setTitle(LanguageService.getByGuildOrInteraction(guild, interaction, "label.musicPlayer"))
                        .setThumbnail(guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setColor(Color.YELLOW)
                        .setDescription(LanguageService.getByGuildOrInteraction(guild, interaction, "message.music.notFound", FormatUtil.filter(value)))
                        .setFooter(guild.getName() + " - " + BotConfig.getAdvertisement(), guild.getIconUrl());
                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
            } else {
                loadAndPlay(channel, Objects.requireNonNull(member.getVoiceState()).getChannel(), ytResult, interactionHook, false);
            }
        }
    }

    /**
     * A method use to skip the current AudioTrack that is played.
     *
     * @param channel         the TextChannel, used to inform the user about the skip.
     * @param interactionHook the Interaction-Hook, used to replace the channel if it is a SlashCommand.
     * @param skipAmount      the amount of Tracks that should be skipped.
     */
    public void skipTrack(GuildMessageChannelUnion channel, InteractionHook interactionHook, int skipAmount) {
        skipTrack(channel, interactionHook, skipAmount, false);
    }

    /**
     * A method use to skip the current AudioTrack that is played.
     *
     * @param channel         the TextChannel, used to inform the user about the skip.
     * @param interactionHook the Interaction-Hook, used to replace the channel if it is a SlashCommand.
     * @param skipAmount      the amount of Tracks that should be skipped.
     * @param silent          if the skip should be silent or not.
     */
    public void skipTrack(GuildMessageChannelUnion channel, InteractionHook interactionHook, int skipAmount, boolean silent) {
        if (!silent) {
            Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setAuthor(channel.getJDA().getSelfUser().getName(), BotConfig.getWebsite(), channel.getJDA().getSelfUser().getAvatarUrl())
                    .setTitle(LanguageService.getByGuild(channel.getGuild(), "label.musicPlayer"))
                    .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                    .setColor(Color.GREEN)
                    .setDescription(LanguageService.getByGuild(channel.getGuild(), "message.music.skip"))
                    .setFooter(channel.getGuild().getName() + " - " + BotConfig.getAdvertisement(), channel.getGuild().getIconUrl()), 5, channel, interactionHook);
        }

        getGuildAudioPlayer(channel.getGuild()).getScheduler().nextTrack(channel, skipAmount, silent);
    }

    /**
     * A method used to seek to a specific position in the current AudioTrack.
     *
     * @param channel             the TextChannel, used to inform the user about the seek.
     * @param seekAmountInSeconds the number of seconds that should be seeked.
     */
    public void seekInTrack(GuildMessageChannelUnion channel, int seekAmountInSeconds) {
        getGuildAudioPlayer(channel.getGuild()).getScheduler().seekPosition(channel, seekAmountInSeconds);
    }

    /**
     * Check if the Bot is trying to join an Audio-Channel right now.
     *
     * @param connectionStatus the current Status of its connection.
     * @return true, if it is connecting | false, if not.
     */
    public boolean isAttemptingToConnect(ConnectionStatus connectionStatus) {
        return connectionStatus == ConnectionStatus.CONNECTING_ATTEMPTING_UDP_DISCOVERY || connectionStatus == ConnectionStatus.CONNECTING_AWAITING_AUTHENTICATION || connectionStatus == ConnectionStatus.CONNECTING_AWAITING_WEBSOCKET_CONNECT || connectionStatus == ConnectionStatus.CONNECTING_AWAITING_READY || connectionStatus == ConnectionStatus.CONNECTING_AWAITING_ENDPOINT;
    }

    /**
     * Let the Bot connect to an Audio-Channel.
     *
     * @param audioManager the Audio-Manager of the Bot.
     * @param audioChannel the Audio-Channel the Bot should join.
     */
    public void connectToAudioChannel(AudioManager audioManager, AudioChannel audioChannel) {
        if (BotConfig.shouldUseLavaLink()) {
            JdaLink link = Main.getInstance().getLavalink().getLink(audioManager.getGuild());

            if (link.getState() == Link.State.NOT_CONNECTED) {
                link.connect(audioChannel);
            }
        } else {
            if (!audioManager.isConnected() && !isAttemptingToConnect(audioManager.getConnectionStatus())) {
                audioManager.openAudioConnection(audioChannel);

                if ((audioManager.isConnected() || (audioManager.getGuild().getSelfMember().getVoiceState() != null && audioManager.getGuild().getSelfMember().getVoiceState().inAudioChannel())) && !audioManager.isSelfDeafened() && audioManager.getGuild().getSelfMember().hasPermission(Permission.VOICE_DEAF_OTHERS)) {
                    audioManager.getGuild().getSelfMember().deafen(true).queue();
                }
            }
        }
    }

    /**
     * Check if the Bot is connected to an Audio-Channel on the given Guild.
     *
     * @param guild the Guild.
     * @return true, if the Bot is connected to an Audio-channel | false, if not.
     */
    public boolean isConnected(Guild guild) {
        if (guild == null) return false;

        if (BotConfig.shouldUseLavaLink()) {
            return Main.getInstance().getLavalink().getLink(guild).getState() == Link.State.CONNECTED;
        } else {
            return guild.getAudioManager().isConnected() || isConnectedMember(guild.getSelfMember());
        }
    }

    /**
     * Method use to disconnect the Bot from the channel.
     *
     * @param guild the Guild.
     */
    public void disconnect(Guild guild) {
        if (BotConfig.shouldUseLavaLink()) {
            Main.getInstance().getLavalink().getLink(guild).destroy();
        } else {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    /**
     * Check if the Member is connected to an Audio-Channel on the Guild.
     *
     * @param member the Member.
     * @return true, if the Member is connected to an Audio-channel | false, if not.
     */
    public boolean isConnectedMember(Member member) {
        return member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel();
    }

    /**
     * Check if the user has enough permission to control the bot.
     *
     * @param commandEvent the CommandEvent.
     * @return true, if the user has enough permission | false, if not.
     */
    public boolean checkInteractPermission(CommandEvent commandEvent) {
        if (commandEvent.getMember().getVoiceState() == null || !commandEvent.getMember().getVoiceState().inAudioChannel()) {
            commandEvent.reply(commandEvent.getResource("message.default.notInVoiceChannel"));
            return false;
        }

        if (commandEvent.getGuild().getSelfMember().getVoiceState() != null && commandEvent.getGuild().getSelfMember().getVoiceState().inAudioChannel() && commandEvent.getGuild().getSelfMember().getVoiceState().getChannel() != null && commandEvent.getMember().getVoiceState().getChannel() != null && !commandEvent.getGuild().getSelfMember().getVoiceState().getChannel().getId().equalsIgnoreCase(commandEvent.getMember().getVoiceState().getChannel().getId())) {
            commandEvent.reply(commandEvent.getResource("message.default.notInSameVoiceChannel"));
            return false;
        }

        return true;
    }
}
