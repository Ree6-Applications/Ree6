package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Songlist extends CommandClass {

    public Songlist() {
        super("songlist", "Shows you every Song in the Queue!", Category.MUSIC);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        EmbedBuilder em = new EmbedBuilder();

        StringBuilder end = new StringBuilder("```");

        for (AudioTrack track : Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.getQueue()) {
            end.append("\n").append(track.getInfo().title);
        }

        end.append("```");

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE, BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.getQueue().isEmpty() ? "No Song in the Queue" : (end.length() > 4096 ? "Error (M-SL-01)" : "Songs: " + end));
        em.setFooter(commandEvent.getGuild().getName() + " - " + Data.ADVERTISEMENT, commandEvent.getGuild().getIconUrl());

        sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}