package de.presti.ree6.commands.impl.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;

public class Songinfo extends Command {

    public Songinfo() {
        super("songinfo", "Get the currently playing Track!", Category.MUSIC);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        EmbedBuilder em = new EmbedBuilder();

        StringBuilder end = new StringBuilder("```");

        for (AudioTrack track : Main.musikWorker.getGuildAudioPlayer(m.getGuild()).scheduler.getQueue()) {
            end.append("\n").append(track.getInfo().title);
        }

        end.append("```");

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.musikWorker.getGuildAudioPlayer(m.getGuild()).player.getPlayingTrack() == null ? "No Song is being played right now!" :  "**Song:** ```" + Main.musikWorker.getGuildAudioPlayer(m.getGuild()).player.getPlayingTrack().getInfo().title + " by " + Main.musikWorker.getGuildAudioPlayer(m.getGuild()).player.getPlayingTrack().getInfo().author + "```");
        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());

        sendMessage(em, 5, m, hook);
    }
}
