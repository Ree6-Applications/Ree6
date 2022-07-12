package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "optout", description = "Opts you out of any data collection on this Guild.", category = Category.INFO)
public class Optout implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getCommandManager().sendMessage("This command is yet to be implemented.",
                commandEvent.getChannel(), commandEvent.getInteractionHook());

        /* Main.getInstance().getCommandManager().sendMessage("You have successfully opted out of any data collection on this Guild.",
                commandEvent.getTextChannel(), commandEvent.getInteractionHook());*/
        // TODO implement
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[] { "opt-out", "out", "opt", "privacy", "ireject" };
    }
}
