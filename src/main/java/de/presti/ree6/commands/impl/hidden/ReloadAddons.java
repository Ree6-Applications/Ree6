package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.addons.Addon;
import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "addon", description = "Only meant for Developers, used to reload or load new Addons.", category = Category.HIDDEN)
public class ReloadAddons implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        if(commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            if (commandEvent.getArguments().length == 0) {
                StringBuilder stringBuilder = new StringBuilder("```");
                for (Addon addon : Main.getInstance().getAddonManager().addons) {
                    stringBuilder.append(addon.getName()).append("v").append(addon.getVersion())
                            .append(" ").append("for").append(" ").append("by").append(" ").append(addon.getAuthor()).append("\n");
                }
                stringBuilder.append("```");
                Main.getInstance().getCommandManager().sendMessage("List of all Addons: " + (stringBuilder.length() == 6 ? "None" : stringBuilder), commandEvent.getTextChannel(), commandEvent.getInteractionHook());
            } else {
                if (commandEvent.getArguments()[0].equalsIgnoreCase("reload")) {
                    Main.getInstance().getCommandManager().sendMessage("Reloading Addons ...", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                    Main.getInstance().getAddonManager().reload();
                    Main.getInstance().getCommandManager().sendMessage("Reloaded Addons!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                } else {
                    Main.getInstance().getCommandManager().sendMessage("Invalid Argument!", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
                }
            }
        } else {
            Main.getInstance().getCommandManager().sendMessage("This Command is not made for users.", 5, commandEvent.getTextChannel(), commandEvent.getInteractionHook());
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