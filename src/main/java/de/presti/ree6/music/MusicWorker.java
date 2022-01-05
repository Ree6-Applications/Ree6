package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.ArrayUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusicWorker {

    // TODO rework.

    public final AudioPlayerManager playerManager;
    public final Map<Long, GuildMusicManager> musicManagers;

    public MusicWorker() {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(guild, playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


    public void loadAndPlaySilence(TextChannel channel, String trackUrl, InteractionHook interactionHook) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        musicManager.scheduler.thechannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                play(channel.getGuild(), musicManager, firstTrack);

                if (playlist.getTracks().size() > 1) {
                    for (AudioTrack tracks : playlist.getTracks()) {
                        if (tracks != firstTrack) {
                            musicManager.scheduler.queue(tracks);
                        }
                    }
                }
            }

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

    public void loadAndPlay(final TextChannel channel, final String trackUrl, InteractionHook interactionHook) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        musicManager.scheduler.thechannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
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

                play(channel.getGuild(), musicManager, track);
            }

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
                em.setDescription("The Song ``" + firstTrack.getInfo().title
                        + "`` has been added to the Queue! (The first Song of the Playlist: " + playlist.getName() + ")");
                em.setFooter(channel.getGuild().getName() + " - " + Data.ADVERTISEMENT, channel.getGuild().getIconUrl());

                Main.getInstance().getCommandManager().sendMessage(em, 5, channel, interactionHook);

                play(channel.getGuild(), musicManager, firstTrack);

                if (playlist.getTracks().size() > 1) {
                    for (AudioTrack tracks : playlist.getTracks()) {
                        if (tracks != firstTrack) {
                            musicManager.scheduler.queue(tracks);
                        }
                    }
                }
            }

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

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        if (ArrayUtil.botJoin.containsKey(guild)) {
            connectToMemberVoiceChannel(guild.getAudioManager(), ArrayUtil.botJoin.get(guild));
        }

        musicManager.scheduler.queue(track);
    }

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

    public boolean isAttemptingToConnect(ConnectionStatus connectionStatus) {
        return connectionStatus == ConnectionStatus.CONNECTING_ATTEMPTING_UDP_DISCOVERY ||
                connectionStatus == ConnectionStatus.CONNECTING_AWAITING_AUTHENTICATION ||
                connectionStatus == ConnectionStatus.CONNECTING_AWAITING_WEBSOCKET_CONNECT ||
                connectionStatus == ConnectionStatus.CONNECTING_AWAITING_READY ||
                connectionStatus == ConnectionStatus.CONNECTING_AWAITING_ENDPOINT;
    }

    public void connectToMemberVoiceChannel(AudioManager audioManager, Member m) {
        if (!audioManager.isConnected() && !isAttemptingToConnect(audioManager.getConnectionStatus())) {
            audioManager.openAudioConnection(Objects.requireNonNull(m.getVoiceState()).getChannel());
            ArrayUtil.botJoin.remove(m.getGuild());

            if ((audioManager.isConnected() || (audioManager.getGuild().getSelfMember().getVoiceState() != null &&
                    audioManager.getGuild().getSelfMember().getVoiceState().inAudioChannel())) && !audioManager.isSelfDeafened()) {

                if (audioManager.getGuild().getSelfMember().hasPermission(Permission.VOICE_DEAF_OTHERS)) {
                    audioManager.getGuild().getSelfMember().deafen(true).queue();
                }
            }
        }
    }

    public boolean isConnected(Guild g) {
        return g != null && (g.getAudioManager().isConnected() || isConnectedMember(g.getSelfMember()));
    }

    public void disconnect(Guild g) {
        g.getAudioManager().closeAudioConnection();

    }

    public boolean isConnectedMember(Member m) {
        return m != null && m.getVoiceState() != null && m.getVoiceState().inAudioChannel();
    }
}
