package de.presti.ree6.audio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class MusicWorker {

    // The AudioPlayer Manager of the Bot.
    public final AudioPlayerManager playerManager;

    // All Guild Music Manager
    public final Map<Long, GuildMusicManager> musicManagers;

    /**
     * The constructor of the Music-worker.
     */
    public MusicWorker() {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());
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

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


    /**
     * Play or add a Song to the Queue without a Message.
     *
     * @param channel         the TextChannel where the command has been performed, used for errors.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook a InteractionHook if it was an SlashCommand.
     */
    public void loadAndPlaySilence(final MessageChannelUnion channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook) {
        loadAndPlay(channel, audioChannel, trackUrl, interactionHook, true);
    }

    /**
     * Play or add a Song to the Queue with a Message.
     *
     * @param channel         the TextChannel where the command has been performed.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook a InteractionHook if it was an SlashCommand.
     * @param silent          if the Bot shouldn't send a Message.
     */
    public void loadAndPlay(final MessageChannelUnion channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook, boolean silent) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.asGuildMessageChannel().getGuild());

        musicManager.getScheduler().setChannel(channel);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            /**
             * Just override the default trackLoaded with a simple play call.
             * @param track the AudioTrack that you want to play
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(channel.getJDA().getSelfUser().getName(), Data.WEBSITE, channel.getJDA().getSelfUser().getAvatarUrl())
                        .setTitle("Music Player!")
                        .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("The Song ``" + FormatUtil.filter(track.getInfo().title) + "`` has been added to the Queue!")
                        .setFooter(channel.asGuildMessageChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.asGuildMessageChannel().getGuild().getIconUrl()), 5, channel, interactionHook);

                play(audioChannel, musicManager, track);
            }

            /**
             * Just override the default playlistLoaded with a rather simple play and queue call.
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
                        .setAuthor(channel.getJDA().getSelfUser().getName(), Data.WEBSITE, channel.getJDA().getSelfUser().getAvatarUrl())
                        .setTitle("Music Player!")
                        .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("The Song ``" + FormatUtil.filter(firstTrack.getInfo().title) + "`` has been added to the Queue! (The first Song of the Playlist: " + FormatUtil.filter(playlist.getName()) + ")")
                        .setFooter(channel.asGuildMessageChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.asGuildMessageChannel().getGuild().getIconUrl()), 5, channel, interactionHook);

                play(audioChannel, musicManager, firstTrack);

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
                if (!silent)
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(channel.getJDA().getSelfUser().getName(), Data.WEBSITE, channel.getJDA().getSelfUser().getAvatarUrl())
                        .setTitle("Music Player!")
                        .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("A Song with the URL ``" + FormatUtil.filter(trackUrl) + "`` couldn't be found!")
                        .setFooter(channel.asGuildMessageChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.asGuildMessageChannel().getGuild().getIconUrl()), 5, channel, interactionHook);
            }

            /**
             * Override the default loadFailed Method to inform the user about it.
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder()
                        .setAuthor(channel.getJDA().getSelfUser().getName(), Data.WEBSITE, channel.getJDA().getSelfUser().getAvatarUrl())
                        .setTitle("Music Player!")
                        .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                        .setColor(Color.GREEN)
                        .setDescription("Error while playing: " + exception.getMessage())
                        .setFooter(channel.asGuildMessageChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.asGuildMessageChannel().getGuild().getIconUrl()), 5, channel, interactionHook);
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
        connectToAudioChannel(musicManager.getGuild().getAudioManager(), audioChannel);
        musicManager.getScheduler().queue(track);
    }

    /**
     * A method use to skip the current AudioTrack that is played.
     *
     * @param channel         the TextChannel, used to inform the user about the skip.
     * @param interactionHook the Interaction-Hook, used to replace the channel if it is a SlashCommand.
     * @param skipAmount      the amount of Tracks that should be skipped.
     */
    public void skipTrack(MessageChannelUnion channel, InteractionHook interactionHook, int skipAmount) {
        Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setAuthor(channel.getJDA().getSelfUser().getName(), Data.WEBSITE, channel.getJDA().getSelfUser().getAvatarUrl())
                .setTitle("Music Player!")
                .setThumbnail(channel.getJDA().getSelfUser().getAvatarUrl())
                .setColor(Color.GREEN)
                .setDescription("Skipping to the next Song!")
                .setFooter(channel.asGuildMessageChannel().getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.asGuildMessageChannel().getGuild().getIconUrl()), 5, channel, interactionHook);

        getGuildAudioPlayer(channel.asGuildMessageChannel().getGuild()).getScheduler().nextTrack(channel, skipAmount);
    }

    /**
     * A method used to seek to a specific position in the current AudioTrack.
     *
     * @param channel             the TextChannel, used to inform the user about the seek.
     * @param seekAmountInSeconds the amount of seconds that should be seeked.
     */
    public void seekInTrack(MessageChannelUnion channel, int seekAmountInSeconds) {
        getGuildAudioPlayer(channel.asGuildMessageChannel().getGuild()).getScheduler().seekPosition(channel, seekAmountInSeconds);
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
        if (!audioManager.isConnected() && !isAttemptingToConnect(audioManager.getConnectionStatus())) {
            audioManager.openAudioConnection(audioChannel);

            if ((audioManager.isConnected() || (audioManager.getGuild().getSelfMember().getVoiceState() != null && audioManager.getGuild().getSelfMember().getVoiceState().inAudioChannel())) && !audioManager.isSelfDeafened() && audioManager.getGuild().getSelfMember().hasPermission(Permission.VOICE_DEAF_OTHERS)) {
                audioManager.getGuild().getSelfMember().deafen(true).queue();
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
        return guild != null && (guild.getAudioManager().isConnected() || isConnectedMember(guild.getSelfMember()));
    }

    /**
     * Method use to disconnect the Bot from the channel.
     *
     * @param guild the Guild.
     */
    public void disconnect(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
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
            Main.getInstance().getCommandManager().sendMessage("Please join a Channel!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            return false;
        }

        if (commandEvent.getGuild().getSelfMember().getVoiceState() != null && commandEvent.getGuild().getSelfMember().getVoiceState().inAudioChannel() && commandEvent.getGuild().getSelfMember().getVoiceState().getChannel() != null && commandEvent.getMember().getVoiceState().getChannel() != null && !commandEvent.getGuild().getSelfMember().getVoiceState().getChannel().getId().equalsIgnoreCase(commandEvent.getMember().getVoiceState().getChannel().getId())) {
            Main.getInstance().getCommandManager().sendMessage("You have to be in the same Channel as me!", commandEvent.getChannel(), commandEvent.getInteractionHook());
            return false;
        }

        return true;
    }
}
