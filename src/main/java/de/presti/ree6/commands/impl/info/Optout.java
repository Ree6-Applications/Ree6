package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * A command to opt-out of data collection.
 */
@Command(name = "optout", description = "Opts you out of any data collection on this Guild.", category = Category.INFO)
public class Optout implements ICommand {

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (Main.getInstance().getSqlConnector().getSqlWorker().isOptOut(commandEvent.getGuild().getId(), commandEvent.getMember().getId())) {
            Main.getInstance().getSqlConnector().getSqlWorker().optIn(commandEvent.getGuild().getId(), commandEvent.getMember().getId());
            Main.getInstance().getCommandManager().sendMessage("You are now opted in to data collection!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
        } else {
            Main.getInstance().getSqlConnector().getSqlWorker().optOut(commandEvent.getGuild().getId(), commandEvent.getMember().getId());
            Main.getInstance().getCommandManager().sendMessage("You are now opted out of data collection!", 5, commandEvent.getChannel(), commandEvent.getInteractionHook());
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
        return new String[] { "opt-out", "out", "opt", "privacy" };
    }
}
