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

public class Songlist extends Command {

    public Songlist() {
        super("songlist", "Shows you every Song in the Queue!", Category.MUSIC);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {

        EmbedBuilder em = new EmbedBuilder();
        
        StringBuilder end = new StringBuilder("```");

        for (AudioTrack track : Main.musicWorker.getGuildAudioPlayer(m.getGuild()).scheduler.getQueue()) {
            end.append("\n").append(track.getInfo().title);
        }

        end.append("```");

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription(Main.musicWorker.getGuildAudioPlayer(m.getGuild()).scheduler.getQueue().size() == 0 ? "No Song in the Queue" :  (end.length() > 4096 ? "Error (M-SL-01)" : "Songs: " + end));
        em.setFooter(m.getGuild().getName() + " - " + Data.advertisement, m.getGuild().getIconUrl());

        sendMessage(em, 5, m, hook);
    }
}