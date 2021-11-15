package de.presti.ree6.commands.impl.music;

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

public class Disconnect extends Command {

    public Disconnect() {
        super("disconnect", "Disconnect the Bot!", Category.MUSIC, new String[] { "dc", "leave" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (Main.musicWorker.getGuildAudioPlayer(m.getGuild()) != null && Main.musicWorker.getGuildAudioPlayer(m.getGuild()).getSendHandler().isMusicPlaying(m.getGuild())) {
            Main.musicWorker.getGuildAudioPlayer(m.getGuild()).scheduler.stopAll();
        } else {
            EmbedBuilder em = new EmbedBuilder();

            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.website,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");

            sendMessage(em, 5, m, hook);
        }
    }
}
