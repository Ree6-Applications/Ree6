package de.presti.ree6.commands.impl.info;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandClass;
import de.presti.ree6.commands.CommandEvent;

public class Invite extends CommandClass {

    public Invite() {
        super("invite", "Get a Invite Link for Ree6!", Category.INFO, new String[] { "inv" });
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage("https://ree6.de/index.html#invite", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
