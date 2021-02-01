package de.presti.ree6.commands.impl;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Data;
import de.presti.ree6.music.MusikWorker;
import de.presti.ree6.utils.ArrayUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class Play extends Command {

    public Play() {
        super("play", "Play a song!", Category.MUSIC);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        if (args.length != 1) {
            EmbedBuilder em = new EmbedBuilder();
            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.GREEN);
            em.setDescription("Usage: ree!play (YouTube Url)");
            em.setFooter(m.getGuild().getName(), m.getGuild().getIconUrl());
            sendMessage(em, 5, m);
        } else {
            if (MusikWorker.isConnectedMember(sender, m.getGuild())) {
                if (ArrayUtil.botjoin.containsKey(m.getGuild())) {
                    ArrayUtil.botjoin.remove(m.getGuild());
                }
                ArrayUtil.botjoin.put(m.getGuild(), sender);
            }
            MusikWorker.loadAndPlay(m, args[0]);
        }
        messageSelf.delete().queue();
    }
}
