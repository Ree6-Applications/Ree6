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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusicWorker {

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


    public void loadAndPlaySilence(TextChannel channel, String trackUrl) {
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
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("A Song with the URL ``" + trackUrl + "`` couldn't be found!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel, null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);
            }
        });
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        musicManager.scheduler.thechannel = channel;

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Song ``" + track.getInfo().title + "`` has been added to the Queue!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("The Song ``" + firstTrack.getInfo().title
                        + "`` has been added to the Queue! (The first Song of the Playlist: " + playlist.getName() + ")");
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);

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
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("A Song with the URL ``" + trackUrl + "`` couldn't be found!");
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

                Main.commandManager.sendMessage(em, 5, channel);
            }
        });
    }

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        if (ArrayUtil.botJoin.containsKey(guild)) {
            connectToMemberVoiceChannel(guild.getAudioManager(), ArrayUtil.botJoin.get(guild));
        }

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        EmbedBuilder em = new EmbedBuilder();
        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("Skipping to the next Song!");
        em.setFooter(channel.getGuild().getName() + " - " + Data.advertisement, channel.getGuild().getIconUrl());

        Main.commandManager.sendMessage(em, 5, channel);

        musicManager.scheduler.nextTrack(channel);
    }

    @SuppressWarnings("deprecation")
    public void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannels().get(0));
        }
    }

    @SuppressWarnings("deprecation")
    public void connectToMemberVoiceChannel(AudioManager audioManager, Member m) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            audioManager.openAudioConnection(Objects.requireNonNull(m.getVoiceState()).getChannel());
            ArrayUtil.botJoin.remove(m.getGuild());

            if ((audioManager.isConnected() || (audioManager.getGuild().getSelfMember().getVoiceState() != null &&
                    audioManager.getGuild().getSelfMember().getVoiceState().inVoiceChannel())) && !audioManager.isSelfDeafened()) {

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
        return m != null && m.getVoiceState() != null && m.getVoiceState().inVoiceChannel();
    }
}
