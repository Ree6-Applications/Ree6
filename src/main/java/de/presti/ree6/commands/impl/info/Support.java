package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Support extends Command {

    public Support() {
        super("support", "Get a Invite Link to the Support Server!", Category.INFO, new String[] { "sup", "supp" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        sendMessage("<https://support.ree6.de/>", m, hook);
    }
}
