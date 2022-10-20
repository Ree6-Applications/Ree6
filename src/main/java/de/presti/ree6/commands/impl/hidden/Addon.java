package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to either reload all Addons or list all of them.
 */
@Command(name = "addon", description = "command.description.addon", category = Category.HIDDEN)
public class Addon implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if(commandEvent.getMember().getUser().getId().equalsIgnoreCase("321580743488831490")) {
            if (commandEvent.getArguments().length == 0) {

                StringBuilder stringBuilder = new StringBuilder("```");
                for (de.presti.ree6.addons.Addon addon : Main.getInstance().getAddonManager().addons) {
                    stringBuilder.append(addon.getName()).append("v").append(addon.getVersion())
                            .append(" ").append("for").append(" ").append("by").append(" ").append(addon.getAuthor()).append("\n");
                }
                stringBuilder.append("```");
                commandEvent.reply(commandEvent.getResource("message.addon.list") + " " + (stringBuilder.length() == 6 ? "None" : stringBuilder));
            } else {
                if (commandEvent.getArguments()[0].equalsIgnoreCase("reload")) {
                    commandEvent.reply(commandEvent.getResource("message.addon.reloadAll"));
                    Main.getInstance().getAddonManager().reload();
                    commandEvent.reply(commandEvent.getResource("message.addon.reloadedAll"));
                } else {
                    commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
                }
            }
        } else {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission"), 5);
        }
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
        return new String[0];
    }
}