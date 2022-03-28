package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.main.Main;

public class ReloadAddons extends CommandClass {

    public ReloadAddons() {
        super("reloadaddons", "Only for the Dev", Category.HIDDEN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if(commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            Main.getInstance().getAddonManager().reload();
        } else {
            sendMessage("The Command " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(commandEvent.getGuild().getId(), "chatprefix").getStringValue() + "reloadaddons couldn't be found!", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
        }
    }
}