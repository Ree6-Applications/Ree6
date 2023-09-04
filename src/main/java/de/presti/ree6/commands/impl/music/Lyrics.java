package de.presti.ree6.commands.impl.music;

import com.jagrosh.jlyrics.LyricsClient;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * Get the Lyrics of a Song.
 */
@Command(name = "lyrics", description = "command.description.lyrics", category = Category.MUSIC)
public class Lyrics implements ICommand {

    /**
     * Lyrics Client.
     * Used to get the Lyrics of the current Song.
     */
    private final LyricsClient client = new LyricsClient();

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            commandEvent.reply(commandEvent.getResource("message.music.notConnected"));
            return;
        }

        if (Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild()) && Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).isMusicPlaying()) {

            GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
            String title = guildMusicManager.getPlayer().getPlayingTrack().getInfo().title.contains("(") && guildMusicManager.getPlayer().getPlayingTrack().getInfo().title.contains(")") ?
                    guildMusicManager.getPlayer().getPlayingTrack().getInfo().title.split("\\(")[0] :
                    guildMusicManager.getPlayer().getPlayingTrack().getInfo().title;

            client.getLyrics(title).thenAccept(lyrics -> {

                if (lyrics == null) {
                    commandEvent.reply(new EmbedBuilder().setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.getWebsite(),
                                    commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl()).setTitle(commandEvent.getResource("label.musicPlayer"))
                            .setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl()).setColor(Color.RED)
                            .setDescription(commandEvent.getResource("message.music.lyrics.notFound", "``" + FormatUtil.filter(title) + "``"))
                            .setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl()).build(), 5);
                    return;
                }

                EmbedBuilder eb = new EmbedBuilder()
                        .setAuthor(lyrics.getAuthor())
                        .setTitle(lyrics.getTitle(), lyrics.getURL());
                if (lyrics.getContent().length() > 15000) {
                    commandEvent.reply(commandEvent.getResource("message.music.lyrics.foundUnlikely", "`" + FormatUtil.filter(title) + "`", lyrics.getURL()));
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
                        commandEvent.reply(eb.setDescription(content.substring(0, index).trim()).build());
                        content = content.substring(index).trim();
                        eb.setAuthor(null).setTitle(null, null);
                        eb.setFooter(commandEvent.getGuild().getName() + " - " + Data.getAdvertisement(), commandEvent.getGuild().getIconUrl());
                    }

                    commandEvent.reply(eb.setDescription(content).build());
                } else {
                    commandEvent.reply(eb.setDescription(lyrics.getContent()).build());
                }
            });
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
