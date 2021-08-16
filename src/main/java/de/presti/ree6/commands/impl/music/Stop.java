package de.presti.ree6.commands.impl.music;

import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.commands.Category;
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
        super("stop", "Stop the song!", Category.MUSIC);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {

        MusikWorker.musicManagers.get(m.getGuild().getIdLong()).scheduler.stopAll();

        deleteMessage(messageSelf);
    }
}