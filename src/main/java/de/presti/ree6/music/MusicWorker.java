package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
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

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    /**
     * Retrieve the GuildMusicManager of a Guild.
     *
     * @param guild the Guild.
     * @return the MusicManager of that Guild.
     */
    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());

        if (!musicManagers.containsKey(guildId)) {
            musicManagers.put(guildId, new GuildMusicManager(guild, playerManager));
        }

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
    public void loadAndPlaySilence(final TextChannel channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        musicManager.scheduler.textChannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            /**
             * Just override the default trackLoaded with a simple play call.
             * @param track the AudioTrack that you want to play
             */
            @Override
            public void trackLoaded(AudioTrack track) {
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
                }

                play(audioChannel, musicManager, firstTrack);

                if (playlist.getTracks().size() > 1) {
                    for (AudioTrack tracks : playlist.getTracks()) {
                        if (tracks != firstTrack) {
                            musicManager.scheduler.queue(tracks);
                        }
                    }
                }
            }

            /**
             * Override the default noMatches Method to inform the user about it.
             */
            @Override
            public void noMatches() {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("A Song with the URL ``" + trackUrl + "`` couldn't be found!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
            }

            /**
             * Override the default loadFailed Method to inform the user about it.
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
            }
        });
    }

    /**
     * Play or add a Song to the Queue with a Message.
     *
     * @param channel         the TextChannel where the command has been performed.
     * @param audioChannel    the AudioChannel for the Bot to join.
     * @param trackUrl        the Track URL.
     * @param interactionHook a InteractionHook if it was an SlashCommand.
     */
    public void loadAndPlay(final TextChannel channel, final AudioChannel audioChannel, final String trackUrl, InteractionHook interactionHook) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        musicManager.scheduler.textChannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            /**
             * Just override the default trackLoaded with a simple play call.
             * @param track the AudioTrack that you want to play
             */
            @Override
            public void trackLoaded(AudioTrack track) {

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Song ``" + track.getInfo().title + "`` has been added to the Queue!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);

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
                }

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Song ``" + firstTrack.getInfo().title + "`` has been added to the Queue! (The first Song of the Playlist: " + playlist.getName() + ")");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);

                play(audioChannel, musicManager, firstTrack);

                if (playlist.getTracks().size() > 1) {
                    for (AudioTrack tracks : playlist.getTracks()) {
                        if (tracks != firstTrack) {
                            musicManager.scheduler.queue(tracks);
                        }
                    }
                }
            }

            /**
             * Override the default noMatches Method to inform the user about it.
             */
            @Override
            public void noMatches() {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("A Song with the URL ``" + trackUrl + "`` couldn't be found!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
            }

            /**
             * Override the default loadFailed Method to inform the user about it.
             */
            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);
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
        connectToAudioChannel(musicManager.guild.getAudioManager(), audioChannel);
        musicManager.scheduler.queue(track);
    }

    /**
     * A method use to skip the current AudioTrack that is played.
     *
     * @param channel         the TextChannel, used to inform the user about the skip.
     * @param interactionHook the Interaction-Hook, used to replace the channel if it is a SlashCommand.
     */
    public void skipTrack(TextChannel channel, InteractionHook interactionHook) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        EmbedBuilder em = new EmbedBuilder();
        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("Skipping to the next Song!");
        em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);

        musicManager.scheduler.nextTrack(channel);
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

    public boolean checkInteractPermission(CommandEvent commandEvent) {
        if (commandEvent.getMember().getVoiceState() == null || !commandEvent.getMember().getVoiceState().inAudioChannel()) {
            Main.getInstance().getCommandManager().sendMessage("Please join a Channel!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return false;
        }

        if (commandEvent.getGuild().getSelfMember().getVoiceState() != null && commandEvent.getGuild().getSelfMember().getVoiceState().inAudioChannel() && commandEvent.getGuild().getSelfMember().getVoiceState().getChannel() != null && commandEvent.getMember().getVoiceState().getChannel() != null && !commandEvent.getGuild().getSelfMember().getVoiceState().getChannel().getId().equalsIgnoreCase(commandEvent.getMember().getVoiceState().getChannel().getId())) {
            Main.getInstance().getCommandManager().sendMessage("You have to be in the same Channel as me!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            return false;
        }

        return true;
    }
}
