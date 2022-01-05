package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
import net.dv8tion.jda.api.Permission;

public class Webinterface extends Command {


    public Webinterface() {
        super("webinterface", "Get your Access-Link to the Webinterface", Category.MOD, new String[] { "web", "interface" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            sendMessage("Please visit <https://cp.ree6.de>", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else {
            sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}