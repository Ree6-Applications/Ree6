package de.presti.ree6.commands.impl.music;

import com.jagrosh.jlyrics.LyricsClient;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.music.AudioPlayerSendHandler;
import de.presti.ree6.music.GuildMusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

@Command(name = "lyrics", description = "Wanna see the Lyrics of the current Song?", category = Category.MUSIC)
public class Lyrics implements ICommand {

    private final LyricsClient client = new LyricsClient();

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            Main.getInstance().getCommandManager().sendMessage("Im not connected to any Channel, so there is nothing to see the lyrics of!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }

        AudioPlayerSendHandler sendingHandler = (AudioPlayerSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();

        if (sendingHandler != null && sendingHandler.isMusicPlaying(commandEvent.getGuild())) {

            GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
            String title = guildMusicManager.player.getPlayingTrack().getInfo().title.contains("(") && guildMusicManager.player.getPlayingTrack().getInfo().title.contains(")") ?
                    guildMusicManager.player.getPlayingTrack().getInfo().title.split("\\(")[0] :
                    guildMusicManager.player.getPlayingTrack().getInfo().title;

            client.getLyrics(title).thenAccept(lyrics -> {

                if (lyrics == null) {
                    Main.getInstance().getCommandManager().sendMessage(new EmbedBuilder().setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                                    BotInfo.botInstance.getSelfUser().getAvatarUrl()).setTitle("Music Player!")
                            .setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl()).setColor(Color.RED)
                            .setDescription("Couldn't find the Lyrics for ``" + title + "``.")
                            .setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl()), 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder()
                        .setAuthor(lyrics.getAuthor())
                        .setTitle(lyrics.getTitle(), lyrics.getURL());
                if (lyrics.getContent().length() > 15000) {
                    Main.getInstance().getCommandManager().sendMessage("Lyrics for `" + title + "` found but likely not correct: " + lyrics.getURL(), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else if (lyrics.getContent().length() > 2000) {
                    String content = lyrics.getContent().trim();
                    while (content.length() > 2000) {
                        int index = content.lastIndexOf("\n\n", 2000);
                        if (index == -1)
                            index = content.lastIndexOf("\n", 2000);
                        if (index == -1)
                            index = content.lastIndexOf(" ", 2000);
                        if (index == -1)
                            index = 2000;
                        Main.getInstance().getCommandManager().sendMessage(eb.setDescription(content.substring(0, index).trim()), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                        content = content.substring(index).trim();
                        eb.setAuthor(null).setTitle(null, null);
                        eb.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());
                    }

                    Main.getInstance().getCommandManager().sendMessage(eb.setDescription(content), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage(eb.setDescription(lyrics.getContent()), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            });
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
