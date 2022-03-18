package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.Command;
import de.presti.ree6.commands.CommandEvent;

public class Test extends Command {

    public Test() {
        super("sdasdwdawrgawhadawrt45646fwng", "test", Category.HIDDEN);
    }

    @Override
    public void onPerform(CommandEvent commandEvent) {
    }
}
