package de.presti.ree6.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.CommandManager;
import de.presti.ree6.main.Data;
import de.presti.ree6.utils.ArrayUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MusikWorker {

    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;

    public MusikWorker() {
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
                em.setDescription("A Song with the URL " + trackUrl + " couldnt be found!");
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);


            }

            @Override
            public void loadFailed(FriendlyException exception) {

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);

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
                em.setDescription("The Song " + track.getInfo().title + " has been added to the Queue!");
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);


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
                em.setDescription("The Song " + firstTrack.getInfo().title
                        + " has been added to the Queue! (The first Song of the Playlist: " + playlist.getName() + ")");
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);


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
                em.setDescription("A Song with the URL " + trackUrl + " couldnt be found!");
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);


            }

            @Override
            public void loadFailed(FriendlyException exception) {

                EmbedBuilder em = new EmbedBuilder();
                em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website, BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setTitle("Music Player!");
                em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                em.setColor(Color.GREEN);
                em.setDescription("Error while playing: " + exception.getMessage());
                em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

                CommandManager.sendMessage(em, 5, channel);

            }
        });
    }

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        if (!ArrayUtil.botjoin.containsKey(guild)) {
            connectToFirstVoiceChannel(guild.getAudioManager());
        } else {
            connectToMemberVoiceChannel(guild.getAudioManager(), ArrayUtil.botjoin.get(guild));
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
        em.setFooter(channel.getGuild().getName(), channel.getGuild().getIconUrl());

        CommandManager.sendMessage(em, 5, channel);

        musicManager.scheduler.nextTrack(channel);
    }

    @SuppressWarnings("deprecation")
    public void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void connectToMemberVoiceChannel(AudioManager audioManager, Member m) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getMembers().contains(m)) {
                    audioManager.openAudioConnection(voiceChannel);
                    ArrayUtil.botjoin.remove(m.getGuild());
                    break;
                }
            }
        }
    }

    public boolean isConnected(Guild g) {
        for (VoiceChannel vc : g.getVoiceChannels()) {
            if (vc.getMembers().contains(g.getMemberById(BotInfo.botInstance.getSelfUser().getId()))) {
                return true;
            }
        }
        return false;
    }

    public void disconnect(Guild g) {
        g.getAudioManager().closeAudioConnection();
    }

    public boolean isConnectedMember(Member m, Guild g) {
        for (VoiceChannel vc : g.getVoiceChannels()) {
            if (vc.getMembers().contains(m)) {
                return true;
            }
        }
        return false;
    }
}
