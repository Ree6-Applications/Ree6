package de.presti.ree6.commands.impl.music;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.main.Main;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Disconnect extends Command {

    public Disconnect() {
        super("disconnect", "Disconnect the Bot!", Category.MUSIC, new String[] { "dc", "leave" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        Main.musikWorker.musicManagers.get(m.getGuild().getIdLong()).scheduler.stopAll();
    }
}
