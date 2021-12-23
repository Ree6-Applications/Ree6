package de.presti.ree6.commands.impl.music;

import com.jagrosh.jlyrics.LyricsClient;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.music.AudioPlayerSendHandler;
import de.presti.ree6.music.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Lyrics extends Command {

    public Lyrics() {
        super("lyrics", "Shows you the Lyrics of the current Song.", Category.MUSIC);
    }

    private final LyricsClient client = new LyricsClient();

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook interactionHook) {
        AudioPlayerSendHandler sendingHandler = (AudioPlayerSendHandler) m.getGuild().getAudioManager().getSendingHandler();

        if (sendingHandler.isMusicPlaying(m.getGuild())) {
            GuildMusicManager gmm = Main.musicWorker.getGuildAudioPlayer(m.getGuild());
            String title = gmm.player.getPlayingTrack().getInfo().title.replace("(Official Music Video)", "").replace("(Official Video)", "")
                    .replace("(Music Video)", "").replace("(Official Music)", "").replace("(Official Lyrics)", "")
                    .replace("(Lyrics)", "").replace("(Audio)", "").replace("(Official Audio)", "");

            client.getLyrics(title).thenAccept(lyrics -> {

                if (lyrics == null) {
                    m.sendMessageEmbeds(new EmbedBuilder().setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                                    BotInfo.botInstance.getSelfUser().getAvatarUrl()).setTitle("Music Player!")
                            .setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl()).setColor(Color.RED)
                            .setDescription("Couldn't find the Lyrics for ``" + title + "``.")
                            .setFooter(m.getGuild().getName() + " - " + Data.ADVERTISEMENT, m.getGuild().getIconUrl()).build())
                            .delay(5, TimeUnit.SECONDS).flatMap(Message::delete).queue();
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder()
                        .setAuthor(lyrics.getAuthor())
                        .setTitle(lyrics.getTitle(), lyrics.getURL());
                if(lyrics.getContent().length()>15000)
                {
                    sendMessage("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL(), m, null);
                }
                else if(lyrics.getContent().length()>2000)
                {
                    String content = lyrics.getContent().trim();
                    while(content.length() > 2000)
                    {
                        int index = content.lastIndexOf("\n\n", 2000);
                        if(index == -1)
                            index = content.lastIndexOf("\n", 2000);
                        if(index == -1)
                            index = content.lastIndexOf(" ", 2000);
                        if(index == -1)
                            index = 2000;
                        m.sendMessageEmbeds(eb.setDescription(content.substring(0, index).trim()).build()).queue();
                        content = content.substring(index).trim();
                        eb.setAuthor(null).setTitle(null, null);
                        eb.setFooter(m.getGuild().getName() + " - " + Data.ADVERTISEMENT, m.getGuild().getIconUrl());
                    }

                    m.sendMessageEmbeds(eb.setDescription(content).build()).queue();
                } else {
                    m.sendMessageEmbeds(eb.setDescription(lyrics.getContent()).build()).queue();
                }
            });
        }
    }
}
