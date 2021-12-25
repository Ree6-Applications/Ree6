package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class Webinterface extends Command {


    public Webinterface() {
        super("webinterface", "Get your Access-Link to the Webinterface", Category.MOD, new String[] { "web", "interface" });
    }

    @Override
    public void onPerform(Member sender, Message messageSelf, String[] args, TextChannel m, InteractionHook hook) {
        if (sender.hasPermission(Permission.ADMINISTRATOR) && sender.hasPermission(Permission.MANAGE_SERVER)) {
            sendMessage("Please visit <https://cp.ree6.de>", 5, m, hook);
        } else {
            sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, m, hook);
        }
        deleteMessage(messageSelf, hook);
    }
}