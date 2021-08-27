package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Credits extends Command {

    public Credits() {
        super("credits", "See the Team behind Ree6!", Category.INFO, new String[] { "cred" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        sendMessage("Here are some AWESOME people!\n<https://www.ree6.de/team.html>", m, hook);
        sendMessage("Lead Developer : Presti | 平和#0240\nSupport Developer : xazed | xazed#5014\ndavid. | david.#3120", m, hook);
    }
}
