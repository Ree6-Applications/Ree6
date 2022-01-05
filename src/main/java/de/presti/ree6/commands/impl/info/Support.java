package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;

public class Support extends Command {

    public Support() {
        super("support", "Get a Invite Link to the Support Server!", Category.INFO, new String[] { "sup", "supp" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage("<https://support.ree6.de/>", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
