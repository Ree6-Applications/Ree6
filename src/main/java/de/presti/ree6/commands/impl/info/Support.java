package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Support extends Command {

    public Support() {
        super("support", "Get a Invite Link to the Support Server!", Category.INFO);
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m) {
        sendMessage("<https://support.ree6.de/>", m);
    }
}
