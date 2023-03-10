package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.presti.ree6.audio.music.GuildMusicManager;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;

@Command(name = "musicpanel", description = "command.description.musicpanel", category = Category.MUSIC)

public class MusicPanel implements ICommand {
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", Permission.MANAGE_SERVER.name()), 5);
            return;
        }

        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        GuildMusicManager guildMusicManager = Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild());

        AudioTrackInfo audioTrackInfo =
                guildMusicManager != null && guildMusicManager.getPlayer().getPlayingTrack() != null ?
                        guildMusicManager.getPlayer().getPlayingTrack().getInfo() : null;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.MAGENTA)
                .setImage("")
                .setTitle("**" + (audioTrackInfo != null ? commandEvent.getResource("message.music.songInfoSlim", audioTrackInfo.title, audioTrackInfo.author)
                        : commandEvent.getResource("message.music.notPlaying")) + "**")
                .setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        messageCreateBuilder.setEmbeds(embedBuilder.build());

        // TODO:: use icons
        messageCreateBuilder.addActionRow(Button.of(ButtonStyle.SECONDARY, "re_music_play", "Play"),
                Button.of(ButtonStyle.SECONDARY, "re_music_stop", "Stop"),
                Button.of(ButtonStyle.SECONDARY, "re_music_skip", "Skip"),
                Button.of(ButtonStyle.SECONDARY, "re_music_loop", "Loop"),
                Button.of(ButtonStyle.SECONDARY, "re_music_shuffle", "Shuffle"),
                Button.success("re_music_add", commandEvent.getResource("label.queueAdd")));

        commandEvent.reply(messageCreateBuilder.build());
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
