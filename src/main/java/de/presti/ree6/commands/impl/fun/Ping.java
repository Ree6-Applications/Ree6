package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;
public class Ping extends Command {

    public Ping() {
        super("ping", "Pong!", Category.FUN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
        sendMessage("Pong", commandEvent.getTextChannel(), commandEvent.getInteractionHook());
    }
}
