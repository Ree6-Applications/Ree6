package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.*;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Command(name = "support", description = "Get an Invite to the Support Server of Ree6!", category = Category.INFO)
public class Support implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        Main.getInstance().getCommandManager().sendMessage("Join our Support Discord Server!\nhttps://support.ree6.de/", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }

    @Override
    public String[] getAlias() {
        return new String[] { "sup", "supp" };
    }
}
