package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Data;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class Stop extends Command {

    public Stop() {
        super("stop", "Stop the song!", Category.MUSIC);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()) != null) {
            Main.getInstance().getMusicWorker().getGuildAudioPlayer(commandEvent.getGuild()).scheduler.stopAll(commandEvent.getInteractionHook());
        } else {
            EmbedBuilder em = new EmbedBuilder();

            em.setAuthor(BotInfo.botInstance.getSelfUser().getName(), Data.WEBSITE,
                    BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setTitle("Music Player!");
            em.setThumbnail(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            em.setColor(Color.RED);
            em.setDescription("Im not playing any Music!");

            sendMessage(em, 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}