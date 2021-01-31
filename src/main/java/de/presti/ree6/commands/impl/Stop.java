package de.presti.ree6.commands.impl;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.music.MusikWorker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Stop extends Command {

    public Stop() {
        super("stop", "Stop the song!");
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {

        EmbedBuilder em = new EmbedBuilder();
        
        MusikWorker.getGuildAudioPlayer(m.getGuild()).player.stopTrack();

        MusikWorker.getGuildAudioPlayer(m.getGuild()).scheduler.clearqueue();

        MusikWorker.disconnect(m.getGuild());

        em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setTitle("Music Player!");
        em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
        em.setColor(Color.GREEN);
        em.setDescription("Music Player has been stopped!");
        em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
        
        sendMessage(em, 5, m);

        messageSelf.delete().queue();
    }
}