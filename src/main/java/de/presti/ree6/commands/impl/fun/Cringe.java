package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;

public class Cringe extends Command {


    public Cringe() {
        super("cringe", "Tell someone that his message is CRINGE", Category.LEVEL);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {

        commandEvent.getTextChannel().getHistoryBefore(commandEvent.getMessage().getId(), 1).complete().getRetrievedHistory().get(0).reply("https://images.ree6.de/cringe.gif").queue();
        if (commandEvent.isSlashCommand()) commandEvent.getInteractionHook().sendMessage("Check below!").queue();
        deleteMessage(commandEvent.getMessage(), commandEvent.getInteractionHook());
    }
}
