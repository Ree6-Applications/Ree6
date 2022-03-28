package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "reload", description = "Only meant for Developers, used to reload or load new Addons.", category = Category.HIDDEN)
public class ReloadAddons implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if(commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            Main.getInstance().getAddonManager().reload();
        } else {
            Main.getInstance().getCommandManager().sendMessage("The Command " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "reloadaddons couldn't be found!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}