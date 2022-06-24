package de.presti.ree6.commands.impl.mod;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Sends a link to the Webinterface.
 */
@Command(name = "webinterface", description = "Shows the url to the Webinterface, which needs Discord OAuth2 access.", category = Category.MOD)
public class Webinterface implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {

        if (commandEvent.getMember().hasPermission(Permission.ADMINISTRATOR) && commandEvent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            Main.getInstance().getCommandManager().sendMessage("Please visit <https://cp.ree6.de>", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        } else {
            Main.getInstance().getCommandManager().sendMessage("You can't use this Command you need the following Permissions: Administrator and Manage Server", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[] { "web", "interface" };
    }
}