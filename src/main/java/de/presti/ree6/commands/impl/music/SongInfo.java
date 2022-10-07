package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.utils.data.Data;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;

/**
 * Get information about the current Song.
 */
@Command(name = "songinfo", description = "command.description.songinfo", category = Category.MUSIC)
public class SongInfo implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        EmbedBuilder em = new EmbedBuilder();

        GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
        AudioTrack audioTrack = guildMusicManager != null ? guildMusicManager.player.getPlayingTrack() : null;

        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), Data.WEBSITE,
                commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(audioTrack == null ? "No Song is being played right now!" : "**Song:** ```"
                + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + "```\n" +
                FormatUtil.getStatusEmoji(guildMusicManager.player) + FormatUtil.progressBar((double)audioTrack.getPosition()/audioTrack.getDuration()) +
                " `[" + FormatUtil.formatTime(audioTrack.getPosition()) + "/" + FormatUtil.formatTime(audioTrack.getDuration()) + "]` " +
                FormatUtil.volumeIcon(guildMusicManager.player.getVolume()));
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        Main.getInstance().getCommandManager().sendMessage(em, 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
        return new String[] { "trackinfo", "cq" };
    }
}
