package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Credits extends Command {

    public Credits() {
        super("credits", "Shows some awesome People", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        sendMessage("Here are some AWESOME people!\n<https://www.ree6.tk/secret.html>", 5, m);
    }
}
