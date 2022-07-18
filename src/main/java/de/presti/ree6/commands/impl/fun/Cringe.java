package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

@Command(name = "cringe", description = "Let shrek tell them that their message was cringe!", category = Category.FUN)
public class Cringe implements ICommand {

    @Override
    public void onPerform(CommandEvent commandEvent) {
        commandEvent.getChannel().getHistoryBefore(commandEvent.getMessage().getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/cringe.gif").queue();
        if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        Main.getInstance().getCommandManager().deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("shrekImage", "Let shrek tell them that their message was not funny!");
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
