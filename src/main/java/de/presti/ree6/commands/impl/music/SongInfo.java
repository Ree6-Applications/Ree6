package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.bot.BotConfig;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.others.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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
        if (!Main.getInstance().getMusicWorker().isConnected(commandEvent.getGuild())) {
            commandEvent.reply(commandEvent.getResource("message.music.notConnected"));
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
        EmbedBuilder em = new EmbedBuilder();

        GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());
        AudioTrack audioTrack = guildMusicManager != null ? guildMusicManager.getPlayer().getPlayingTrack() : null;

        em.setAuthor(commandEvent.getGuild().getJDA().getSelfUser().getName(), BotConfig.getWebsite(),
                commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        em.setTitle(commandEvent.getResource("label.musicPlayer"));
        em.setThumbnail(commandEvent.getGuild().getJDA().getSelfUser().getEffectiveAvatarUrl());
        if (audioTrack != null && audioTrack.getInfo().artworkUrl != null) em.setImage(audioTrack.getInfo().artworkUrl);
        em.setColor(BotConfig.getMainColor());
        em.setDescription(audioTrack == null ? commandEvent.getResource("message.music.notPlaying") :
                commandEvent.getResource("message.music.songInfo", audioTrack.getInfo().title, audioTrack.getInfo().author,
                FormatUtil.getStatusEmoji(guildMusicManager.getPlayer()) + FormatUtil.progressBar((double)audioTrack.getPosition() / audioTrack.getDuration()),
                FormatUtil.formatTime(audioTrack.getPosition()), FormatUtil.formatTime(audioTrack.getDuration()), FormatUtil.volumeIcon(guildMusicManager.getPlayer().getVolume())));
        em.setFooter(commandEvent.getGuild().getName() + " - " + BotConfig.getAdvertisement(), commandEvent.getGuild().getIconUrl());
        messageCreateBuilder.setEmbeds(em.build());
        if (audioTrack != null) messageCreateBuilder.addActionRow(Button.of(ButtonStyle.LINK, audioTrack.getInfo().uri, "Watch"));

        commandEvent.reply(messageCreateBuilder.build(), 5);
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
