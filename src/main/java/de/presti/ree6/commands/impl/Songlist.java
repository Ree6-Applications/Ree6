package de.presti.ree6.commands.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.music.MusikWorker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Songlist extends Command {

    public Songlist() {
        super("songlist", "Shows you every Song in the Queue!");
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {

        EmbedBuilder em = new EmbedBuilder();
        
        String end = "```";

        for (AudioTrack track : MusikWorker.getGuildAudioPlayer(m.getGuild()).scheduler.getQueue()) {
            end+= "\n" + track.getInfo().title;
        }

        end += "```";

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription( MusikWorker.getGuildAudioPlayer(m.getGuild()).scheduler.getQueue().size() == 0 ? "No Song in the Queue" : "Songs:" + end);
        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());

        sendMessage(em, 5, m);

        messageSelf.delete().queue();

    }
}